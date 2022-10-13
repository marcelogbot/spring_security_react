package br.com.ms_spring.email.controllers;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;

import br.com.ms_spring.email.services.RegistrationService;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping(path = "api/v1/registration")
@CrossOrigin(origins = "http://localhost:3000/")
@AllArgsConstructor
public class RegistrationController {

    private RegistrationService registrationService;

    @PostMapping
    public String register(@RequestBody RegistrationRequest request) throws JsonProcessingException {
        return registrationService.register(request);
    }

    @GetMapping(path = "confirm")
    @CrossOrigin(origins = "http://localhost:3000/")
    public String confirm (@RequestParam("token") String token) {
    
        return registrationService.confirmToken(token);
    }
    
}
