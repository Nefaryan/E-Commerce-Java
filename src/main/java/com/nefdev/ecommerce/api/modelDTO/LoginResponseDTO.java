package com.nefdev.ecommerce.api.modelDTO;

import lombok.Data;

@Data
public class LoginResponseDTO {

    /** The JWT token to be used for authentication. */
    private String jwt;
    /** Was the login process successful? */
    private boolean success;
    /** The reason for failure on login. */
    private String failureReason;
}
