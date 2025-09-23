package com.suracle.backend_api.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "requirements.api")
@Getter
@Setter
public class ApiKeysProperties {
    private String usdaKey;
    private String cbpKey;
    private String dataGovKey;
    private String epaKey;
}


