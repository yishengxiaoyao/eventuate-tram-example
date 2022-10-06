package com.edu.ftgo.service;

import com.edu.ftgo.domain.Customer;

public interface CustomerService {

    public Customer addCustomer(String name, String email, String country);
}