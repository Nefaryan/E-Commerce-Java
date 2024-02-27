package com.nefdev.ecommerce.dao;

import com.nefdev.ecommerce.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserDAO extends JpaRepository<User,Long> {

    Optional<User> findByUsernameIgnoreCase(String username);

    Optional<User> findByEMailIgnoreCase(String eMail);
}
