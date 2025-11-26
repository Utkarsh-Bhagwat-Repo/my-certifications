package com.healthtrack360.service;

import com.healthtrack360.domain.*;
import com.healthtrack360.domain.enums.AppointmentStatus;
import com.healthtrack360.dto.AppointmentRequest;
import com.healthtrack360.messaging.NotificationProducer;
import com.healthtrack360.exception.BusinessValidationException;
import com.healthtrack360.exception.ResourceNotFoundException;

import com.healthtrack360.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.List;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final DoctorAvailabilityRepository availabilityRepository;
    private final NotificationProducer notificationProducer;

    public AppointmentService(AppointmentRepository appointmentRepository,
                              DoctorRepository doctorRepository,
                              PatientRepository patientRepository,
                              DoctorAvailabilityRepository availabilityRepository,
                              NotificationProducer notificationProducer) {
        this.appointmentRepository = appointmentRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.availabilityRepository = availabilityRepository;
        this.notificationProducer = notificationProducer;
    }

    @Transactional
    public Appointment bookAppointment(Long patientUserId, AppointmentRequest request) {
        Patient patient = patientRepository.findByUserId(patientUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));
        Doctor doctor = doctorRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));
        if (!doctor.isEnabled()) {
            throw new BusinessValidationException("Doctor is not accepting appointments");
        }

        LocalDateTime slot = request.getAppointmentTime();
        if (slot.isBefore(LocalDateTime.now())) {
            throw new BusinessValidationException("Cannot book appointment in the past");
        }

        if (!isWithinDoctorAvailability(doctor, slot)) {
            throw new BusinessValidationException("Selected time is outside doctor's working hours");
        }

        if (appointmentRepository.existsByDoctorAndAppointmentTime(doctor, slot)) {
            throw new BusinessValidationException("Selected time slot is already booked");
        }

        Appointment appointment = new Appointment();
        appointment.setAppointmentTime(slot);
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        appointment.setDoctor(doctor);
        appointment.setPatient(patient);

        Appointment saved = appointmentRepository.save(appointment);

        String message = "APPOINTMENT_BOOKED:" + saved.getId() +
                ":DOCTOR_EMAIL:" + doctor.getUser().getEmail() +
                ":PATIENT_EMAIL:" + patient.getUser().getEmail();
        notificationProducer.sendNotification(message);

        return saved;
    }

    private boolean isWithinDoctorAvailability(Doctor doctor, LocalDateTime slot) {
        DayOfWeek day = slot.getDayOfWeek();
        LocalTime time = slot.toLocalTime();
        List<DoctorAvailability> rules = availabilityRepository.findByDoctorAndDayOfWeek(doctor, day);
        if (rules.isEmpty()) {
            return false;
        }
        return rules.stream().anyMatch(rule ->
                !time.isBefore(rule.getStartTime()) && !time.isAfter(rule.getEndTime()));
    }

    public List<Appointment> getPatientAppointments(Long patientUserId) {
        Patient patient = patientRepository.findByUserId(patientUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));
        return appointmentRepository.findByPatientOrderByAppointmentTimeDesc(patient);
    }

    public List<Appointment> getDoctorTodayAppointments(Long doctorUserId) {
        Doctor doctor = doctorRepository.findByUserId(doctorUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = start.plusDays(1);
        return appointmentRepository.findByDoctorAndAppointmentTimeBetweenOrderByAppointmentTimeAsc(doctor, start, end);
    }

    public Appointment getById(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));
    }
}
