package com.example.bankcards.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/docs")
public class OpenApiController {

    @Value("${springdoc.external-docs.path:docs/openapi.yaml}")
    private String openApiPath;

    @GetMapping(value = "/openapi.yaml", produces = "application/yaml")
    public ResponseEntity<byte[]> getOpenApiYaml() throws IOException {
        Path filePath = Paths.get(openApiPath);

        if (!Files.exists(filePath)) {
            filePath = Paths.get("docs/openapi.yaml");
        }

        if (!Files.exists(filePath)) {
            return ResponseEntity.notFound().build();
        }

        byte[] content = Files.readAllBytes(filePath);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "application/yaml")
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"openapi.yaml\"")
                .body(content);
    }

    @GetMapping(value = "/openapi.json", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Resource> getOpenApiJson() throws IOException {
        Path filePath = Paths.get("docs/openapi.yaml");

        if (!Files.exists(filePath)) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new FileSystemResource(filePath);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(resource);
    }
}