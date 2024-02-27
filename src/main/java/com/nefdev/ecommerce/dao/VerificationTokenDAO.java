package com.nefdev.ecommerce.dao;

import com.nefdev.ecommerce.model.User;
import com.nefdev.ecommerce.model.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VerificationTokenDAO extends JpaRepository<VerificationToken,Long> {

    Optional<VerificationToken> findByToken(String token);

    void deleteByUser(User user);
}
