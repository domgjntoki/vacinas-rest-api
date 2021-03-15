package com.orange.rest_vacinas.dao;

import com.orange.rest_vacinas.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, String> {
    // Vamos usar essas query para adicionar vacinas a partir do email ou cpf do usuário no futuro
    @Query("from User where cpf=:cpf")
    User getUserByCpf(@Param("cpf") String cpf);

    @Query("from User where email=:email")
    User getUserByEmail(@Param("email") String email);
}
