package com.healthtrack360.service;

import com.healthtrack360.domain.*;
import com.healthtrack360.domain.enums.RequestStatus;
import com.healthtrack360.dto.PatientRequestDto;
import com.healthtrack360.messaging.NotificationProducer;
import com.healthtrack360.exception.BusinessValidationException;
import com.healthtrack360.exception.ResourceNotFoundException;
import com.healthtrack360.exception.ConflictException;
import com.healthtrack360.exception.UnauthorizedActionException;

import com.healthtrack360.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PatientRequestService {

    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRequestRepository requestRepository;
    private final NotificationProducer notificationProducer;

    public PatientRequestService(PatientRepository patientRepository,
                                 DoctorRepository doctorRepository,
                                 PatientRequestRepository requestRepository,
                                 NotificationProducer notificationProducer) {
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.requestRepository = requestRepository;
        this.notificationProducer = notificationProducer;
    }

    @Transactional
    public PatientRequest submitRequest(Long patientUserId, PatientRequestDto dto) {
        Patient patient = patientRepository.findByUserId(patientUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));
        Doctor doctor = doctorRepository.findById(dto.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));

        String text = dto.getText() == null ? "" : dto.getText().trim();
        if (text.isBlank()) {
            throw new BusinessValidationException("Request text is mandatory");
        }

        if (requestRepository.existsByPatientAndTextAndStatus(patient, text, RequestStatus.PENDING)) {
            throw new ConflictException("Similar pending request already exists");
        }

        PatientRequest request = new PatientRequest();
        request.setPatient(patient);
        request.setDoctor(doctor);
        request.setText(text);
        request.setStatus(RequestStatus.PENDING);
        request.setCreatedAt(LocalDateTime.now());

        PatientRequest saved = requestRepository.save(request);

        String message = "PATIENT_REQUEST_SUBMITTED:" + saved.getId() +
                ":DOCTOR_EMAIL:" + doctor.getUser().getEmail() +
                ":PATIENT_EMAIL:" + patient.getUser().getEmail();
        notificationProducer.sendNotification(message);
        return saved;
    }

    @Transactional
    public PatientRequest decideRequest(Long doctorUserId, Long requestId, boolean approve, String instruction) {
        Doctor doctor = doctorRepository.findByUserId(doctorUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));
        PatientRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found"));
        if (!request.getDoctor().getId().equals(doctor.getId())) {
            throw new UnauthorizedActionException("You are not allowed to modify this patient request");
        }
        if (request.getStatus() != RequestStatus.PENDING) {
            throw new ConflictException("Request decision is already recorded");
        }
        request.setStatus(approve ? RequestStatus.APPROVED : RequestStatus.REJECTED);
        if (instruction != null && !instruction.isBlank()) {
            request.setDoctorInstruction(instruction.trim());
        }
        request.setDecidedAt(LocalDateTime.now());
        PatientRequest saved = requestRepository.save(request);

        String message = "PATIENT_REQUEST_DECIDED:" + saved.getId() +
                ":STATUS:" + saved.getStatus() +
                ":DOCTOR_EMAIL:" + doctor.getUser().getEmail() +
                ":PATIENT_EMAIL:" + saved.getPatient().getUser().getEmail();
        notificationProducer.sendNotification(message);
        return saved;
    }

    public List<PatientRequest> getPendingForDoctor(Long doctorUserId) {
        Doctor doctor = doctorRepository.findByUserId(doctorUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));
        return requestRepository.findByDoctorAndStatusOrderByCreatedAtAsc(doctor, RequestStatus.PENDING);
    }

    public List<PatientRequest> getHistoryForPatient(Long patientUserId) {
        Patient patient = patientRepository.findByUserId(patientUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));
        return requestRepository.findByPatientOrderByCreatedAtDesc(patient);
    }

    public PatientRequest getById(Long id) {
        return requestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found"));
    }
}
