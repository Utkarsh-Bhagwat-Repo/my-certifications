package com.healthtrack360.controller;

import com.healthtrack360.domain.Appointment;
import com.healthtrack360.dto.AppointmentRequest;
import com.healthtrack360.service.AppointmentService;
import com.healthtrack360.service.CurrentUserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final CurrentUserService currentUserService;

    public AppointmentController(AppointmentService appointmentService,
                                 CurrentUserService currentUserService) {
        this.appointmentService = appointmentService;
        this.currentUserService = currentUserService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_PATIENT')")
    public ResponseEntity<Appointment> book(
            @AuthenticationPrincipal UserDetails user,
            @RequestBody AppointmentRequest request) {
        Long userId = currentUserService.getUserId(user);
        Appointment created = appointmentService.bookAppointment(userId, request);
        return ResponseEntity.ok(created);
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('ROLE_PATIENT')")
    public ResponseEntity<List<Appointment>> myAppointments(@AuthenticationPrincipal UserDetails user) {
        Long userId = currentUserService.getUserId(user);
        return ResponseEntity.ok(appointmentService.getPatientAppointments(userId));
    }

    @GetMapping("/doctor/today")
    @PreAuthorize("hasRole('ROLE_DOCTOR')")
    public ResponseEntity<List<Appointment>> doctorToday(@AuthenticationPrincipal UserDetails user) {
        Long userId = currentUserService.getUserId(user);
        return ResponseEntity.ok(appointmentService.getDoctorTodayAppointments(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Appointment> byId(@PathVariable Long id) {
        return ResponseEntity.ok(appointmentService.getById(id));
    }
}
