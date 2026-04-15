package kors.roma.dev.common;

import java.nio.charset.StandardCharsets;

import org.apache.commons.pool2.impl.GenericObjectPool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import kors.roma.dev.exceptions.PooledObjectCreationException;
import kors.roma.dev.messages.UserLifecycleEvent;
import kors.roma.dev.model.User;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Component
public class UserPublishAdapter<T extends UserEventPublisher> {

    @Autowired
    @Qualifier("rabbit-pool")
    private GenericObjectPool<T> pubPool;
    private final ObjectMapper jsonMapper = new ObjectMapper();

    private void publishHelper(byte[] encodedData) throws JacksonException,
        PooledObjectCreationException
    {
        T publisher = null;
        try {
            publisher = pubPool.borrowObject();
            publisher.publish(encodedData,1);
        } catch (JacksonException e) {
            System.out.println("Couldn't parse to JSON string");
            throw e;
        }
        catch (Exception e){
            throw new PooledObjectCreationException("Couldn't create UserEventPublisher object");
        }
        finally{
            if(publisher != null){
                if(!publisher.isValid()){
                    try {
                        pubPool.invalidateObject(publisher);
                    } 
                    catch (Exception e) {}
                }
                else{
                    pubPool.returnObject(publisher);
                }
            }
        }
    }
    
    public void publishUserCreated(User user) throws Exception{
        if(user == null){
            throw new IllegalArgumentException();
        }
        var ulcEvent = new UserLifecycleEvent(
            user.getId().toString(),
            user.getUsername(),
            user.getFirstName(),
            user.getLastName(),
            user.getEmail(),
            "delete");
        var encodedData = jsonMapper.writeValueAsString(ulcEvent).
            getBytes(StandardCharsets.UTF_8);
        publishHelper(encodedData);
    }

    public void publishUserDeleted(User user) throws Exception{
        if(user == null){
            throw new IllegalArgumentException();
        }
        var ulcEvent = new UserLifecycleEvent(user.getId().toString(),"","","","","create");
        var encodedData = jsonMapper.writeValueAsString(ulcEvent).
            getBytes(StandardCharsets.UTF_8);
        publishHelper(encodedData);
    }
}
