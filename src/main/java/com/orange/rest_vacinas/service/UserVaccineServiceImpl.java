package com.orange.rest_vacinas.service;

import com.orange.rest_vacinas.dao.UserRepository;
import com.orange.rest_vacinas.dao.VaccineRepository;
import com.orange.rest_vacinas.entity.User;
import com.orange.rest_vacinas.entity.Vaccine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserVaccineServiceImpl implements UserVaccineService {

    private final UserRepository userRepository;

    private final VaccineRepository vaccineRepository;

    @Autowired
    public UserVaccineServiceImpl(UserRepository userRepository,
                                  VaccineRepository vaccineRepository) {
        this.userRepository = userRepository;
        this.vaccineRepository = vaccineRepository;
    }

    @Override
    public Vaccine registerVaccine(Vaccine vaccine) {
        // Garante que irá registrar uma nova vacina settando id para 0
        vaccine.setId(0L);
        return vaccineRepository.save(vaccine);
    }

    @Override
    public User registerUser(User user) {
        if(userRepository.findById(user.getCpf()).isPresent())
            throw new DuplicateKeyException("Usuário com cpf já encontrado");
        else
            return userRepository.save(user);
    }

    @Override
    public User getUserByCpf(String cpf) {
        return userRepository.getUserByCpf(cpf);
    }

    @Override
    public User getUserByEmail(String email) {
        return userRepository.getUserByEmail(email);
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}
