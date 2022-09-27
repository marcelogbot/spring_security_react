package br.com.ms_spring.email.controllers;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class RegistrationRequest {
    
    private final String name;
    private final String username;
    private final String email;
    private final String password;
    
}
