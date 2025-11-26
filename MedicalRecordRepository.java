package com.healthtrack360.repository;

import com.healthtrack360.domain.MedicalRecord;
import com.healthtrack360.domain.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {

    List<MedicalRecord> findByPatient(Patient patient);
}
