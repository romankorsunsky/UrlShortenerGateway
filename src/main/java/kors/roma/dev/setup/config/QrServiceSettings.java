package kors.roma.dev.setup.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Component
@ConfigurationProperties(prefix="services.qr_service")
@NoArgsConstructor
@Getter
@Setter
public class QrServiceSettings {
    private String ip;
}
