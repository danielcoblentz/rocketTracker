package com.rockettracker.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import com.rockettracker.model.User; 
import java.util.Optional; 

@Repository
public interface UserRepository extends CrudRepository<User, Long> { 

    Optional<User> findByEmail(String email); 
    Optional<User> findByVerificationCode(String verificationCode); 
}
