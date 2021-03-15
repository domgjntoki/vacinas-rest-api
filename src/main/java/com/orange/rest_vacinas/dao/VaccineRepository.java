package com.orange.rest_vacinas.dao;

import com.orange.rest_vacinas.entity.Vaccine;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VaccineRepository extends JpaRepository<Vaccine, Long> {
}
