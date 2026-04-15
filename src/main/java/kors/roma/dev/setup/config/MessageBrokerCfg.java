package kors.roma.dev.setup.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.rabbitmq.client.ConnectionFactory;

import lombok.Setter;

@Configuration
@Setter
public class MessageBrokerCfg {
    private RabbitSettings mbSettings;

    public MessageBrokerCfg(RabbitSettings rabbitSettings){
        this.mbSettings = rabbitSettings;
    }
    
    @Bean
    public ConnectionFactory mqConnectionFactory(){
        ConnectionFactory connFactory = new ConnectionFactory();
        connFactory.setHost(mbSettings.getHost());
        connFactory.setVirtualHost(mbSettings.getVhost());
        connFactory.setPort(mbSettings.getPort());
        connFactory.setUsername(mbSettings.getUsername());
        connFactory.setPassword(mbSettings.getPassword());
        return connFactory;
    }
}
