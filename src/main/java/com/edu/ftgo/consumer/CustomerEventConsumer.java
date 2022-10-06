package com.edu.ftgo.consumer;

import com.edu.ftgo.event.CustomerAddEvent;
import io.eventuate.tram.events.subscriber.DomainEventEnvelope;
import io.eventuate.tram.events.subscriber.DomainEventHandlers;
import io.eventuate.tram.events.subscriber.DomainEventHandlersBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CustomerEventConsumer {
    public DomainEventHandlers domainEventHandlers() {
        return DomainEventHandlersBuilder
                .forAggregateType("com.edu.ftgo.domain.Customer")
                .onEvent(CustomerAddEvent.class, this::createCustomer)
                .build();
    }

    private void createCustomer(DomainEventEnvelope<CustomerAddEvent> de) {
        String restaurantIds = de.getAggregateId();
        long id = Long.parseLong(restaurantIds);
        log.info("create customer {} successfully!!!", de.getEvent().getName());
    }
}