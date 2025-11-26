package com.healthtrack360.email;

import com.healthtrack360.domain.Doctor;
import com.healthtrack360.domain.Patient;
import org.springframework.stereotype.Service;

@Service
public class EmailTemplateService {

    public String buildPatientRegistrationSubject(Patient patient) {
        return "Welcome to HealthTrack 360, " + patient.getGivenName();
    }

    public String buildPatientRegistrationBody(Patient patient) {
        StringBuilder sb = new StringBuilder();
        sb.append("Dear ").append(patient.getGivenName()).append(" ").append(patient.getFamilyName()).append(",\n\n");
        sb.append("Your patient profile has been successfully registered in HealthTrack 360.\n\n");
        sb.append("Patient Code: ").append(patient.getPatientCode()).append("\n");
        sb.append("Phone: ").append(patient.getPhoneNumber()).append("\n");
        sb.append("City: ").append(patient.getCity()).append(", ").append(patient.getState()).append("\n\n");
        sb.append("You can now log in, manage your appointments, and view your medical records.\n\n");
        sb.append("Regards,\n");
        sb.append("HealthTrack 360 Team");
        return sb.toString();
    }

    public String buildDoctorRegistrationSubject(Doctor doctor) {
        return "Doctor Onboarding - HealthTrack 360";
    }

    public String buildDoctorRegistrationBody(Doctor doctor) {
        StringBuilder sb = new StringBuilder();
        sb.append("Dear ").append(doctor.getFullName()).append(",\n\n");
        sb.append("Your doctor account has been created and approved in HealthTrack 360.\n\n");
        sb.append("Specialization: ").append(doctor.getSpecialization()).append("\n");
        sb.append("Enabled: ").append(doctor.isEnabled() ? "Yes" : "No").append("\n\n");
        sb.append("You can now log in and view your dashboard, today's appointments, and patient requests.\n\n");
        sb.append("Regards,\n");
        sb.append("HealthTrack 360 Admin");
        return sb.toString();
    }
}
