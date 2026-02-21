package com.kkvat.automation.service;

import com.kkvat.automation.model.EntityManagement;
import com.kkvat.automation.repository.EntityManagementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Optional;

@Service
public class EntityGeneratorService {

    private final EntityManagementRepository repository;
    private final EntityManagementService emService;

    public EntityGeneratorService(EntityManagementRepository repository, EntityManagementService emService) {
        this.repository = repository;
        this.emService = emService;
    }

    @Transactional
    public String generate(Long id) throws IOException {
        Optional<EntityManagement> opt = repository.findById(id);
        if (opt.isEmpty()) throw new RuntimeException("EntityManagement not found");
        EntityManagement em = opt.get();

        // Create a target folder under backend/generated/<table>
        Path projectRoot = Path.of(System.getProperty("user.dir"));
        Path target = projectRoot.resolve("generated").resolve(em.getEntityTableName() == null ? em.getEntityName() : em.getEntityTableName());
        Files.createDirectories(target);

        // 1) Create DDL SQL file
        String ddl = buildDDL(em);
        Path ddlFile = target.resolve("create_" + (em.getEntityTableName() != null ? em.getEntityTableName() : em.getEntityName()) + ".sql");
        Files.writeString(ddlFile, ddl, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        // 2) Create Java Entity skeleton
        String entityJava = buildEntityJava(em);
        Path javaDir = projectRoot.resolve("backend").resolve("src").resolve("main").resolve("java").resolve("com").resolve("kkvat").resolve("automation").resolve("generated");
        Files.createDirectories(javaDir);
        Path entityFile = javaDir.resolve(capitalize(em.getEntityName()) + ".java");
        Files.writeString(entityFile, entityJava, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        // 3) Create Repository skeleton
        String repoJava = buildRepositoryJava(em);
        Path repoFile = javaDir.resolve(capitalize(em.getEntityName()) + "Repository.java");
        Files.writeString(repoFile, repoJava, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        // 4) Create frontend Angular component skeleton
        buildFrontendComponent(em, projectRoot);

        // 5) Create triggers and stored procedure SQL
        String triggers = buildTriggersSQL(em);
        Path triggersFile = target.resolve("triggers_" + tableNameSafe(em) + ".sql");
        Files.writeString(triggersFile, triggers, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        String sprocs = buildStoredProceduresSQL(em);
        Path sprocsFile = target.resolve("sprocs_" + tableNameSafe(em) + ".sql");
        Files.writeString(sprocsFile, sprocs, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        // 6) Create rollback SQL
        String rollback = buildRollbackSQL(em);
        Path rollbackFile = target.resolve("rollback_" + tableNameSafe(em) + ".sql");
        Files.writeString(rollbackFile, rollback, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        // 7) Create manifest listing generated files for potential rollback
        String manifest = "DDL:" + ddlFile.toAbsolutePath().toString() + "\n" +
            "ENTITY_JAVA:" + entityFile.toAbsolutePath().toString() + "\n" +
            "REPO_JAVA:" + repoFile.toAbsolutePath().toString() + "\n" +
            "FRONTEND_DIR:" + projectRoot.resolve("frontend/kkvat-frontend/src/app/generated/" + safeName(em)).toAbsolutePath().toString() + "\n" +
            "TRIGGERS:" + triggersFile.toAbsolutePath().toString() + "\n" +
            "SPROCS:" + sprocsFile.toAbsolutePath().toString() + "\n" +
            "ROLLBACK:" + rollbackFile.toAbsolutePath().toString();
        Path manifestFile = target.resolve("manifest_" + tableNameSafe(em) + ".txt");
        Files.writeString(manifestFile, manifest, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        // Simple audit log (append to generated/audit.log)
        Path auditLog = projectRoot.resolve("generated").resolve("audit.log");
        String auditEntry = java.time.LocalDateTime.now().toString() + " - Generated entity: " + safeName(em) + " by generator\n";
        Files.writeString(auditLog, auditEntry, StandardOpenOption.CREATE, StandardOpenOption.APPEND);

        // Update status
        emService.setStatus(id, "GENERATED");

        return "Generated at: " + target.toAbsolutePath().toString();
    }

    private String buildDDL(EntityManagement em) {
        String table = em.getEntityTableName() != null ? em.getEntityTableName() : em.getEntityName();
        if (table == null) table = "generated_table";
        StringBuilder sb = new StringBuilder();
        sb.append("-- DDL generated by EntityGeneratorService\n");
        sb.append("CREATE TABLE IF NOT EXISTS ").append(table).append(" (\n");
        sb.append("  id BIGINT AUTO_INCREMENT PRIMARY KEY,\n");
        sb.append("  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,\n");
        sb.append("  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,\n");
        sb.append("  name VARCHAR(255)\n");
        sb.append(") ENGINE=InnoDB;\n");
        return sb.toString();
    }

    private String buildEntityJava(EntityManagement em) {
        String className = capitalize(em.getEntityName() == null ? (em.getEntityTableName() == null ? "Generated" : em.getEntityTableName()) : em.getEntityName());
        String table = em.getEntityTableName() != null ? em.getEntityTableName() : em.getEntityName();
        if (table == null) table = "generated_table";
        return "package com.kkvat.automation.generated;\n\n" +
                "import jakarta.persistence.*;\n" +
                "import lombok.*;\n\n" +
                "@Entity\n@Table(name = \"" + table + "\")\n@Data\n@NoArgsConstructor\n@AllArgsConstructor\npublic class " + className + " {\n\n" +
                "    @Id\n    @GeneratedValue(strategy = GenerationType.IDENTITY)\n    private Long id;\n\n    private String name;\n\n}";
    }

    private String buildRepositoryJava(EntityManagement em) {
        String className = capitalize(em.getEntityName() == null ? (em.getEntityTableName() == null ? "Generated" : em.getEntityTableName()) : em.getEntityName());
        return "package com.kkvat.automation.generated;\n\n" +
                "import org.springframework.data.jpa.repository.JpaRepository;\n" +
                "import org.springframework.stereotype.Repository;\n\n" +
                "@Repository\npublic interface " + className + "Repository extends JpaRepository<" + className + ", Long> {}\n";
    }

    private void buildFrontendComponent(EntityManagement em, Path projectRoot) throws IOException {
        String name = safeName(em);
        Path frontendDir = projectRoot.resolve("frontend/kkvat-frontend/src/app/generated/");
        Files.createDirectories(frontendDir);
        String compTs = "import { Component, OnInit } from '@angular/core';\n" +
                "@Component({ selector: 'app-" + name + "', templateUrl: './" + name + ".component.html', styleUrls: ['./" + name + ".component.css'] })\n" +
                "export class " + capitalize(name) + "Component implements OnInit {\n  items: any[] = [];\n  constructor() {}\n  ngOnInit(): void {}\n}\n";
        String compHtml = "<div>Generated component for " + name + "</div>\n";
        String compCss = "/* Generated component styles */\n";
        Files.writeString(frontendDir.resolve(name + ".component.ts"), compTs, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        Files.writeString(frontendDir.resolve(name + ".component.html"), compHtml, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        Files.writeString(frontendDir.resolve(name + ".component.css"), compCss, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    private String buildTriggersSQL(EntityManagement em) {
        String table = em.getEntityTableName() != null ? em.getEntityTableName() : em.getEntityName();
        if (table == null) table = "generated_table";
        StringBuilder sb = new StringBuilder();
        sb.append("-- Trigger: update updated_at on ").append(table).append("\n");
        sb.append("DELIMITER $$\n");
        sb.append("CREATE TRIGGER trg_").append(table).append("_upd BEFORE UPDATE ON ").append(table).append(" FOR EACH ROW BEGIN\n");
        sb.append("  SET NEW.updated_at = CURRENT_TIMESTAMP;\n");
        sb.append("END$$\n");
        sb.append("DELIMITER ;\n");
        return sb.toString();
    }

    private String buildStoredProceduresSQL(EntityManagement em) {
        String table = em.getEntityTableName() != null ? em.getEntityTableName() : em.getEntityName();
        if (table == null) table = "generated_table";
        StringBuilder sb = new StringBuilder();
        sb.append("-- Stored procedure: insert into ").append(table).append("\n");
        sb.append("DELIMITER $$\n");
        sb.append("CREATE PROCEDURE sp_insert_" + table + "(IN p_name VARCHAR(255))\nBEGIN\n");
        sb.append("  INSERT INTO ").append(table).append("(name) VALUES (p_name);\nEND$$\nDELIMITER ;\n");
        return sb.toString();
    }

    private String buildRollbackSQL(EntityManagement em) {
        String table = em.getEntityTableName() != null ? em.getEntityTableName() : em.getEntityName();
        if (table == null) table = "generated_table";
        StringBuilder sb = new StringBuilder();
        sb.append("-- Rollback: drop generated table and stored procs for ").append(table).append("\n");
        sb.append("DROP PROCEDURE IF EXISTS sp_insert_").append(table).append(";\n");
        sb.append("DROP TRIGGER IF EXISTS trg_").append(table).append("_upd;\n");
        sb.append("DROP TABLE IF EXISTS ").append(table).append(";\n");
        return sb.toString();
    }

    private String safeName(EntityManagement em) {
        String s = em.getEntityName() != null ? em.getEntityName() : em.getEntityTableName();
        if (s == null) return "generated";
        return s.replaceAll("[^A-Za-z0-9]", "-").toLowerCase();
    }

    private String tableNameSafe(EntityManagement em) {
        String s = em.getEntityTableName() != null ? em.getEntityTableName() : em.getEntityName();
        if (s == null) return "generated_table";
        return s.replaceAll("[^A-Za-z0-9]", "_");
    }

    private String capitalize(String s) {
        if (s == null || s.isBlank()) return "Generated";
        String cleaned = s.replaceAll("[^A-Za-z0-9]", "_");
        return Character.toUpperCase(cleaned.charAt(0)) + cleaned.substring(1);
    }
}
