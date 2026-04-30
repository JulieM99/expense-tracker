package org.example.authcommon;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jwt")
@Getter
@Setter
public class JwtProperties {
    private String secret;

    public String getSecret() { return secret; }
    public void setSecret(String secret) { this.secret = secret; }
}
