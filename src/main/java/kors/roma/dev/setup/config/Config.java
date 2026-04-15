package kors.roma.dev.setup.config;

import java.security.SecureRandom;

import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.rabbitmq.client.ConnectionFactory;

import kors.roma.dev.common.Logger;
import kors.roma.dev.common.RabbitUserEventPublisher;
import kors.roma.dev.common.UserLifecycleEventPublisherFactory;

@Configuration
public class Config {
    
    @Bean
    public PasswordEncoder bCrypt(){
        return new BCryptPasswordEncoder(14,new SecureRandom());
    }

    @Bean
    public Logger logger(){
        Logger logger = new Logger(){
            @Override
            public void logInfo(String str){
                System.out.println("[INFO]: " + str);
            };
            @Override
            public void logErr(String str,Exception e){
                System.out.println("[ERR]" + str);
                // ignore the Exception for now, but in general save relevant info
                // for viewing and debugging purposes when errors occur.
            };
        };
        return logger;
    }

    @Bean
    @Qualifier("rabbit-pool")
    public GenericObjectPool<RabbitUserEventPublisher> genericRmqMsgPool(
        @Autowired ConnectionFactory connfac) throws Exception
    {
        var conn = connfac.newConnection();
        System.out.println("-----------------------------CREATED CONNECTION---------------------------");
        GenericObjectPoolConfig<RabbitUserEventPublisher> config = 
            new GenericObjectPoolConfig<>();
        config.setMaxTotal(4);
        return new GenericObjectPool<>(
            new UserLifecycleEventPublisherFactory(conn),
            config
        );
    }
}
