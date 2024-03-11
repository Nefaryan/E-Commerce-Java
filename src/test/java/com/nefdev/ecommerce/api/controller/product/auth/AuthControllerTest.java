package com.nefdev.ecommerce.api.controller.product.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nefdev.ecommerce.api.modelDTO.RegistrationDTO;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;


    @Test
    @Transactional
    public void testResgisterUser() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        RegistrationDTO registrationDTO = new RegistrationDTO();

        registrationDTO.setUsername(null);
        registrationDTO.setEmail("AuthControllerTest$Registre@Junit.com");
        registrationDTO.setFirstName("FirstName");
        registrationDTO.setLastName("LastName");
        registrationDTO.setPassword("Password123");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(registrationDTO)))

                .andExpect(status().is(HttpStatus.INTERNAL_SERVER_ERROR.value()));

    }

}
