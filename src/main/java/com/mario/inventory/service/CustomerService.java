package com.mario.inventory.service;

import com.mario.inventory.domain.Customer;
import com.mario.inventory.exception.BusinessRuleException;
import com.mario.inventory.exception.NotFoundException;
import com.mario.inventory.repository.CustomerRepository;
import com.mario.inventory.web.dto.CustomerDtos.CustomerRequest;
import com.mario.inventory.web.dto.CustomerDtos.CustomerResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CustomerService {

    private final CustomerRepository customers;

    public CustomerService(CustomerRepository customers) {
        this.customers = customers;
    }

    public Page<CustomerResponse> list(Pageable pageable) {
        return customers.findAll(pageable).map(CustomerResponse::from);
    }

    public CustomerResponse get(Long id) {
        return customers.findById(id)
                .map(CustomerResponse::from)
                .orElseThrow(() -> new NotFoundException("Cliente", id));
    }

    @Transactional
    public CustomerResponse create(CustomerRequest request) {
        if (customers.existsByEmail(request.email())) {
            throw new BusinessRuleException("Ya existe un cliente con email " + request.email());
        }
        return CustomerResponse.from(customers.save(new Customer(request.name(), request.email(), request.taxId())));
    }
}
