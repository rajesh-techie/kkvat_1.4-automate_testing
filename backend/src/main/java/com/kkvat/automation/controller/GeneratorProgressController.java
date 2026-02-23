package com.kkvat.automation.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Files;
import java.nio.file.Path;

@RestController
@RequestMapping("/api/generator")
public class GeneratorProgressController {

    @GetMapping("/progress/{name}")
    public ResponseEntity<?> getProgress(@PathVariable String name) {
        try {
            Path projectRoot = Path.of(System.getProperty("user.dir"));
            Path progress = projectRoot.resolve("generated").resolve(name).resolve("progress.json");
            if (!Files.exists(progress)) return ResponseEntity.ok(java.util.List.of());
            String json = Files.readString(progress);
            com.fasterxml.jackson.databind.ObjectMapper om = new com.fasterxml.jackson.databind.ObjectMapper();
            Object obj = om.readValue(json, Object.class);
            return ResponseEntity.ok(obj);
        } catch (Exception ex) {
            return ResponseEntity.status(500).body(java.util.Map.of("error", ex.getMessage()));
        }
    }
}
