package com.busline.tranmaunhan.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.TimeZone;

@Configuration
public class TimeZoneConfig {

    private final String timeZoneId;

    public TimeZoneConfig(@Value("${spring.jackson.time-zone:Asia/Ho_Chi_Minh}") String timeZoneId) {
        this.timeZoneId = timeZoneId;
    }

    @PostConstruct
    public void setDefaultTimeZone() {
        TimeZone.setDefault(TimeZone.getTimeZone(timeZoneId));
    }
}
