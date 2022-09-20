package br.com.ms_spring.email.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.ms_spring.email.models.EmailModel;

public interface EmailRepository extends JpaRepository<EmailModel, Long> {

     

    
}
