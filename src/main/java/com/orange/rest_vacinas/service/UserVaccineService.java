package com.orange.rest_vacinas.service;

import com.orange.rest_vacinas.entity.User;
import com.orange.rest_vacinas.entity.Vaccine;

import java.util.List;

public interface UserVaccineService {
    Vaccine registerVaccine(Vaccine vaccine);

    User registerUser(User user);

    User getUserByCpf(String cpf);

    User getUserByEmail(String email);

    List<User> getAllUsers();
}
