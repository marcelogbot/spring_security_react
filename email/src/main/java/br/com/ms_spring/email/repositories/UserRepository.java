package br.com.ms_spring.email.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.ms_spring.email.models.UserModel;

public interface UserRepository extends JpaRepository<UserModel, Long> {
    UserModel findByUsername(String userName); 
    UserModel findByEmail(String email); 
}
