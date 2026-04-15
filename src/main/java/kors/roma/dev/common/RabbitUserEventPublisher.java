package kors.roma.dev.common;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantLock;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

import kors.roma.dev.messages.Message;
import lombok.Getter;
import lombok.Setter;


@Setter
@Getter
/**
 * This class is horrible if I am being honest,
 * if the Channel is dead, the object pool closes it, and just
 * spins up another RabbitUserEventPublisher wrapped in a PooledObject
 * but then the initialization is somewhat trivial, there is for sure
 * more to do, better to add a Channel/Connection manager/supplier,
 * and just resinstntiate the channel, instead of tearing down the entire
 * RabbituserEventPublisher object and creating a new one.
 * Should just use a Spring AMPQ channelFactory.
 */
public class RabbitUserEventPublisher implements UserEventPublisher{
    private final Connection conn;
    private Channel chan;
    private final String KEY = "user_event";
    private ConcurrentNavigableMap<Long,Message> inflightMessages =
        new ConcurrentSkipListMap<>();

    private final String USERS_EXCHANGE = "users-exchange";

    private final ReentrantLock sequencedPublishLock = new ReentrantLock();

    public RabbitUserEventPublisher(Connection conn) 
        throws IOException,TimeoutException
    {
        this.conn = conn;
        chan = conn.createChannel();
        Map<String,Object> args = Map.of("x-queue-type", "quorum");
        chan.exchangeDeclare(USERS_EXCHANGE, BuiltinExchangeType.FANOUT,
            true, false,args);
        chan.queueDeclare("user_events_q", true, false, false, args);
        chan.queueBind("user_events_q", USERS_EXCHANGE, "irrelevant");
        chan.confirmSelect();
        chan.addConfirmListener(
            (tag, batched) -> { //ACK handler
                try{
                    if(batched){
                        var acked = inflightMessages.headMap(tag, true);
                        acked.clear();
                        System.out.println("acked up to tag" + tag);
                    }
                    else{
                        inflightMessages.remove(tag);
                    }
                }
                catch(Exception e){
                    //log it, but not to System.out it's dogwater and slow
                    // unless writes are buffered and flushed later, Ill check later
                }
                finally{

                }
            },
            (tag, batched) -> { //NACK handler
                try {
                    if(!batched){
                        var msg = inflightMessages.remove(tag);

                        if(msg == null) return;
                        
                        if(msg.shouldRetry()){
                            msg.incRetries();
                            publishHelper(msg);
                        }
                        else{
                            // [log]/[persist to db]/[send to DLX]/[custom-scheme]
                        }

                    }
                    else{
                        //remember, the underlying view is not consistent
                        var nacked = inflightMessages.headMap(tag,true);
                        for (var entry : nacked.entrySet()) {
                            if(entry == null) continue; //someone else took it alrdy
                            
                            var msg = inflightMessages.remove(entry.getKey());
                            if(msg.shouldRetry()){
                                msg.incRetries();
                                publishHelper(msg);
                            }
                            else{
                                // reached the point where we retried maxRetries times:
                                // [log]/[persist to db]/[send to DLX]/[custom-scheme]
                            }
                        }
                    }
                    System.out.println("[debug] got NACK for message");
                } 
                catch (Exception e) {
                    //noop now, log it or something
                }
                finally{

                }
            });
    }

    private void publishHelper(Message msg) throws Exception{
       /*   ------------------------------------------------------------ *
        *    this is not optimal at all, the Rabbit Client reading       *
        *   from the socket is now dependent on a lock it has no control *
        *  of, the confirmListener callback uses publishHelper(), so     *
        *  another thread that dies and locks the lock, blocks the reader*
        * thread managed by the Rabbit Client. think of a better solution*
        * also, if basicPublish errors, we need to clean the entry we put*
        * when calling inflightMessages.put(seq,msg).                    *
        * I use double try-catch block inside , anyway it's wierd af     *
        * ---------------------------------------------------------------*/

        try{
            sequencedPublishLock.lock();
            var seq = chan.getNextPublishSeqNo();
            inflightMessages.put(seq,msg);
            try {
                chan.basicPublish(USERS_EXCHANGE, KEY, null, msg.getData());
            } catch (IOException e) {
                inflightMessages.remove(seq);
            }
        }
        finally{
            sequencedPublishLock.unlock();
        }

    }

    @Override
    public void publish(byte[] data,int maxRetries){
        try {
            Message msg = new Message(data,maxRetries);
            publishHelper(msg);
        } 
        catch (IOException e) {
            System.out.println("Couldn't publish User creation event");
        }
        catch (Exception e){
            System.out.println("Inflight Message Map error");
        }
    }

    @Override
    public boolean isValid() {
        //read rabbitmq docs, it says isOpen() is not enough in prod.
        return this.chan.isOpen();
    }
}
