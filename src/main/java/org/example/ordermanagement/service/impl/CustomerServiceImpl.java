package org.example.ordermanagement.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.ordermanagement.db.entity.Customer;
import org.example.ordermanagement.db.repository.CustomerRepository;
import org.example.ordermanagement.service.CustomerService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {
    private final CustomerRepository customerRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return null;
    }

    @Override
    public UserDetailsService userDetailsService() {
        return this::getByUsername;
    }

    public Customer getByUsername(String username) {
        return customerRepository.findByUsername(username);

    }
}
