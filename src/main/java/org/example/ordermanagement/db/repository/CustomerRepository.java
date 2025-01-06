package org.example.ordermanagement.db.repository;

import org.example.ordermanagement.db.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepository extends JpaRepository<Customer,Long> {

    Customer findByUsername(String username);
}
