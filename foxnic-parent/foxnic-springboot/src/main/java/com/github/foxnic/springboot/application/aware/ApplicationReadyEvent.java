package com.github.foxnic.springboot.application.aware;

import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;

public interface ApplicationReadyEvent  extends  ApplicationListener<ApplicationStartedEvent> {
}
