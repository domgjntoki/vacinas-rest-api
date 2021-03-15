package com.orange.rest_vacinas.rest;

import com.orange.rest_vacinas.entity.User;
import com.orange.rest_vacinas.entity.Vaccine;
import com.orange.rest_vacinas.rest.error_handling.InvalidFormException;
import com.orange.rest_vacinas.rest.error_handling.UserNotFoundException;
import com.orange.rest_vacinas.service.UserVaccineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

@RestController
public class VaccineApiRestController {

    private final UserVaccineService service;

    @Autowired
    public VaccineApiRestController(UserVaccineService service) {
        this.service = service;
    }

    @PostMapping("usuarios")
    public ResponseEntity<RestResponse> registerUser(
            @Valid @RequestBody User user, BindingResult result) {
        verifyRegisterData(result); // Se houver dados mal formatados, lança um InvalidFormException
        service.registerUser(user);

        return new ResponseEntity<>(
                new RestResponse(HttpStatus.CREATED.value(), "Usuário cadastrado com sucesso!"),
                HttpStatus.CREATED
        );
    }

    @PostMapping("vacinas")
    public ResponseEntity<RestResponse> registerVaccine(
            @Valid @RequestBody Vaccine vaccine, BindingResult result) {
        verifyRegisterData(result); // Se houver dados mal formatados, lança um InvalidFormException
        User user = getUserFromDatabase(vaccine.getUser());
        if(user == null) throw new UserNotFoundException("Usuário não encontrado na base de dados");

        vaccine.setUser(user);
        service.registerVaccine(vaccine);
        return new ResponseEntity<>(
                new RestResponse(HttpStatus.CREATED.value(), "Vacina cadastrada com sucesso!"),
                HttpStatus.CREATED
        );
    }

    private void verifyRegisterData(BindingResult result) throws InvalidFormException {
        if (result.hasErrors()) {
            for (FieldError error : result.getFieldErrors()) {
                throw new InvalidFormException("Dados de cadastro inválidos.", error.getField());
            }
        }
    }

    private User getUserFromDatabase(User user) {
        if(user == null) return null;

        if(user.getCpf() != null) {
            user = service.getUserByCpf(user.getCpf());
        } else if(user.getEmail() != null) {
            user = service.getUserByEmail(user.getEmail());
        }
        return user;
    }
}
