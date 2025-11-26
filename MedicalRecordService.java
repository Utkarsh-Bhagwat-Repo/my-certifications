package com.healthtrack360.service;

import com.healthtrack360.domain.*;
import com.healthtrack360.dto.MedicalRecordRequest;
import com.healthtrack360.repository.AppointmentRepository;
import com.healthtrack360.repository.MedicalRecordRepository;
import com.healthtrack360.repository.PatientRepository;
import com.healthtrack360.exception.BusinessValidationException;
import com.healthtrack360.exception.ResourceNotFoundException;
import com.healthtrack360.exception.ConflictException;
import com.healthtrack360.exception.UnauthorizedActionException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MedicalRecordService {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final MedicalRecordRepository medicalRecordRepository;

    public MedicalRecordService(AppointmentRepository appointmentRepository,
                                PatientRepository patientRepository,
                                MedicalRecordRepository medicalRecordRepository) {
        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
        this.medicalRecordRepository = medicalRecordRepository;
    }

    @Transactional
    public MedicalRecord createRecord(Long doctorUserId, MedicalRecordRequest request) {
        Appointment appointment = appointmentRepository.findById(request.getAppointmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));

        Doctor doctor = appointment.getDoctor();
        if (doctor.getUser() == null || !doctor.getUser().getId().equals(doctorUserId)) {
            throw new UnauthorizedActionException("You are not allowed to create a record for this appointment");
        }

        if (appointment.getMedicalRecord() != null) {
            throw new ConflictException("Medical record already exists for this appointment");
        }

        if (request.getDiagnosis() == null || request.getDiagnosis().isBlank()) {
            throw new BusinessValidationException("Diagnosis is mandatory");
        }

        Patient patient = appointment.getPatient();
        MedicalRecord record = new MedicalRecord();
        record.setAppointment(appointment);
        record.setPatient(patient);
        record.setDiagnosis(request.getDiagnosis().trim());
        record.setObservations(request.getObservations());
        record.setMedication(request.getMedication());
        record.setCreatedAt(LocalDateTime.now());

        MedicalRecord saved = medicalRecordRepository.save(record);
        appointment.setMedicalRecord(saved);

        return saved;
    }

    public List<MedicalRecord> getPatientRecords(Long patientUserId) {
        Patient patient = patientRepository.findByUserId(patientUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));
        return medicalRecordRepository.findByPatient(patient);
    }

    public MedicalRecord getRecordForDoctor(Long doctorUserId, Long recordId) {
        MedicalRecord record = medicalRecordRepository.findById(recordId)
                .orElseThrow(() -> new ResourceNotFoundException("Medical record not found"));
        Doctor doctor = record.getAppointment().getDoctor();
        if (doctor.getUser() == null || !doctor.getUser().getId().equals(doctorUserId)) {
            throw new UnauthorizedActionException("You are not allowed to view this medical record");
        }
        return record;
    }
}
