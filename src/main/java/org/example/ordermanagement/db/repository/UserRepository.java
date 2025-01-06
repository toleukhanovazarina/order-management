package org.example.ordermanagement.db.repository;

import org.example.ordermanagement.db.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {

    User findByUsername(String username);
}
