package kors.roma.dev.setup.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Component
@ConfigurationProperties(prefix="app.rabbitmq")
@Getter
@Setter
@NoArgsConstructor
public class RabbitSettings {
    private String host;
    private String vhost;
    private Integer port;
    private String username;
    private String password;
}
