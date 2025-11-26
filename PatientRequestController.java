package com.healthtrack360.controller;

import com.healthtrack360.domain.PatientRequest;
import com.healthtrack360.dto.PatientRequestDto;
import com.healthtrack360.service.CurrentUserService;
import com.healthtrack360.service.PatientRequestService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/requests")
public class PatientRequestController {

    private final PatientRequestService patientRequestService;
    private final CurrentUserService currentUserService;

    public PatientRequestController(PatientRequestService patientRequestService,
                                    CurrentUserService currentUserService) {
        this.patientRequestService = patientRequestService;
        this.currentUserService = currentUserService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_PATIENT')")
    public ResponseEntity<PatientRequest> submit(
            @AuthenticationPrincipal UserDetails user,
            @RequestBody PatientRequestDto dto) {
        Long userId = currentUserService.getUserId(user);
        return ResponseEntity.ok(patientRequestService.submitRequest(userId, dto));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('ROLE_DOCTOR')")
    public ResponseEntity<PatientRequest> approve(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable Long id,
            @RequestParam(value = "instruction", required = false) String instruction) {
        Long userId = currentUserService.getUserId(user);
        return ResponseEntity.ok(patientRequestService.decideRequest(userId, id, true, instruction));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('ROLE_DOCTOR')")
    public ResponseEntity<PatientRequest> reject(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable Long id,
            @RequestParam(value = "instruction", required = false) String instruction) {
        Long userId = currentUserService.getUserId(user);
        return ResponseEntity.ok(patientRequestService.decideRequest(userId, id, false, instruction));
    }

    @GetMapping("/doctor/pending")
    @PreAuthorize("hasRole('ROLE_DOCTOR')")
    public ResponseEntity<List<PatientRequest>> doctorPending(
            @AuthenticationPrincipal UserDetails user) {
        Long userId = currentUserService.getUserId(user);
        return ResponseEntity.ok(patientRequestService.getPendingForDoctor(userId));
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('ROLE_PATIENT')")
    public ResponseEntity<List<PatientRequest>> myRequests(@AuthenticationPrincipal UserDetails user) {
        Long userId = currentUserService.getUserId(user);
        return ResponseEntity.ok(patientRequestService.getHistoryForPatient(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PatientRequest> byId(@PathVariable Long id) {
        return ResponseEntity.ok(patientRequestService.getById(id));
    }
}
