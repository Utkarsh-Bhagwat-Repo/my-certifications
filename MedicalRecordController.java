package com.healthtrack360.controller;

import com.healthtrack360.domain.MedicalRecord;
import com.healthtrack360.dto.MedicalRecordRequest;
import com.healthtrack360.service.CurrentUserService;
import com.healthtrack360.service.MedicalRecordService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/records")
public class MedicalRecordController {

    private final MedicalRecordService medicalRecordService;
    private final CurrentUserService currentUserService;

    public MedicalRecordController(MedicalRecordService medicalRecordService,
                                   CurrentUserService currentUserService) {
        this.medicalRecordService = medicalRecordService;
        this.currentUserService = currentUserService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_DOCTOR')")
    public ResponseEntity<MedicalRecord> create(
            @AuthenticationPrincipal UserDetails user,
            @RequestBody MedicalRecordRequest request) {
        Long userId = currentUserService.getUserId(user);
        return ResponseEntity.ok(medicalRecordService.createRecord(userId, request));
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('ROLE_PATIENT')")
    public ResponseEntity<List<MedicalRecord>> myRecords(@AuthenticationPrincipal UserDetails user) {
        Long userId = currentUserService.getUserId(user);
        return ResponseEntity.ok(medicalRecordService.getPatientRecords(userId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_DOCTOR')")
    public ResponseEntity<MedicalRecord> byId(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable Long id) {
        Long userId = currentUserService.getUserId(user);
        return ResponseEntity.ok(medicalRecordService.getRecordForDoctor(userId, id));
    }
}
