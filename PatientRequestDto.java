package com.healthtrack360.dto;

public class PatientRequestDto {

    private String text;
    private Long doctorId;

    public PatientRequestDto() {
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Long getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Long doctorId) {
        this.doctorId = doctorId;
    }
}
