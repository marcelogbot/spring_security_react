package br.com.ms_spring.email;

import java.time.LocalDateTime;
import java.util.ArrayList;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import br.com.ms_spring.email.models.RoleModel;
import br.com.ms_spring.email.models.UserModel;
import br.com.ms_spring.email.services.UserService;

@SpringBootApplication
public class EmailApplication {

	public static void main(String[] args) {
		SpringApplication.run(EmailApplication.class, args);
	}

	@Bean
	CommandLineRunner run(UserService userService) {
		return args -> {
			userService.saveRole(new RoleModel(null,"ROLE_USER"));
			userService.saveRole(new RoleModel(null,"ROLE_ADMIN"));
			
			userService.saveUser(new UserModel(null, "Marcelo", "G Botelho",
											 "marcelo", "1234", "marcunb@gmail.com",
											  LocalDateTime.now(), new ArrayList<>(), false, true));
			
			userService.addRoleToUser("marcelo", "ROLE_ADMIN");
			userService.addRoleToUser("marcelo", "ROLE_USER");
		};
	}


}
