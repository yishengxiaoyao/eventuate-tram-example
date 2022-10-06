package com.edu.ftgo.domain;

import com.edu.ftgo.event.CustomerAddEvent;
import io.eventuate.tram.events.publisher.ResultWithEvents;
import javax.persistence.*;
import static java.util.Collections.singletonList;
@Entity
@Table(name = "customer")
@Access(AccessType.FIELD)
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String email;
    private String country;
    public Customer() {
    }
    public Customer(String name, String email, String country) {
        super();
        this.name = name;
        this.email = email;
        this.country = country;
    }
    public static ResultWithEvents<Customer> add(String name, String email, String country) {
        Customer customer = new Customer(name, email, country);
        return new ResultWithEvents<>(customer,
                singletonList(new CustomerAddEvent(customer.getName(), customer.getEmail(), customer.getCountry())));
    }
    public Long getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public String getEmail() {
        return email;
    }
    public String getCountry() {
        return country;
    }
}