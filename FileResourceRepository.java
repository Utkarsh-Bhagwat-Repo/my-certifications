package com.healthtrack360.repository;

import com.healthtrack360.domain.FileResource;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileResourceRepository extends JpaRepository<FileResource, Long> {
}
