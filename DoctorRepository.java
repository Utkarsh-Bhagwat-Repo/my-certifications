package com.healthtrack360.repository;

import com.healthtrack360.domain.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    List<Doctor> findByEnabledTrue();

    Optional<Doctor> findByUserId(Long userId);
}
