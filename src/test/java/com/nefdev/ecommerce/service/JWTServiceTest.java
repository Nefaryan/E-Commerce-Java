package com.nefdev.ecommerce.service;

import com.nefdev.ecommerce.dao.UserDAO;
import com.nefdev.ecommerce.model.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class JWTServiceTest {

    @Autowired
    private JWTService jwtService;

    @Autowired
    private UserDAO userDAO;

    @Test
    public void testVerificationTokenNotUsableForLogin(){
        User user = userDAO.findByUsernameIgnoreCase("UserA").get();
        String token = jwtService.generateVerificationJWT(user);
        Assertions.assertNull(jwtService.getUsername(token),"Verification token not contain a username");
    }

    @Test
    public void testAuthTokenReturnUsername(){
        User user = userDAO.findByUsernameIgnoreCase("UserA").get();
        String token = jwtService.generateJWT(user);
        Assertions.assertEquals(user.getUsername(), jwtService.getUsername(token),
                "Token for auth contain the username");
    }
}
