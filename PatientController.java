package com.healthtrack360.controller;

import com.healthtrack360.domain.Patient;
import com.healthtrack360.dto.PatientRegistrationRequest;
import com.healthtrack360.dto.PatientUpdateRequest;
import com.healthtrack360.service.CurrentUserService;
import com.healthtrack360.service.PatientService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patients")
public class PatientController {

    private final PatientService patientService;
    private final CurrentUserService currentUserService;

    public PatientController(PatientService patientService,
                             CurrentUserService currentUserService) {
        this.patientService = patientService;
        this.currentUserService = currentUserService;
    }

    @PostMapping("/register")
    public ResponseEntity<Patient> register(@RequestBody PatientRegistrationRequest request) {
        Patient created = patientService.register(request);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/me")
    @PreAuthorize("hasRole('ROLE_PATIENT')")
    public ResponseEntity<Patient> update(
            @AuthenticationPrincipal UserDetails user,
            @RequestBody PatientUpdateRequest request) {
        Long userId = currentUserService.getUserId(user);
        Patient updated = patientService.updateDemographics(userId, request);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('ROLE_PATIENT')")
    public ResponseEntity<Patient> me(@AuthenticationPrincipal UserDetails user) {
        Long userId = currentUserService.getUserId(user);
        Patient patient = patientService.findAll().stream()
                .filter(p -> p.getUser() != null && p.getUser().getId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Patient not found for user"));
        return ResponseEntity.ok(patient);
    }

    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<List<Patient>> all() {
        return ResponseEntity.ok(patientService.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Patient> byId(@PathVariable Long id) {
        return ResponseEntity.ok(patientService.findById(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        patientService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
