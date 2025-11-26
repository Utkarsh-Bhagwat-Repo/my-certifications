package com.healthtrack360.service;

import com.healthtrack360.domain.Patient;
import com.healthtrack360.domain.User;
import com.healthtrack360.domain.enums.Gender;
import com.healthtrack360.domain.enums.RoleName;
import com.healthtrack360.dto.PatientRegistrationRequest;
import com.healthtrack360.dto.PatientUpdateRequest;
import com.healthtrack360.messaging.NotificationProducer;
import com.healthtrack360.exception.BusinessValidationException;
import com.healthtrack360.exception.ResourceNotFoundException;
import com.healthtrack360.exception.ConflictException;

import com.healthtrack360.email.EmailEventProducer;
import com.healthtrack360.email.EmailMessage;
import com.healthtrack360.email.EmailTemplateService;
import java.util.Collections;
import com.healthtrack360.repository.PatientRepository;
import com.healthtrack360.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class PatientService {

    private final PatientRepository patientRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final NotificationProducer notificationProducer;
    private final EmailEventProducer emailEventProducer;
    private final EmailTemplateService emailTemplateService;

    public PatientService(PatientRepository patientRepository,
                          UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          NotificationProducer notificationProducer,
                          EmailEventProducer emailEventProducer,
                          EmailTemplateService emailTemplateService) {
        this.patientRepository = patientRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.notificationProducer = notificationProducer;
        this.emailEventProducer = emailEventProducer;
        this.emailTemplateService = emailTemplateService;
    }

    @Transactional
    public Patient register(PatientRegistrationRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email already registered");
        }
        if (patientRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new ConflictException("Phone already registered");
        }
        LocalDate today = LocalDate.now();
        if (request.getDateOfBirth() == null || !request.getDateOfBirth().isBefore(today)) {
            throw new BusinessValidationException("Date of birth must be in the past");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEnabled(true);
        user.getRoles().add(RoleName.ROLE_PATIENT);
        user = userRepository.save(user);

        String patientCode = generatePatientCode();

        Patient patient = new Patient();
        patient.setPatientCode(patientCode);
        patient.setGivenName(request.getGivenName());
        patient.setFamilyName(request.getFamilyName());
        patient.setGender(request.getGender() == null ? Gender.OTHER : request.getGender());
        patient.setDateOfBirth(request.getDateOfBirth());
        patient.setAddressLine1(request.getAddressLine1());
        patient.setAddressLine2(request.getAddressLine2());
        patient.setCountry(request.getCountry());
        patient.setState(request.getState());
        patient.setCity(request.getCity());
        patient.setZip(request.getZip());
        patient.setPhoneNumber(request.getPhoneNumber());
        patient.setUser(user);

        Patient saved = patientRepository.save(patient);

        notificationProducer.sendNotification("PATIENT_REGISTERED:" + saved.getPatientCode() + ":" + saved.getUser().getEmail());

        String subject = emailTemplateService.buildPatientRegistrationSubject(saved);
        String body = emailTemplateService.buildPatientRegistrationBody(saved);
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
    public Patient updateDemographics(Long userId, PatientUpdateRequest request) {
        Patient patient = patientRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));
        if (request.getPhoneNumber() != null &&
                !request.getPhoneNumber().equals(patient.getPhoneNumber()) &&
                patientRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new ConflictException("Phone already used by another patient");
        }
        if (request.getAddressLine1() != null) patient.setAddressLine1(request.getAddressLine1());
        if (request.getAddressLine2() != null) patient.setAddressLine2(request.getAddressLine2());
        if (request.getCountry() != null) patient.setCountry(request.getCountry());
        if (request.getState() != null) patient.setState(request.getState());
        if (request.getCity() != null) patient.setCity(request.getCity());
        if (request.getZip() != null) patient.setZip(request.getZip());
        if (request.getPhoneNumber() != null) patient.setPhoneNumber(request.getPhoneNumber());
        return patientRepository.save(patient);
    }

    public List<Patient> findAll() {
        return patientRepository.findAll();
    }

    public Patient findById(Long id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));
    }

    @Transactional
    public void deleteById(Long id) {
        patientRepository.deleteById(id);
    }

    private String generatePatientCode() {
        String year = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy"));
        String randomSuffix = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "PT-" + year + "-" + randomSuffix;
    }
}
