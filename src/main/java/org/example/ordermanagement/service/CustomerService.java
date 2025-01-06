package org.example.ordermanagement.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public interface CustomerService {
    UserDetails loadUserByUsername(String username) throws UsernameNotFoundException;

    UserDetailsService userDetailsService();
}
