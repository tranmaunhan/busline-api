package com.busline.tranmaunhan.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.notification.email")
public class EmailNotificationProperties {

    private boolean enabled;
    private String fromAddress;
    private String fromName = "SaigonST BusLine";

    public boolean canSendTo(String email) {
        return enabled
                && StringUtils.hasText(email)
                && StringUtils.hasText(fromName);
    }
}
