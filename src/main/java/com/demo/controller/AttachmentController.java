package com.demo.controller;


import com.demo.service.ObjectStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class AttachmentController {

    private static final String bucketName = "nirjain-attachment-bucket";

    @Autowired
    ObjectStorageService objectStorageService;

    @PostMapping(value = "/upload")
    public ResponseEntity uploadFile(MultipartFile file) {
        try {
            objectStorageService.putObject(bucketName, file, true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new ResponseEntity(HttpStatus.ACCEPTED);
    }

    @GetMapping(value = "/attachment-list")
    public List<String> listAttachments() {
        var s3ObjectSummaryList = objectStorageService.listObjects(bucketName);
        var attachmentList = s3ObjectSummaryList.stream().map(line -> line.getKey()).collect(Collectors.toList());
        return attachmentList;
    }

    @GetMapping(value = "/file/{fileName}")
    public ResponseEntity<ByteArrayResource> downloadObject(@PathVariable String fileName) {
        byte[] fileStream = objectStorageService.downloadObject(bucketName, fileName);
        ByteArrayResource byteArrayResource = new ByteArrayResource(fileStream);
        return ResponseEntity.ok()
                .contentLength(fileStream.length)
                .header("content-type", "application/octet-stream")
                .header("content-disposition", "attachment; fileName=\"" + fileName + "\"")
                .body(byteArrayResource);

    }

    @GetMapping(value = "/ping")
    public ResponseEntity ping() {
        return new ResponseEntity(HttpStatus.OK);
    }
}
