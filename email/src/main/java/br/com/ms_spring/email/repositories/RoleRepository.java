package br.com.ms_spring.email.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.ms_spring.email.models.RoleModel;

public interface RoleRepository extends JpaRepository<RoleModel, Long> {
    RoleModel findByName(String name);
}
