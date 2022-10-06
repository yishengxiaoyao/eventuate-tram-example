package com.edu.ftgo.controller;

import com.edu.ftgo.domain.Customer;
import com.edu.ftgo.domain.CustomerRequest;
import com.edu.ftgo.domain.CustomerResponse;
import com.edu.ftgo.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CustomerController {
    @Autowired
    private CustomerService customerService;

    @Autowired
    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @RequestMapping(value = "/customer", method = RequestMethod.POST)
    public CustomerResponse createCustomer(@RequestBody CustomerRequest createCustomerRequest) {
        Customer customer = customerService.addCustomer(createCustomerRequest.getName(),
                createCustomerRequest.getEmail(), createCustomerRequest.getCountry());
        return new CustomerResponse(customer.getId());
    }
}