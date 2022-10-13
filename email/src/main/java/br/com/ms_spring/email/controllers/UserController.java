package br.com.ms_spring.email.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import br.com.ms_spring.email.models.RoleModel;
import br.com.ms_spring.email.models.UserModel;
import br.com.ms_spring.email.services.UserService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/users")
    public ResponseEntity<List<UserModel>> getAllUsers() throws JsonProcessingException {
        return ResponseEntity.ok().body(userService.getAllUsers());
    }

    @GetMapping("/user/{username}")
    public ResponseEntity<UserModel> getUser(@PathVariable(value = "username") String username) {
        return ResponseEntity.ok().body(userService.getUser(username));
    }

    @PostMapping("/user/save")
    public ResponseEntity<?> saveUser(@RequestBody UserModel newUser) {
        URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/api/user/save").toUriString());

        return ResponseEntity.created(uri).body(userService.saveUser(newUser));
    }

    @PutMapping("/user/update")
    public ResponseEntity<?> updateUser(@RequestBody UserModel newUser) {
        URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/api/user/update").toUriString());

        return ResponseEntity.created(uri).body(userService.updateUser(newUser));
    }

    @DeleteMapping("/user/delete")
    public void deleteUser(@RequestBody UserModel user) {
        if(user.getUsername() == null) {
            user.setUsername("");
        }
        UserModel userModel = userService.getUser(user.getUsername());
        userService.deleteUser(userModel);
        log.info("User ({}) deleted.", userModel.getUserID()+"-"+userModel.getUsername());
    }

    @PutMapping("/user/enable") 
    public void enableUser(@RequestBody UserModel userToEnable) {
        if(userToEnable.getUsername() == null) {
            userToEnable.setUsername("");
        }
        UserModel userModel = userService.getUser(userToEnable.getUsername());
        userService.enableUserModel(userModel);
    }

    @PostMapping("/role/save")
    public ResponseEntity<RoleModel> saveRole(@RequestBody RoleModel newRole) {
        URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/api/role/save").toUriString());
        return ResponseEntity.created(uri).body(userService.saveRole(newRole));
    }

    @PostMapping("/role/addtouser")
    public ResponseEntity<?> addRoleToUser(@RequestBody RoleToUserForm form) {
        log.info("Form: {}", form);
        userService.addRoleToUser(form.getUsername(), form.getRoleName());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/role/removetouser")
    public ResponseEntity<?> removeRoleToUser(@RequestBody RoleToUserForm form) {
        userService.removeRoleToUser(form.getUsername(), form.getRoleName());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/token/refresh")
    public void  refreshToken (HttpServletRequest request, HttpServletResponse response) throws IOException {
        String authorizationHeader = request.getHeader(org.springframework.http.HttpHeaders.AUTHORIZATION);
        
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                
            try {
                ObjectMapper mapper = new ObjectMapper();
                mapper.registerModule(new JavaTimeModule());

                String refresh_token = authorizationHeader.substring("Bearer ".length());
                Algorithm algorithm = Algorithm.HMAC256("MSEmail1002".getBytes());
                JWTVerifier verifier = JWT.require(algorithm).build();
                DecodedJWT decodedJWT = verifier.verify(refresh_token);
                String userName = decodedJWT.getSubject();
                UserModel user = userService.getUser(userName);
                
                String access_token = JWT.create()
                    .withSubject(user.getUsername())
                    .withExpiresAt(new Date(System.currentTimeMillis()+10*60*1000))
                    .withIssuer(request.getRequestURL().toString())
                    .withClaim("roles", user.getRoles().stream().map(RoleModel::getName).collect(Collectors.toList()))
                    .sign(algorithm);
                
                refresh_token = JWT.create()
                    .withSubject(user.getUsername())
                    .withExpiresAt(new Date(System.currentTimeMillis()+30*60*1000))
                    .withIssuer(request.getRequestURL().toString())
                    .sign(algorithm);
                    
                //String userJson = mapper.writeValueAsString(user);

                Map<String, String> tokens = new HashMap<>();
                tokens.put("access_token", access_token);
                tokens.put("refresh_token", refresh_token);
                //tokens.put("user", userJson);
                response.setContentType(org.springframework.http.MediaType.APPLICATION_JSON_VALUE);
                new ObjectMapper().writeValue(response.getOutputStream(), tokens);

            } catch (Exception e) {
                log.info("Error: {}", e.getMessage());
                response.setHeader("error", e.getMessage());
                response.setStatus(org.springframework.http.HttpStatus.FORBIDDEN.value());
                Map<String, String> error = new HashMap<>();
                error.put("error_message", e.getMessage());
                response.setContentType(org.springframework.http.MediaType.APPLICATION_JSON_VALUE);
                new ObjectMapper().writeValue(response.getOutputStream(), error);
            }
           
        } else {
            throw new RuntimeException("Refresh token is missing!");
        }

    }
    
    @GetMapping("/roles")
    public ResponseEntity<List<RoleModel>> getAllRoles() {
        return ResponseEntity.ok().body(userService.getAllRoles());
    }

}

@Data
class RoleToUserForm {
    private String username;
    private String roleName;
}
