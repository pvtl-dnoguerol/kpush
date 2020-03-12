package com.whizzosoftware.kpush;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;

import java.util.ArrayList;
import java.util.List;

public class MockApplicationEventPublisher implements ApplicationEventPublisher {
    private List<Object> events = new ArrayList<>();

    @Override
    public void publishEvent(ApplicationEvent event) {
        events.add(event);
    }

    @Override
    public void publishEvent(Object event) {
        events.add(event);
    }

    public int getPublishedEventCount() {
        return events.size();
    }

    public Object getEvent(int ix) {
        return events.get(ix);
    }
}
