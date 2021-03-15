package com.orange.rest_vacinas.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import org.hibernate.validator.constraints.br.CPF;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import java.sql.Date;
import java.util.List;

@Entity
@Table(name="user")
public class User {
    @NotNull
    @Column(name="name")
    private String name;

    @CPF
    @NotNull
    @Id
    @Column(name="cpf")
    private String cpf;

    @Email
    @NotNull
    @Column(name="email")
    private String email;

    @Column(name="birth_date")
    @NotNull
    private Date birthDate;

    @OneToMany(mappedBy="user",
            cascade=CascadeType.ALL)
    @JsonManagedReference // Evita recursão infinita ao serializar para json.
    // Quando um usuário for deletado, queremos deletar todas as vacinas relacionadas
    List<Vaccine> vaccines;

    // Construtores padrões e getters/setters ...

    public User() {
    }

    public User(@NotNull String name, @CPF @NotNull String cpf,
                @Email @NotNull String email, @NotNull Date birthDate) {
        this.name = name;
        this.cpf = cpf;
        this.email = email;
        this.birthDate = birthDate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Date getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }

    public List<Vaccine> getVaccines() {
        return vaccines;
    }

    public void setVaccines(List<Vaccine> vaccines) {
        this.vaccines = vaccines;
    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", cpf='" + cpf + '\'' +
                ", email='" + email + '\'' +
                ", birthDate=" + birthDate +
                '}';
    }
}
