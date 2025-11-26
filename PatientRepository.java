package com.healthtrack360.repository;

import com.healthtrack360.domain.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Long> {

    boolean existsByPhoneNumber(String phoneNumber);

    Optional<Patient> findByUserId(Long userId);

    Optional<Patient> findByPatientCode(String patientCode);
}
