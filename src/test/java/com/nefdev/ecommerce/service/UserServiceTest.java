package com.nefdev.ecommerce.service;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetupTest;
import com.nefdev.ecommerce.api.modelDTO.LoginDTO;
import com.nefdev.ecommerce.api.modelDTO.RegistrationDTO;
import com.nefdev.ecommerce.dao.VerificationTokenDAO;
import com.nefdev.ecommerce.exception.EmailFailureException;
import com.nefdev.ecommerce.exception.UserAlreadyExistsException;
import com.nefdev.ecommerce.exception.UserNotVerifiedException;
import com.nefdev.ecommerce.model.VerificationToken;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class UserServiceTest {


    /** Extension for mocking email sending. */
    @RegisterExtension
    private static GreenMailExtension greenMailExtension = new GreenMailExtension(ServerSetupTest.SMTP)
            .withConfiguration(GreenMailConfiguration.aConfig().withUser("springboot", "secret"))
            .withPerMethodLifecycle(true);
    /** The UserService to test. */
    @Autowired
    private UserService userService;

    @Autowired
    private VerificationTokenDAO tokenDAO;

    /**
     * Tests the registration process of the user.
     * @throws MessagingException Thrown if the mocked email service fails somehow.
     */
    @Test
    @Transactional
    public void testRegisterUser() throws MessagingException {
        RegistrationDTO body = new RegistrationDTO();
        body.setUsername("UserA");
        body.setEmail("UserServiceTest$testRegisterUser@junit.com");
        body.setFirstName("FirstName");
        body.setLastName("LastName");
        body.setPassword("MySecretPassword123");
        Assertions.assertThrows(UserAlreadyExistsException.class,
                () -> userService.registerUser(body), "Username should already be in use.");
        body.setUsername("UserServiceTest$testRegisterUser");
        body.setEmail("UserA@junit.com");
        Assertions.assertThrows(UserAlreadyExistsException.class,
                () -> userService.registerUser(body), "Email should already be in use.");
        body.setEmail("UserServiceTest$testRegisterUser@junit.com");
        Assertions.assertDoesNotThrow(() -> userService.registerUser(body),
                "User should register successfully.");
        Assertions.assertEquals(body.getEmail(), greenMailExtension.getReceivedMessages()[0]
                .getRecipients(Message.RecipientType.TO)[0].toString());
    }

    @Test
    @Transactional
    public void testLoginUser() throws UserNotVerifiedException, EmailFailureException {
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("UserA-NotExist");
        loginDTO.setPassword("PasswordA123-BadPass");
        Assertions.assertNull(userService.loginUser(loginDTO),"The User should not exist.");
        loginDTO.setUsername("UserA");
        Assertions.assertNull(userService.loginUser(loginDTO),"The password is not correct.");
        loginDTO.setPassword("PasswordA123");
        Assertions.assertNotNull(userService.loginUser(loginDTO),"The user login successfully");
        loginDTO.setUsername("UserB");
        loginDTO.setPassword("PasswordB123");
        try{
              userService.loginUser(loginDTO);
              Assertions.assertTrue(false,"User not verified the mail");
        }catch (UserNotVerifiedException ex){
            Assertions.assertTrue(ex.isNewEmailSent(),"Email for verification should be sent.");
            Assertions.assertEquals(1,greenMailExtension.getReceivedMessages().length);
        }
        try{
            userService.loginUser(loginDTO);
            Assertions.assertTrue(false,"User not verified the mail");
        }catch (UserNotVerifiedException ex){
            Assertions.assertFalse(ex.isNewEmailSent(),"Email for verification should not be sent.");
            Assertions.assertEquals(1,greenMailExtension.getReceivedMessages().length);
        }

    }

    @Test
    @Transactional
    public void testVerifyUser() throws UserNotVerifiedException, EmailFailureException {
        Assertions.assertFalse(userService.verifyUser("Bad Token"),"Token ois bad or does not exist should" +
                "return false");
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("UserB");
        loginDTO.setPassword("PasswordB123");
        try{
            userService.loginUser(loginDTO);
            Assertions.assertTrue(false,"User should not verified the mail.");
        }catch (UserNotVerifiedException ex){
            List<VerificationToken> tokens = tokenDAO.findByUser_IdOrderByIdDesc(2L);
            String token = tokens.get(0).getToken();
            Assertions.assertTrue(userService.verifyUser(token),"Token should be valid");
            Assertions.assertNotNull(loginDTO,"The User should now be verified");
        }
    }


}
