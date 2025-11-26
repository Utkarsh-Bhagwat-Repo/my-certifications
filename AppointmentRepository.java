package com.healthtrack360.repository;

import com.healthtrack360.domain.Appointment;
import com.healthtrack360.domain.Doctor;
import com.healthtrack360.domain.Patient;
import com.healthtrack360.domain.enums.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    boolean existsByDoctorAndAppointmentTime(Doctor doctor, LocalDateTime appointmentTime);

    List<Appointment> findByDoctorAndAppointmentTimeBetween(Doctor doctor, LocalDateTime start, LocalDateTime end);

    List<Appointment> findByDoctorAndAppointmentTimeBetweenAndStatusIn(
            Doctor doctor, LocalDateTime start, LocalDateTime end, List<AppointmentStatus> statuses);

    List<Appointment> findByDoctorAndAppointmentTimeBetweenOrderByAppointmentTimeAsc(
            Doctor doctor, LocalDateTime start, LocalDateTime end);

    List<Appointment> findByPatientOrderByAppointmentTimeDesc(Patient patient);
}
