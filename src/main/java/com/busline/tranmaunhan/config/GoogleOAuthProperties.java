package com.busline.tranmaunhan.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.security.google")
public class GoogleOAuthProperties {

    private String clientId;
    private String clientSecret;
    private String redirectUri;

    public boolean isEnabled() {
        return StringUtils.hasText(clientId)
                && StringUtils.hasText(clientSecret)
                && StringUtils.hasText(redirectUri);
    }
}
