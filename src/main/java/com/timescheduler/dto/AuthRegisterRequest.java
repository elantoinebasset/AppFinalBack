package com.timescheduler.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthRegisterRequest {

    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String password;
}
