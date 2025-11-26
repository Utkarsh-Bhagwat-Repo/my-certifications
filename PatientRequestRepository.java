package com.healthtrack360.repository;

import com.healthtrack360.domain.Doctor;
import com.healthtrack360.domain.Patient;
import com.healthtrack360.domain.PatientRequest;
import com.healthtrack360.domain.enums.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PatientRequestRepository extends JpaRepository<PatientRequest, Long> {

    List<PatientRequest> findByPatientOrderByCreatedAtDesc(Patient patient);

    List<PatientRequest> findByDoctorAndStatusOrderByCreatedAtAsc(Doctor doctor, RequestStatus status);

    boolean existsByPatientAndTextAndStatus(Patient patient, String text, RequestStatus status);
}
