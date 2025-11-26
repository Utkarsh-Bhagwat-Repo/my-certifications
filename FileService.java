package com.healthtrack360.service;

import com.healthtrack360.domain.FileResource;
import com.healthtrack360.domain.Patient;
import com.healthtrack360.repository.FileResourceRepository;
import com.healthtrack360.repository.PatientRepository;
import com.healthtrack360.exception.BusinessValidationException;
import com.healthtrack360.exception.ResourceNotFoundException;
import com.healthtrack360.exception.FileStorageException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class FileService {

    private final FileResourceRepository fileResourceRepository;
    private final PatientRepository patientRepository;

    public FileService(FileResourceRepository fileResourceRepository,
                       PatientRepository patientRepository) {
        this.fileResourceRepository = fileResourceRepository;
        this.patientRepository = patientRepository;
    }

    @Transactional
    public FileResource uploadPatientFile(Long patientUserId, MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new BusinessValidationException("File cannot be empty");
        }
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new BusinessValidationException("File too large, maximum allowed size is 5 MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || (!contentType.startsWith("image/") && !contentType.equals("application/pdf"))) {
            throw new BusinessValidationException("Only image or PDF files are allowed");
        }

        Patient patient = patientRepository.findByUserId(patientUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));

        FileResource resource = new FileResource();
        resource.setFilename(file.getOriginalFilename());
        resource.setContentType(contentType);
        resource.setSize(file.getSize());
        resource.setData(file.getBytes());
        resource.setPatient(patient);

        return fileResourceRepository.save(resource);
    }

    public FileResource getFile(Long id) {
        return fileResourceRepository.findById(id)
                .orElseThrow(() -> new FileStorageException("File not found"));
    }
}
