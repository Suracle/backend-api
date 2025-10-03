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
    private String tavilyKey;
    private String commerceKey;
    private String fccKey;
    private String cpscKey;
    private String ntiaKey;
    private String dotKey;
    private String doeKey;
    private String doiKey;
    private String dolKey;
    
    // Getter methods
    public String getUsdaKey() { return usdaKey; }
    public String getCbpKey() { return cbpKey; }
    public String getDataGovKey() { return dataGovKey; }
    public String getEpaKey() { return epaKey; }
    public String getTavilyKey() { return tavilyKey; }
    public String getCommerceKey() { return commerceKey; }
    public String getFccKey() { return fccKey; }
    public String getCpscKey() { return cpscKey; }
    public String getNtiaKey() { return ntiaKey; }
    public String getDotKey() { return dotKey; }
    public String getDoeKey() { return doeKey; }
    public String getDoiKey() { return doiKey; }
    public String getDolKey() { return dolKey; }
}


