package kors.roma.dev.common;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

import com.rabbitmq.client.Connection;

import lombok.Getter;

@Getter
public class UserLifecycleEventPublisherFactory 
    extends BasePooledObjectFactory<RabbitUserEventPublisher>{

    private final Connection conn;

    public UserLifecycleEventPublisherFactory(Connection conn){
        super();
        this.conn = conn;
    }
    @Override
    public RabbitUserEventPublisher create() throws Exception {
        return new RabbitUserEventPublisher(conn);
    }

    @Override
    public PooledObject<RabbitUserEventPublisher> wrap(RabbitUserEventPublisher obj) {
        return new DefaultPooledObject<>(obj);
    }
    
    @Override
    public boolean validateObject(PooledObject<RabbitUserEventPublisher> publisher){
        return publisher.getObject().isValid();
    }

    @Override
    public void destroyObject(PooledObject<RabbitUserEventPublisher> publisher)
        throws Exception
    {
        //close the underlying connection
        publisher.getObject().getChan().close();
    }
}
