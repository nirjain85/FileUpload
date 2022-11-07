package com.demo.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;

import com.amazonaws.util.IOUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;


@Service
@Slf4j
public class ObjectStorageService {


    @Autowired
    AmazonS3 amazonS3Client;

    public void putObject(String bucketName, MultipartFile multipartFile, boolean publicObject) throws IOException {

        String objectName = System.currentTimeMillis() + "-" + multipartFile.getOriginalFilename();
        File file = getFile(multipartFile);

        try {
            var putObjectRequest = new PutObjectRequest(bucketName, objectName, file).withCannedAcl(CannedAccessControlList.PublicRead);
            PutObjectResult putObjectResult = amazonS3Client.putObject(putObjectRequest);
            log.info(putObjectResult.getETag());
            file.delete();
        } catch (Exception e) {
            log.error("Error while sending file to S3 bucket" + e.getMessage());
        }

    }

    public byte[] downloadObject(String bucketName, String objectName) {

        try {
            S3Object s3object = amazonS3Client.getObject(bucketName, objectName);
            S3ObjectInputStream inputStream = s3object.getObjectContent();
            byte[] content = IOUtils.toByteArray(inputStream);
            return content;

        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return null;
    }

    public List<S3ObjectSummary> listObjects(String bucketName) {
        ObjectListing objectListing = amazonS3Client.listObjects(bucketName);
        return objectListing.getObjectSummaries();
    }

    private File getFile(MultipartFile multipartFile) {
        File file = new File(multipartFile.getOriginalFilename());
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(multipartFile.getBytes());
        } catch (Exception exception) {
            log.error("Error In file conversion");

        }
        return file;
    }
}
