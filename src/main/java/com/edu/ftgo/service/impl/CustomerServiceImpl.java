package com.edu.ftgo.service.impl;

import com.edu.ftgo.domain.Customer;
import com.edu.ftgo.repository.CustomerRepository;
import com.edu.ftgo.service.CustomerService;
import io.eventuate.tram.events.publisher.DomainEventPublisher;
import io.eventuate.tram.events.publisher.ResultWithEvents;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CustomerServiceImpl implements CustomerService {
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private DomainEventPublisher domainEventPublisher;

    public Customer addCustomer(String name, String email, String country) {
        ResultWithEvents<Customer> customerWithAddEvents = Customer.add(name, email, country);
        Customer customer = customerRepository.save(customerWithAddEvents.result);
        domainEventPublisher.publish(Customer.class, customer.getId(), customerWithAddEvents.events);
        return customer;
    }
}
