package com.healthtrack360.service;

import com.healthtrack360.domain.Doctor;
import com.healthtrack360.domain.User;
import com.healthtrack360.domain.enums.RoleName;
import com.healthtrack360.dto.DoctorCreateRequest;
import com.healthtrack360.dto.DoctorUpdateRequest;
import com.healthtrack360.exception.ResourceNotFoundException;
import com.healthtrack360.exception.ConflictException;
import com.healthtrack360.email.EmailEventProducer;
import com.healthtrack360.email.EmailMessage;
import com.healthtrack360.email.EmailTemplateService;
import java.util.Collections;

import com.healthtrack360.email.EmailEventProducer;
import com.healthtrack360.email.EmailMessage;
import com.healthtrack360.email.EmailTemplateService;
import java.util.Collections;
import com.healthtrack360.repository.DoctorRepository;
import com.healthtrack360.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailEventProducer emailEventProducer;
    private final EmailTemplateService emailTemplateService;

    public DoctorService(DoctorRepository doctorRepository,
                         UserRepository userRepository,
                         PasswordEncoder passwordEncoder,
                         EmailEventProducer emailEventProducer,
                         EmailTemplateService emailTemplateService) {
        this.doctorRepository = doctorRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailEventProducer = emailEventProducer;
        this.emailTemplateService = emailTemplateService;
    }

    @Transactional
    public Doctor createDoctor(DoctorCreateRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email already used for another account");
        }
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEnabled(true);
        user.getRoles().add(RoleName.ROLE_DOCTOR);
        user = userRepository.save(user);

        Doctor doctor = new Doctor();
        doctor.setFullName(request.getFullName());
        doctor.setSpecialization(request.getSpecialization());
        doctor.setEnabled(true);
        doctor.setUser(user);

        Doctor saved = doctorRepository.save(doctor);

        String subject = emailTemplateService.buildDoctorRegistrationSubject(saved);
        String body = emailTemplateService.buildDoctorRegistrationBody(saved);
        EmailMessage emailMessage = new EmailMessage(
                saved.getUser().getEmail(),
                Collections.emptyList(),
                subject,
                body
        );
        emailEventProducer.sendEmailEvent(emailMessage);

        return saved;
    }

    public List<Doctor> findAll() {
        return doctorRepository.findAll();
    }

    public Doctor findById(Long id) {
        return doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));
    }

    @Transactional
    public Doctor updateDoctor(Long id, DoctorUpdateRequest request) {
        Doctor doctor = findById(id);
        if (request.getFullName() != null) doctor.setFullName(request.getFullName());
        if (request.getSpecialization() != null) doctor.setSpecialization(request.getSpecialization());
        if (request.getEnabled() != null) doctor.setEnabled(request.getEnabled());
        Doctor saved = doctorRepository.save(doctor);

        String subject = emailTemplateService.buildDoctorRegistrationSubject(saved);
        String body = emailTemplateService.buildDoctorRegistrationBody(saved);
        EmailMessage emailMessage = new EmailMessage(
                saved.getUser().getEmail(),
                Collections.emptyList(),
                subject,
                body
        );
        emailEventProducer.sendEmailEvent(emailMessage);

        return saved;
    }

    @Transactional
    public void deleteDoctor(Long id) {
        doctorRepository.deleteById(id);
    }
}
