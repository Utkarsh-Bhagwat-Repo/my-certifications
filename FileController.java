package com.healthtrack360.controller;

import com.healthtrack360.domain.FileResource;
import com.healthtrack360.service.CurrentUserService;
import com.healthtrack360.service.FileService;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final FileService fileService;
    private final CurrentUserService currentUserService;

    public FileController(FileService fileService,
                          CurrentUserService currentUserService) {
        this.fileService = fileService;
        this.currentUserService = currentUserService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ROLE_PATIENT')")
    public ResponseEntity<Long> upload(
            @AuthenticationPrincipal UserDetails user,
            @RequestParam("file") MultipartFile file) throws Exception {
        Long userId = currentUserService.getUserId(user);
        FileResource resource = fileService.uploadPatientFile(userId, file);
        return ResponseEntity.ok(resource.getId());
    }

    @GetMapping("/{id}")
    public ResponseEntity<byte[]> get(@PathVariable Long id) {
        FileResource resource = fileService.getFile(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(resource.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource.getData());
    }

}
