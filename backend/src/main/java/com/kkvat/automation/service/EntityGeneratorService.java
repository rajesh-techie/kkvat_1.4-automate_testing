package com.kkvat.automation.service;

import com.kkvat.automation.model.EntityManagement;
import com.kkvat.automation.repository.EntityManagementRepository;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EntityGeneratorService {

    private final EntityManagementRepository repository;
    private final EntityManagementService emService;
    private final DataSource dataSource;

    public EntityGeneratorService(EntityManagementRepository repository, EntityManagementService emService, DataSource dataSource) {
        this.repository = repository;
        this.emService = emService;
        this.dataSource = dataSource;
    }

    /**
     * Parse columns JSON from EntityManagement into a list of maps.
     */
    private java.util.List<java.util.Map<String, Object>> parseColumns(EntityManagement em) {
        java.util.List<java.util.Map<String, Object>> columns = new java.util.ArrayList<>();
        if (em.getColumns() == null || em.getColumns().isBlank()) return columns;
        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        String s = em.getColumns().trim();
        // Try multiple strategies: direct parse, unquote-then-parse, unescape and parse
        for (int attempt = 0; attempt < 5; attempt++) {
            try {
                if (s.startsWith("[") || s.startsWith("{")) {
                    columns = mapper.readValue(s, java.util.List.class);
                    return columns;
                }
                // if s is a JSON string literal like "[...]", read it to unquote
                try {
                    String inner = mapper.readValue(s, String.class);
                    if (inner != null) s = inner.trim();
                } catch (Exception e) {
                    // last resort: remove surrounding quotes if present
                    if (s.startsWith("\"") && s.endsWith("\"")) {
                        s = s.substring(1, s.length() - 1);
                    }
                    // unescape common sequences
                    s = s.replaceAll("\\\\\"", "\"").replaceAll("\\\\\\\\", "\\\\");
                }
            } catch (Exception ex) {
                // continue attempts
            }
        }
        try {
            Path dbg = Path.of(System.getProperty("user.dir")).resolve("generated").resolve("last_columns_debug.txt");
            Files.createDirectories(dbg.getParent());
            Files.writeString(dbg, s == null ? "null" : s, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (Exception ignore) {}
        System.err.println("[GENERATOR] Failed to parse columns JSON after attempts: " + (s == null ? "null" : s.substring(0, Math.min(200, s.length()))));
        return columns;
    }

    /**
     * Run generation steps 1..8 for safe testing.
     */
    @Transactional
    public String generate(Long id) throws IOException {
        EntityManagement em = repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Entity config not found: " + id));
        Path projectRoot = Path.of(System.getProperty("user.dir"));

        // Clear the whole generated folder before creating artifacts for this entity
        clearGeneratedRoot(projectRoot);
        String folderName = "entity_" + tableNameSafe(em).toLowerCase();
        Path target = projectRoot.resolve("generated").resolve(folderName);

        // Clear only this entity folder before generating
        if (Files.exists(target)) {
            try {
                Files.walk(target)
                        .sorted(java.util.Comparator.reverseOrder())
                        .forEach(p -> {
                            try { Files.deleteIfExists(p); } catch (Exception ignored) {}
                        });
            } catch (Exception ignored) {}
        }
        Files.createDirectories(target);
        String table = em.getEntityTableName() != null ? em.getEntityTableName() : em.getEntityName();
        if (table == null) table = tableNameSafe(em);

        // Parse columns JSON early so optional artifacts can use it
        java.util.List<java.util.Map<String, Object>> columns = parseColumns(em);

        Path ddlFile = target.resolve("create_" + tableNameSafe(em) + ".sql");
        System.out.println("[GENERATOR] Creating DDL file: " + ddlFile);
        Files.writeString(ddlFile, buildDDL(em), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        Path triggersFile = target.resolve(table + "_triggers.sql");
        System.out.println("[GENERATOR] Creating triggers SQL: " + triggersFile);
        Files.writeString(triggersFile, buildTriggersSQL(em), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        Path storedProcsFile = target.resolve(table + "_stored_procs.sql");
        System.out.println("[GENERATOR] Creating stored procedures SQL: " + storedProcsFile);
        Files.writeString(storedProcsFile, buildStoredProceduresSQL(em), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        Path rollbackFile = target.resolve(table + "_rollback.sql");
        System.out.println("[GENERATOR] Creating rollback SQL: " + rollbackFile);
        Files.writeString(rollbackFile, buildRollbackSQL(em), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        // Optional additional artifacts based on flags
        try {
            if (Boolean.TRUE.equals(em.getDoWeNeedAuditTable())) {
                Path auditFile = target.resolve(table + "_audit.sql");
                System.out.println("[GENERATOR] Creating audit DDL: " + auditFile);
                Files.writeString(auditFile, buildAuditDDL(em, columns), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            }
            if (Boolean.TRUE.equals(em.getDoWeNeedArchiveRecords())) {
                Path archiveFile = target.resolve(table + "_archive.sql");
                System.out.println("[GENERATOR] Creating archive DDL: " + archiveFile);
                Files.writeString(archiveFile, buildArchiveDDL(em, columns), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            }
            if (Boolean.TRUE.equals(em.getDoWeNeedWorkflow())) {
                Path instanceFile = target.resolve(table + "_instance.sql");
                System.out.println("[GENERATOR] Creating instance DDL: " + instanceFile);
                Files.writeString(instanceFile, buildInstanceDDL(em, columns), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            }
            if (Boolean.TRUE.equals(em.getDoWeNeedCreateView())) {
                Path viewsFile = target.resolve(table + "_create_views.sql");
                System.out.println("[GENERATOR] Creating views SQL: " + viewsFile);
                Files.writeString(viewsFile, buildCreateViewsSQL(em, columns), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            }
            // detailed stored procs / triggers / menu permissions
            Path storedImpl = target.resolve(table + "_stored_procs_impl.sql");
            System.out.println("[GENERATOR] Creating detailed stored procs SQL: " + storedImpl);
            Files.writeString(storedImpl, buildStoredProceduresImplSQL(em, columns), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            Path trigInstToAudit = target.resolve(table + "_create_trigger_instance_to_audit.sql");
            System.out.println("[GENERATOR] Creating trigger instance->audit SQL: " + trigInstToAudit);
            Files.writeString(trigInstToAudit, buildTriggerInstanceToAuditSQL(em, columns), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            Path menuPerm = target.resolve(table + "_menu_permissions.sql");
            System.out.println("[GENERATOR] Creating menu permissions SQL: " + menuPerm);
            Files.writeString(menuPerm, buildMenuPermissionsSQL(em), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (Exception ex) {
            System.err.println("[GENERATOR] Warning: failed to write optional artifacts: " + ex.getMessage());
        }


        // columns already parsed earlier via parseColumns(em)

        // Generate Entity/Repository/Service/Controller Java files named by ClassName
        String className = capitalize(em.getEntityName() == null ? tableNameSafe(em) : em.getEntityName());
        Path entityJavaFile = target.resolve(className + ".java");
        System.out.println("[GENERATOR] Creating Entity Java: " + entityJavaFile);
        Files.writeString(entityJavaFile, buildDynamicEntityJava(em, columns), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        // Generate Repository Java (prefixed with table name)
        Path repoJavaFile = target.resolve(table + "_Repository.java");
        System.out.println("[GENERATOR] Creating Repository Java: " + repoJavaFile);
        Files.writeString(repoJavaFile, buildRepositoryJava(em), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        // Generate Service Java (prefixed with table name)
        Path serviceJavaFile = target.resolve(table + "_Service.java");
        System.out.println("[GENERATOR] Creating Service Java: " + serviceJavaFile);
        Files.writeString(serviceJavaFile, buildServiceJava(em), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        // Generate Controller Java (prefixed with table name)
        Path controllerJavaFile = target.resolve(table + "_Controller.java");
        System.out.println("[GENERATOR] Creating Controller Java: " + controllerJavaFile);
        Files.writeString(controllerJavaFile, buildControllerJava(em), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        // Generate frontend CRUD files (placeholder, will extend)
        System.out.println("[GENERATOR] Creating frontend CRUD files for entity: " + em.getEntityName());
        buildFrontendCrudFiles(em, columns, projectRoot);

        System.out.println("[GENERATOR] Updating progress.json and prefixed progress file");
        updateProgress(target, 8, "COMPLETE", "ok", "Generation complete");

        System.out.println("[GENERATOR] Generation complete for entity: " + em.getEntityName());
        return "Artifacts generated at: " + target.toAbsolutePath().toString();
    }

    /**
     * Build a dynamic Entity Java class based on columns JSON.
     */
    private String buildDynamicEntityJava(EntityManagement em, java.util.List<java.util.Map<String, Object>> columns) {
        String className = capitalize(em.getEntityName() == null ? tableNameSafe(em) : em.getEntityName());
        String table = em.getEntityTableName() != null ? em.getEntityTableName() : em.getEntityName();
        if (table == null) table = tableNameSafe(em);

        StringBuilder sb = new StringBuilder();
        sb.append("package com.kkvat.automation.generated;\n\n");
        sb.append("import jakarta.persistence.*;\n\n");
        sb.append("@Entity\n@Table(name=\"").append(table).append("\")\n");
        sb.append("public class ").append(className).append(" {\n\n");
        // Determine primary/index/unique flags from columns
        java.util.List<String> pkCols = new java.util.ArrayList<>();
        java.util.List<String> idxCols = new java.util.ArrayList<>();
        java.util.List<String> uqCols = new java.util.ArrayList<>();
        for (java.util.Map<String, Object> col : columns) {
            Object primaryFlag = col.getOrDefault("column_primary", col.get("primary"));
            Object indexFlag = col.getOrDefault("column_index", col.get("index"));
            Object uniqueFlag = col.getOrDefault("column_unique", col.get("unique"));
            if (primaryFlag != null && ("1".equals(primaryFlag.toString()) || "true".equalsIgnoreCase(primaryFlag.toString()))) pkCols.add((String) col.getOrDefault("column_name", col.getOrDefault("name", "col")));
            if (indexFlag != null && ("1".equals(indexFlag.toString()) || "true".equalsIgnoreCase(indexFlag.toString()))) idxCols.add((String) col.getOrDefault("column_name", col.getOrDefault("name", "col")));
            if (uniqueFlag != null && ("1".equals(uniqueFlag.toString()) || "true".equalsIgnoreCase(uniqueFlag.toString()))) uqCols.add((String) col.getOrDefault("column_name", col.getOrDefault("name", "col")));
        }

        boolean hasExplicitPk = !pkCols.isEmpty();
        // If no explicit primary specified, keep auto id
        if (!hasExplicitPk) {
            sb.append("    @Id\n    @GeneratedValue(strategy=GenerationType.IDENTITY)\n    private Long id;\n\n");
        }

        for (java.util.Map<String, Object> col : columns) {
            String name = (String) col.getOrDefault("column_name", col.getOrDefault("name", "col"));
            String datatype = col.getOrDefault("column_datatype", col.getOrDefault("type", "string")).toString();
            String javaType = mapColumnType(datatype);
            // If this column is the single primary, annotate with @Id (no GeneratedValue)
            if (hasExplicitPk && pkCols.size() == 1 && pkCols.contains(name)) {
                sb.append("    @Id\n");
            }
            sb.append("    private ").append(javaType).append(" ").append(name).append(";\n");
        }

        sb.append("\n    public ").append(className).append("() {}\n\n");

        sb.append("    public Long getId() { return id; }\n");
        sb.append("    public void setId(Long id) { this.id = id; }\n\n");

        for (java.util.Map<String, Object> col : columns) {
            String name = (String) col.getOrDefault("column_name", col.getOrDefault("name", "col"));
            String datatype = col.getOrDefault("column_datatype", col.getOrDefault("type", "string")).toString();
            String javaType = mapColumnType(datatype);
            String cap = Character.toUpperCase(name.charAt(0)) + name.substring(1);
            sb.append("    public ").append(javaType).append(" get").append(cap).append("() { return ").append(name).append("; }\n");
            sb.append("    public void set").append(cap).append("(").append(javaType).append(" ").append(name).append(") { this.").append(name).append(" = ").append(name).append("; }\n\n");
        }

        sb.append("    @Override\n    public String toString() {\n        return \"").append(className).append("{" );
        sb.append(" + \"id=\" + id");
        for (java.util.Map<String, Object> col : columns) {
            String name = (String) col.getOrDefault("column_name", col.getOrDefault("name", "col"));
            sb.append(" + \" , ").append(name).append("=\" + ").append(name);
        }
        sb.append(" + \"}\";\n    }\n\n");

        sb.append("}\n");
        return sb.toString();
    }

    /**
     * Map column type from JSON to Java type.
     */
    private String mapColumnType(String type) {
        if (type == null) return "String";
        switch (type.toLowerCase()) {
            case "int": case "integer": return "Integer";
            case "long": return "Long";
            case "bool": case "boolean": return "Boolean";
            case "date": return "java.time.LocalDate";
            case "datetime": return "java.time.LocalDateTime";
            case "double": return "Double";
            case "number": return "Double";
            case "float": return "Float";
            default: return "String";
        }
    }

    /**
     * Build a Service Java class for the entity.
     */
    private String buildServiceJava(EntityManagement em) {
        String className = capitalize(em.getEntityName() == null ? tableNameSafe(em) : em.getEntityName());
        // determine id type
        String idType = "Long";
        java.util.List<java.util.Map<String, Object>> columns = parseColumns(em);
        if (columns != null && !columns.isEmpty()) {
            java.util.List<java.util.Map<String, Object>> pks = new java.util.ArrayList<>();
            for (java.util.Map<String, Object> c : columns) {
                Object primaryFlag = c.getOrDefault("column_primary", c.get("primary"));
                if (primaryFlag != null && ("1".equals(primaryFlag.toString()) || "true".equalsIgnoreCase(primaryFlag.toString()))) pks.add(c);
            }
            if (pks.size() == 1) {
                String dt = pks.get(0).getOrDefault("column_datatype", pks.get(0).getOrDefault("type", "string")).toString();
                idType = mapColumnType(dt);
            }
        }
        return "package com.kkvat.automation.generated;\n\n" +
                "import org.springframework.stereotype.Service;\n" +
                "import java.util.List;\n\n" +
                "@Service\n" +
                "public class " + className + "Service {\n" +
                "    // Autowire repository\n" +
                "    private final " + className + "Repository repo;\n\n" +
                "    public " + className + "Service(" + className + "Repository repo) { this.repo = repo; }\n\n" +
                "    public List<" + className + "> findAll() { return repo.findAll(); }\n" +
                "    public " + className + " save(" + className + " e) { return repo.save(e); }\n" +
                "    public void delete(" + idType + " id) { repo.deleteById(id); }\n" +
                "    public " + className + " findById(" + idType + " id) { return repo.findById(id).orElse(null); }\n" +
                "}\n";
    }

    /**
     * Build a Controller Java class for the entity.
     */
    private String buildControllerJava(EntityManagement em) {
        String className = capitalize(em.getEntityName() == null ? tableNameSafe(em) : em.getEntityName());
        String varName = className.substring(0,1).toLowerCase() + className.substring(1);
        String basePath = "/api/generated/" + tableNameSafe(em).toLowerCase();
        // determine id type for path vars
        String idType = "Long";
        java.util.List<java.util.Map<String, Object>> columns = parseColumns(em);
        if (columns != null && !columns.isEmpty()) {
            java.util.List<java.util.Map<String, Object>> pks = new java.util.ArrayList<>();
            for (java.util.Map<String, Object> c : columns) {
                Object primaryFlag = c.getOrDefault("column_primary", c.get("primary"));
                if (primaryFlag != null && ("1".equals(primaryFlag.toString()) || "true".equalsIgnoreCase(primaryFlag.toString()))) pks.add(c);
            }
            if (pks.size() == 1) {
                String dt = pks.get(0).getOrDefault("column_datatype", pks.get(0).getOrDefault("type", "string")).toString();
                idType = mapColumnType(dt);
            }
        }
        return "package com.kkvat.automation.generated;\n\n" +
                "import org.springframework.web.bind.annotation.*;\n" +
                "import java.util.List;\n\n" +
                "@RestController\n@RequestMapping(\"" + basePath + "\")\n" +
                "public class " + className + "Controller {\n" +
                "    private final " + className + "Service service;\n\n" +
                "    public " + className + "Controller(" + className + "Service service) { this.service = service; }\n\n" +
                "    @GetMapping\n" +
                "    public List<" + className + "> getAll() { return service.findAll(); }\n\n" +
                "    @PostMapping\n" +
                "    public " + className + " create(@RequestBody " + className + " e) { return service.save(e); }\n\n" +
                "    @GetMapping(\"/{id}\")\n" +
                "    public " + className + " get(@PathVariable " + idType + " id) { return service.findById(id); }\n\n" +
                "    @PutMapping(\"/{id}\")\n" +
                "    public " + className + " update(@PathVariable " + idType + " id, @RequestBody " + className + " e) { e.setId(id); return service.save(e); }\n\n" +
                "    @DeleteMapping(\"/{id}\")\n" +
                "    public void delete(@PathVariable " + idType + " id) { service.delete(id); }\n" +
                "}\n";
    }

    /**
     * Build frontend CRUD files (placeholder, will extend for full CRUD UI).
     */
    private void buildFrontendCrudFiles(EntityManagement em, java.util.List<java.util.Map<String, Object>> columns, Path projectRoot) throws IOException {
        String name = (em.getEntityName() != null ? em.getEntityName() : tableNameSafe(em)).toLowerCase();
        String className = capitalize(em.getEntityName() == null ? tableNameSafe(em) : em.getEntityName());
        Path frontendDir = projectRoot.resolve("frontend/kkvat-frontend/src/app/generated/" + name);
        Files.createDirectories(frontendDir);

        // Generate HTML for CRUD table and form
        StringBuilder html = new StringBuilder();
        html.append("<h2>" + className + " Management</h2>\n");
        html.append("<form #f=\"ngForm\" (ngSubmit)=\"onSubmit()\">\n");
        for (java.util.Map<String, Object> col : columns) {
            String colName = (String) col.getOrDefault("name", "col");
            String colType = (String) col.getOrDefault("type", "string");
            html.append("  <label>" + colName + ": <input name=\"").append(colName).append("\" [(ngModel)]=\"form.").append(colName).append("\" type=\"").append(mapInputType(colType)).append("\"></label><br>\n");
        }
        html.append("  <button type=\"submit\">Save</button>\n</form>\n");
        html.append("<table border=1>\n<tr>");
        for (java.util.Map<String, Object> col : columns) {
            String colName = (String) col.getOrDefault("name", "col");
            html.append("<th>").append(colName).append("</th>");
        }
        html.append("<th>Actions</th></tr>\n");
        html.append("<tr *ngFor=\"let row of rows\">\n");
        for (java.util.Map<String, Object> col : columns) {
            String colName = (String) col.getOrDefault("name", "col");
            html.append("<td>{{row.").append(colName).append("}}</td>");
        }
        html.append("<td><button (click)=\"edit(row)\">Edit</button> <button (click)=\"delete(row)\">Delete</button></td></tr>\n</table>\n");

        Files.writeString(frontendDir.resolve(name + ".component.html"), html.toString(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        // Generate TS for CRUD logic
        StringBuilder ts = new StringBuilder();
        ts.append("import { Component, OnInit } from '@angular/core';\n");
        ts.append("import { NgForm } from '@angular/forms';\n");
        ts.append("import { ").append(className).append("Service } from './").append(name).append(".service';\n");
        ts.append("@Component({\n");
        ts.append("  selector: 'app-" + name + "',\n");
        ts.append("  templateUrl: './" + name + ".component.html',\n");
        ts.append("  styleUrls: ['./" + name + ".component.css']\n");
        ts.append("})\n");
        ts.append("export class " + className + "Component implements OnInit {\n");
        ts.append("  rows: any[] = [];\n");
        ts.append("  form: any = {};\n");
        ts.append("  editingId: any = null;\n");
        ts.append("  constructor(private svc: " + className + "Service) {}\n");
        ts.append("  ngOnInit() { this.load(); }\n");
        ts.append("  load() { this.svc.getAll().subscribe(r => this.rows = r); }\n");
        ts.append("  onSubmit() {\n");
        ts.append("    if (this.editingId) { this.svc.update(this.editingId, this.form).subscribe(() => { this.load(); this.form = {}; this.editingId = null; }); }\n");
        ts.append("    else { this.svc.create(this.form).subscribe(() => { this.load(); this.form = {}; }); }\n");
        ts.append("  }\n");
        ts.append("  edit(row: any) { this.form = { ...row }; this.editingId = row.id; }\n");
        ts.append("  delete(row: any) { if (confirm('Delete?')) this.svc.delete(row.id).subscribe(() => this.load()); }\n");
        ts.append("}\n");

        Files.writeString(frontendDir.resolve(name + ".component.ts"), ts.toString(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        // CSS
        Files.writeString(frontendDir.resolve(name + ".component.css"), "table { width: 100%; }\nform { margin-bottom: 1em; }\n", StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        // Service TS
        StringBuilder svc = new StringBuilder();
        svc.append("import { Injectable } from '@angular/core';\n");
        svc.append("import { HttpClient } from '@angular/common/http';\n");
        svc.append("@Injectable({ providedIn: 'root' })\n");
        svc.append("export class " + className + "Service {\n");
        svc.append("  base = '/api/generated/" + name + "';\n");
        svc.append("  constructor(private http: HttpClient) {}\n");
        svc.append("  getAll() { return this.http.get<any[]>(this.base); }\n");
        svc.append("  create(data: any) { return this.http.post(this.base, data); }\n");
        svc.append("  update(id: any, data: any) { return this.http.put(this.base + '/' + id, data); }\n");
        svc.append("  delete(id: any) { return this.http.delete(this.base + '/' + id); }\n");
        svc.append("}\n");

        Files.writeString(frontendDir.resolve(name + ".service.ts"), svc.toString(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    /**
     * Map column type to HTML input type.
     */
    private String mapInputType(String type) {
        if (type == null) return "text";
        switch (type.toLowerCase()) {
            case "int": case "integer": case "long": return "number";
            case "bool": case "boolean": return "checkbox";
            case "date": return "date";
            case "datetime": return "datetime-local";
            case "email": return "email";
            case "password": return "password";
            default: return "text";
        }
    }

    // (keep all other methods as previously implemented)

    private void updateProgress(Path target, int step, String name, String status, String message) {
        try {
            Path progress = target.resolve("progress.json");
            java.util.List<java.util.Map<String,Object>> list = new java.util.ArrayList<>();
            if (Files.exists(progress)) {
                String existing = Files.readString(progress);
                try { list = new com.fasterxml.jackson.databind.ObjectMapper().readValue(existing, java.util.List.class); } catch (Exception ignored) { list = new java.util.ArrayList<>(); }
            }
            java.util.Map<String,Object> m = new java.util.HashMap<>();
            m.put("step", step);
            m.put("name", name);
            m.put("status", status);
            m.put("message", message);
            m.put("updatedAt", java.time.LocalDateTime.now().toString());
            list.add(m);
            String out = new com.fasterxml.jackson.databind.ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(list);
            // Write generic progress file for compatibility
            Files.writeString(progress, out, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            // Also write a prefixed progress file named <table>_progress.json when possible
            try {
                String folder = target.getFileName().toString();
                String table = folder.startsWith("entity_") ? folder.substring(7) : folder;
                Path pref = target.resolve(table + "_progress.json");
                Files.writeString(pref, out, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            } catch (Exception ignore) {}
        } catch (Exception e) {
            System.err.println("Failed to update progress.json: " + e.getMessage());
        }
    }

    public List<Map<String, String>> listGenerated() throws IOException {
        Path projectRoot = Path.of(System.getProperty("user.dir"));
        Path root = projectRoot.resolve("generated");
        if (!Files.exists(root)) return List.of();
        return Files.list(root)
                .filter(Files::isDirectory)
                .map(p -> Map.of(
                        "name", p.getFileName().toString(),
                        "path", p.toAbsolutePath().toString(),
                        "manifest", p.resolve("manifest_" + p.getFileName().toString() + ".txt").toAbsolutePath().toString()
                ))
                .collect(Collectors.toList());
    }

    public boolean deleteGenerated(String name) throws IOException {
        Path projectRoot = Path.of(System.getProperty("user.dir"));
        Path target = projectRoot.resolve("generated").resolve(name);
        if (!Files.exists(target)) return false;
        Files.walk(target).sorted(java.util.Comparator.reverseOrder()).forEach(p -> { try { Files.deleteIfExists(p); } catch (IOException ignored) {} });
        return true;
    }

    private String buildDDL(EntityManagement em) {
        // Use the entityTableName from payload when present (do not use safe/modified filename)
        String table = em.getEntityTableName();
        if (table == null || table.isBlank()) table = em.getEntityName();
        if (table == null) table = "generated_table";
        java.util.List<java.util.Map<String, Object>> columns = parseColumns(em);
        System.out.println("[GENERATOR] buildDDL parsed columns: " + (columns == null ? "null" : columns.toString()));

        java.util.List<String> colDefs = new java.util.ArrayList<>();
        java.util.List<String> pkCols = new java.util.ArrayList<>();
        java.util.List<String> indexCols = new java.util.ArrayList<>();
        java.util.List<String> uniqueCols = new java.util.ArrayList<>();

        // Determine column definitions and flags
        if (columns != null && !columns.isEmpty()) {
            for (java.util.Map<String, Object> col : columns) {
                String colName = (String) (col.getOrDefault("column_name", col.getOrDefault("name", "col")));
                Object dt = col.getOrDefault("column_datatype", col.getOrDefault("type", "string"));
                String datatype = dt == null ? "string" : dt.toString();
                Integer length = null;
                try { Object l = col.get("column_length"); if (l != null) length = Integer.parseInt(l.toString()); } catch (Exception ignored) {}
                String sqlType = mapSqlType(datatype, length);

                // flags
                Object primaryFlag = col.getOrDefault("column_primary", col.get("primary"));
                Object indexFlag = col.getOrDefault("column_index", col.get("index"));
                Object uniqueFlag = col.getOrDefault("column_unique", col.get("unique"));

                boolean isPrimary = primaryFlag != null && ("1".equals(primaryFlag.toString()) || "true".equalsIgnoreCase(primaryFlag.toString()));
                boolean isIndex = indexFlag != null && ("1".equals(indexFlag.toString()) || "true".equalsIgnoreCase(indexFlag.toString()));
                boolean isUnique = uniqueFlag != null && ("1".equals(uniqueFlag.toString()) || "true".equalsIgnoreCase(uniqueFlag.toString()));

                if (isPrimary) pkCols.add(colName);
                if (isIndex) indexCols.add(colName);
                if (isUnique) uniqueCols.add(colName);

                String def = "  " + colName + " " + sqlType;
                // If single primary column, mark inline as PRIMARY KEY
                if (isPrimary && pkCols.size() == 1) {
                    def += " PRIMARY KEY";
                }
                colDefs.add(def);
            }
        } else {
            colDefs.add("  name VARCHAR(255)");
        }

        // If no explicit primary specified, include auto id
        boolean hasExplicitPk = !pkCols.isEmpty();
        if (!hasExplicitPk) {
            colDefs.add(0, "  id BIGINT AUTO_INCREMENT PRIMARY KEY");
        } else {
            // If there are multiple PK columns, add composite PK constraint
            if (pkCols.size() > 1) {
                String joined = pkCols.stream().collect(Collectors.joining(", "));
                colDefs.add("  PRIMARY KEY (" + joined + ")");
            }
        }

        // Add audit columns
        colDefs.add("  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP");
        colDefs.add("  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP");

        StringBuilder sb = new StringBuilder();
        sb.append("-- DDL generated by EntityGeneratorService\n");
        sb.append("CREATE TABLE IF NOT EXISTS `").append(table).append("` (\n");
        sb.append(String.join(",\n", colDefs)).append("\n");
        sb.append(") ENGINE=InnoDB;\n");

        // Emit indexes (separate statements)
        if (!indexCols.isEmpty() || !uniqueCols.isEmpty()) {
            for (String ic : indexCols) {
                sb.append("CREATE INDEX idx_").append(table).append("_").append(ic).append(" ON ").append(table).append("(").append(ic).append(");\n");
            }
            for (String uc : uniqueCols) {
                sb.append("CREATE UNIQUE INDEX uq_").append(table).append("_").append(uc).append(" ON ").append(table).append("(").append(uc).append(");\n");
            }
        }

        return sb.toString();
    }

    private String mapSqlType(String datatype, Integer length) {
        if (datatype == null) return length != null ? "VARCHAR(" + length + ")" : "VARCHAR(255)";
        switch (datatype.toLowerCase()) {
            case "string":
            case "varchar":
            case "text":
                return length != null ? "VARCHAR(" + length + ")" : "VARCHAR(255)";
            case "int": case "integer":
                return "INT";
            case "long":
                return "BIGINT";
            case "number":
            case "double":
                return "DOUBLE";
            case "float":
                return "FLOAT";
            case "bool": case "boolean":
                return "TINYINT(1)";
            case "date":
                return "DATE";
            case "datetime":
                return "DATETIME";
            default:
                return length != null ? "VARCHAR(" + length + ")" : "VARCHAR(255)";
        }
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
        sb.append("CREATE PROCEDURE sp_insert_").append(table).append("(IN p_name VARCHAR(255))\nBEGIN\n");
        sb.append("  INSERT INTO ").append(table).append("(name) VALUES (p_name);\nEND$$\nDELIMITER ;\n");
        return sb.toString();
    }

    private String buildStoredProceduresImplSQL(EntityManagement em, java.util.List<java.util.Map<String, Object>> columns) {
        String table = em.getEntityTableName() != null ? em.getEntityTableName() : em.getEntityName();
        if (table == null) table = "generated_table";
        StringBuilder sb = new StringBuilder();
        sb.append("-- Detailed stored procedures for ").append(table).append("\n");

        // Per-entity procedure to move records to archive based on provided criteria and period
        sb.append("DELIMITER $$\n");
        sb.append("CREATE PROCEDURE sp_move_to_archive_"+table+"(\n");
        sb.append("  IN p_criteria_field VARCHAR(128),\n");
        sb.append("  IN p_criteria_value VARCHAR(255),\n");
        sb.append("  IN p_main_months INT,\n");
        sb.append("  IN p_archive_months INT\n");
        sb.append(")\nBEGIN\n");
        sb.append("  DECLARE v_move_sql TEXT;\n");
        sb.append("  DECLARE v_delete_sql TEXT;\n");
        sb.append("  DECLARE v_cleanup_sql TEXT;\n\n");
        sb.append("  SET @cond := CONCAT(p_criteria_field, ' = ', QUOTE(p_criteria_value));\n\n");
        sb.append("  SET @move_sql := CONCAT('INSERT INTO `', '"+table+"_archive', '` SELECT * FROM `', '"+table+"', '` WHERE ', @cond, ' AND updated_at < (NOW() - INTERVAL ', p_main_months, ' MONTH)');\n");
        sb.append("  PREPARE stmt_move FROM @move_sql;\n  EXECUTE stmt_move;\n  DEALLOCATE PREPARE stmt_move;\n\n");
        sb.append("  SET @delete_sql := CONCAT('DELETE FROM `', '"+table+"', '` WHERE ', @cond, ' AND updated_at < (NOW() - INTERVAL ', p_main_months, ' MONTH)');\n");
        sb.append("  PREPARE stmt_delete FROM @delete_sql;\n  EXECUTE stmt_delete;\n  DEALLOCATE PREPARE stmt_delete;\n\n");
        sb.append("  SET v_total_months := p_main_months + p_archive_months;\n");
        sb.append("  SET @cleanup_sql := CONCAT('DELETE FROM `', '"+table+"_archive', '` WHERE ', @cond, ' AND updated_at < (NOW() - INTERVAL ', v_total_months, ' MONTH)');\n");
        sb.append("  PREPARE stmt_cleanup FROM @cleanup_sql;\n  EXECUTE stmt_cleanup;\n  DEALLOCATE PREPARE stmt_cleanup;\n\n");
        sb.append("END$$\nDELIMITER ;\n\n");

        // Convenience wrapper that uses configured criteria/months when present
        String cfgCrit = em.getCriteriaToMoveFromMainToArchiveTable() != null ? em.getCriteriaToMoveFromMainToArchiveTable() : null;
        Integer cfgMonths = em.getHowManyMonthsArchiveTable() != null ? em.getHowManyMonthsArchiveTable() : null;
        if (cfgCrit != null || cfgMonths != null) {
            sb.append("DELIMITER $$\n");
            sb.append("CREATE PROCEDURE sp_move_to_archive_"+table+"_now()\nBEGIN\n");
            String critLiteral = cfgCrit != null ? cfgCrit.replace("'", "\\'") : "1=0";
            int monthsLiteral = cfgMonths != null ? cfgMonths : 0;
            // Attempt to parse simple equality from configured criteria if it is in the form 'field=value' or 'field = value'
            String field = "";
            String value = "";
            try {
                if (critLiteral.contains("=")) {
                    String[] parts = critLiteral.split("=", 2);
                    field = parts[0].trim();
                    value = parts[1].trim().replaceAll("^'|\\'$", "");
                }
            } catch (Exception ignore) {}
            if (!field.isBlank()) {
                sb.append("  CALL sp_move_to_archive_"+table+"('"+field+"', '"+value+"', "+monthsLiteral+", "+monthsLiteral+");\n");
            } else {
                sb.append("  -- No parseable configured criteria; supply parameters to sp_move_to_archive_"+table+"\n");
            }
            sb.append("END$$\nDELIMITER ;\n\n");
        }

        // Example: procedure to purge archive older than N months if configured
        if (em.getHowManyMonthsArchiveTable() != null && em.getHowManyMonthsArchiveTable() > 0) {
            sb.append("DELIMITER $$\n");
            sb.append("CREATE PROCEDURE sp_purge_archive_"+table+"()\nBEGIN\n");
            sb.append("  DELETE FROM "+table+"_archive WHERE created_at < DATE_SUB(CURRENT_DATE, INTERVAL "+em.getHowManyMonthsArchiveTable()+" MONTH);\n");
            sb.append("END$$\nDELIMITER ;\n");
        }

        // Generic procedure to move rows from main table to archive and cleanup
        sb.append("\n-- Stored procedure: move rows from main table to archive and clean up archive\n");
        sb.append("-- Parameters: p_main_table, p_archive_table, p_criteria_field, p_criteria_value, p_main_months, p_archive_months\n");
        sb.append("DELIMITER $$\n");
        sb.append("CREATE DEFINER=CURRENT_USER PROCEDURE `sp_move_to_archive_and_cleanup`(\n");
        sb.append("  IN p_main_table VARCHAR(128),\n");
        sb.append("  IN p_archive_table VARCHAR(128),\n");
        sb.append("  IN p_criteria_field VARCHAR(128),\n");
        sb.append("  IN p_criteria_value VARCHAR(255),\n");
        sb.append("  IN p_main_months INT,\n");
        sb.append("  IN p_archive_months INT\n");
        sb.append(")\nBEGIN\n");
        sb.append("  DECLARE v_move_sql TEXT;\n");
        sb.append("  DECLARE v_delete_sql TEXT;\n");
        sb.append("  DECLARE v_cleanup_sql TEXT;\n");
        sb.append("  DECLARE v_total_months INT;\n\n");
        sb.append("  SET @cond := CONCAT(p_criteria_field, ' = ', QUOTE(p_criteria_value));\n\n");
        sb.append("  SET @move_sql := CONCAT('INSERT INTO `', p_archive_table, '` SELECT * FROM `', p_main_table, '` WHERE ', @cond, ' AND updated_at < (NOW() - INTERVAL ', p_main_months, ' MONTH)');\n");
        sb.append("  PREPARE stmt_move FROM @move_sql;\n  EXECUTE stmt_move;\n  DEALLOCATE PREPARE stmt_move;\n\n");
        sb.append("  SET @delete_sql := CONCAT('DELETE FROM `', p_main_table, '` WHERE ', @cond, ' AND updated_at < (NOW() - INTERVAL ', p_main_months, ' MONTH)');\n");
        sb.append("  PREPARE stmt_delete FROM @delete_sql;\n  EXECUTE stmt_delete;\n  DEALLOCATE PREPARE stmt_delete;\n\n");
        sb.append("  SET v_total_months := p_main_months + p_archive_months;\n");
        sb.append("  SET @cleanup_sql := CONCAT('DELETE FROM `', p_archive_table, '` WHERE ', @cond, ' AND updated_at < (NOW() - INTERVAL ', v_total_months, ' MONTH)');\n");
        sb.append("  PREPARE stmt_cleanup FROM @cleanup_sql;\n  EXECUTE stmt_cleanup;\n  DEALLOCATE PREPARE stmt_cleanup;\n\n");
        sb.append("END$$\nDELIMITER ;\n\n");

        sb.append("-- Example: CALL sp_move_to_archive_and_cleanup('"+table+"', '"+table+"_archive', 'isActive', '0', 6, 2);\n");

        return sb.toString();
    }

    private String buildTriggerInstanceToAuditSQL(EntityManagement em, java.util.List<java.util.Map<String, Object>> columns) {
        String table = em.getEntityTableName() != null ? em.getEntityTableName() : em.getEntityName();
        if (table == null) table = "generated_table";
        // Use canonical SQL naming: uppercase table name for SQL artifacts
        String tableUpper = table.toUpperCase();
        String auditTable = tableUpper + "_AUDIT";
        String instanceTable = tableUpper + "_INSTANCE";
        // Trigger base (strip leading TBL_ if present) and lowercase for trigger names, matching canonical file
        String triggerBase = tableUpper.replaceFirst("(?i)^TBL_", "").toLowerCase();
        StringBuilder sb = new StringBuilder();
        sb.append("-- Trigger to copy approved instance rows into ").append(auditTable).append("\n");
        sb.append("-- Creates triggers to insert audit rows when an instance is approved\n\n");
        sb.append("DELIMITER $$\n\n");

        // AFTER INSERT trigger on instance table
        sb.append("DROP TRIGGER IF EXISTS `trg_"+triggerBase+"_instance_after_insert`$$\n");
        sb.append("CREATE TRIGGER `trg_"+triggerBase+"_instance_after_insert`\n");
        sb.append("AFTER INSERT ON `").append(instanceTable).append("`\n");
        sb.append("FOR EACH ROW\nBEGIN\n");
        sb.append("  IF NEW.status LIKE 'APPROVED%' THEN\n");
        sb.append("    INSERT INTO `").append(auditTable).append("` (\n");
        sb.append("      `target_pk`, `action_type`, `changed_columns`, `old_values`, `new_values`,\n");
        sb.append("      `changed_by`, `approved_by`, `approved_at`, `changed_at`, `comments`\n");
        sb.append("    ) VALUES (\n");
        sb.append("      NEW.target_pk,\n");
        sb.append("      NEW.action_type,\n");
        sb.append("      JSON_KEYS(IFNULL(NEW.new_values, NEW.old_values)),\n");
        sb.append("      NEW.old_values,\n");
        sb.append("      NEW.new_values,\n");
        sb.append("      NEW.created_by,\n");
        sb.append("      NEW.approved_by,\n");
        sb.append("      NEW.approved_at,\n");
        sb.append("      NOW(),\n");
        sb.append("      NEW.comments\n");
        sb.append("    );\n");
        sb.append("  END IF;\nEND$$\n\n");

        // AFTER UPDATE trigger on instance table
        sb.append("DROP TRIGGER IF EXISTS `trg_"+triggerBase+"_instance_after_update`$$\n");
        sb.append("CREATE TRIGGER `trg_"+triggerBase+"_instance_after_update`\n");
        sb.append("AFTER UPDATE ON `").append(instanceTable).append("`\n");
        sb.append("FOR EACH ROW\nBEGIN\n");
        sb.append("  IF NEW.status LIKE 'APPROVED%' AND (OLD.status NOT LIKE 'APPROVED%') THEN\n");
        sb.append("    INSERT INTO `").append(auditTable).append("` (\n");
        sb.append("      `target_pk`, `action_type`, `changed_columns`, `old_values`, `new_values`,\n");
        sb.append("      `changed_by`, `approved_by`, `approved_at`, `changed_at`, `comments`\n");
        sb.append("    ) VALUES (\n");
        sb.append("      NEW.target_pk,\n");
        sb.append("      NEW.action_type,\n");
        sb.append("      JSON_KEYS(IFNULL(NEW.new_values, NEW.old_values)),\n");
        sb.append("      NEW.old_values,\n");
        sb.append("      NEW.new_values,\n");
        sb.append("      NEW.created_by,\n");
        sb.append("      NEW.approved_by,\n");
        sb.append("      NEW.approved_at,\n");
        sb.append("      NOW(),\n");
        sb.append("      NEW.comments\n");
        sb.append("    );\n");
        sb.append("  END IF;\nEND$$\n\n");

        sb.append("DELIMITER ;\n\n");

        sb.append("-- Notes:\n-- - Triggers insert audit rows when an instance is created or updated into an APPROVED state.\n");
        sb.append("-- - `changed_columns` is derived via JSON_KEYS on `new_values` (falls back to `old_values`).\n");
        sb.append("-- - For composite or complex workflows, adjust guards to avoid duplicates.\n");

        return sb.toString();
    }

    private String buildAuditDDL(EntityManagement em, java.util.List<java.util.Map<String, Object>> columns) {
        String table = em.getEntityTableName() != null ? em.getEntityTableName() : em.getEntityName();
        if (table == null) table = "generated_table";
        // Standardized audit table (diff-only) schema per requested template
        // Use entity table name + _audit to follow naming convention
        String auditName = table + "_audit";
        StringBuilder sb = new StringBuilder();
        sb.append("-- ").append(auditName).append(" (diff-only audit table)\n");
        sb.append("CREATE TABLE IF NOT EXISTS `").append(auditName).append("` (\n");
        sb.append("  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,\n");
        sb.append("  `target_pk` VARCHAR(1024) NOT NULL,\n");
        sb.append("  `action_type` VARCHAR(32) NOT NULL,\n");
        sb.append("  `changed_columns` JSON NOT NULL,\n");
        sb.append("  `old_values` JSON,\n");
        sb.append("  `new_values` JSON NOT NULL,\n");
        sb.append("  `changed_by` VARCHAR(64),\n");
        sb.append("  `approved_by` VARCHAR(64),\n");
        sb.append("  `approved_at` TIMESTAMP NULL,\n");
        sb.append("  `changed_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,\n");
        sb.append("  `comments` TEXT\n");
        sb.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;\n\n");
        sb.append("-- Recommended indexes:\n-- CREATE INDEX idx_").append(auditName).append("_target_pk ON ").append(auditName).append("(target_pk);\n");
        sb.append("-- CREATE INDEX idx_").append(auditName).append("_changed_at ON ").append(auditName).append("(changed_at);\n\n");
        sb.append("-- Notes:\n-- - `target_pk` should match the format used in ").append(table).append("_instance.target_pk (string or serialized JSON for composite PKs).\n");
        sb.append("-- - `old_values` / `new_values` store only changed columns (diff). Example changed_columns: [\"firstname\",\"isActive\"]\n");
        return sb.toString();
    }

    private String buildArchiveDDL(EntityManagement em, java.util.List<java.util.Map<String, Object>> columns) {
        String table = em.getEntityTableName() != null ? em.getEntityTableName() : em.getEntityName();
        if (table == null) table = "generated_table";
        java.util.List<java.util.Map<String, Object>> cols = columns != null ? columns : parseColumns(em);

        java.util.List<String> colDefs = new java.util.ArrayList<>();
        java.util.List<String> pkCols = new java.util.ArrayList<>();
        java.util.List<String> indexCols = new java.util.ArrayList<>();
        java.util.List<String> uniqueCols = new java.util.ArrayList<>();

        if (cols != null && !cols.isEmpty()) {
            for (java.util.Map<String, Object> c : cols) {
                String name = (String) (c.getOrDefault("column_name", c.getOrDefault("name", "col")));
                Object dt = c.getOrDefault("column_datatype", c.getOrDefault("type", "string"));
                String datatype = dt == null ? "string" : dt.toString();
                Integer length = null;
                try { Object l = c.get("column_length"); if (l != null) length = Integer.parseInt(l.toString()); } catch (Exception ignored) {}
                String sqlType = mapSqlType(datatype, length);

                Object primaryFlag = c.getOrDefault("column_primary", c.get("primary"));
                Object indexFlag = c.getOrDefault("column_index", c.get("index"));
                Object uniqueFlag = c.getOrDefault("column_unique", c.get("unique"));

                boolean isPrimary = primaryFlag != null && ("1".equals(primaryFlag.toString()) || "true".equalsIgnoreCase(primaryFlag.toString()));
                boolean isIndex = indexFlag != null && ("1".equals(indexFlag.toString()) || "true".equalsIgnoreCase(indexFlag.toString()));
                boolean isUnique = uniqueFlag != null && ("1".equals(uniqueFlag.toString()) || "true".equalsIgnoreCase(uniqueFlag.toString()));

                if (isPrimary) pkCols.add(name);
                if (isIndex) indexCols.add(name);
                if (isUnique) uniqueCols.add(name);

                String def = "  " + name + " " + sqlType;
                if (isPrimary && pkCols.size() == 1) {
                    def += " PRIMARY KEY";
                }
                colDefs.add(def);
            }
        } else {
            colDefs.add("  name VARCHAR(255)");
        }

        boolean hasExplicitPk = !pkCols.isEmpty();
        if (!hasExplicitPk) {
            colDefs.add(0, "  id BIGINT AUTO_INCREMENT PRIMARY KEY");
        } else {
            if (pkCols.size() > 1) {
                String joined = String.join(", ", pkCols);
                colDefs.add("  PRIMARY KEY (" + joined + ")");
            }
        }

        // Add audit-like timestamps for archive
        colDefs.add("  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP");
        colDefs.add("  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP");

        String archive = table + "_archive";
        StringBuilder sb = new StringBuilder();
        sb.append("-- Archive table for ").append(table).append("\n");
        sb.append("CREATE TABLE IF NOT EXISTS `").append(archive).append("` (\n");
        sb.append(String.join(",\n", colDefs)).append("\n");
        sb.append(") ENGINE=InnoDB;\n");

        if (!indexCols.isEmpty() || !uniqueCols.isEmpty()) {
            for (String ic : indexCols) {
                sb.append("CREATE INDEX idx_").append(table).append("_").append(ic).append(" ON ").append(archive).append("(").append(ic).append(");\n");
            }
            for (String uc : uniqueCols) {
                sb.append("CREATE UNIQUE INDEX uq_").append(table).append("_").append(uc).append(" ON ").append(archive).append("(").append(uc).append(");\n");
            }
        }

        return sb.toString();
    }

    private String buildInstanceDDL(EntityManagement em, java.util.List<java.util.Map<String, Object>> columns) {
        // Standardized instance table (diff/approval workflow) per requested template
        String table = em.getEntityTableName() != null ? em.getEntityTableName() : em.getEntityName();
        if (table == null) table = "generated_table";
        // Use entity table name + _instance (safe characters)
        String instanceName = tableNameSafe(em) + "_instance";
        StringBuilder sb = new StringBuilder();
        sb.append("-- Instance table for ").append(table).append(" (workflow/approval queue)\n");
        sb.append("CREATE TABLE IF NOT EXISTS `").append(instanceName).append("` (\n");
        sb.append("  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,\n");
        sb.append("  `action_type` VARCHAR(32) NOT NULL,\n");
        sb.append("  `target_pk` VARCHAR(1024),\n");
        sb.append("  `old_values` JSON,\n");
        sb.append("  `new_values` JSON NOT NULL,\n");
        sb.append("  `status` VARCHAR(32) NOT NULL DEFAULT 'PENDING',\n");
        sb.append("  `created_by` VARCHAR(64),\n");
        sb.append("  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,\n");
        sb.append("  `approved_by` VARCHAR(64),\n");
        sb.append("  `approved_at` TIMESTAMP NULL,\n");
        sb.append("  `comments` TEXT\n");
        sb.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;\n");
        sb.append("\n-- Recommended indexes (create as needed):\n-- CREATE INDEX idx_").append(instanceName).append("_status ON ").append(instanceName).append("(status);\n");
        sb.append("-- CREATE INDEX idx_").append(instanceName).append("_created_at ON ").append(instanceName).append("(created_at);\n");
        return sb.toString();
    }

    private String buildCreateViewsSQL(EntityManagement em, java.util.List<java.util.Map<String, Object>> columns) {
        String table = em.getEntityTableName() != null ? em.getEntityTableName() : em.getEntityName();
        if (table == null) table = "generated_table";
        String tableName = tableNameSafe(em);
        String low = tableName.toLowerCase();
        StringBuilder sb = new StringBuilder();
        sb.append("-- Create or replace views for generated tables\n");
        sb.append("-- Views present all columns from the corresponding tables for read-only consumption\n\n");

        sb.append("CREATE OR REPLACE VIEW `vw_").append(low).append("` AS\n");
        sb.append("SELECT * FROM `").append(tableName).append("`;\n\n");

        if (Boolean.TRUE.equals(em.getDoWeNeedWorkflow())) {
            sb.append("CREATE OR REPLACE VIEW `vw_").append(low).append("_instance` AS\n");
            sb.append("SELECT * FROM `").append(tableName).append("_instance`;\n\n");
        }
        if (Boolean.TRUE.equals(em.getDoWeNeedAuditTable())) {
            sb.append("CREATE OR REPLACE VIEW `vw_").append(low).append("_audit` AS\n");
            sb.append("SELECT * FROM `").append(tableName).append("_audit`;\n\n");
        }
        if (Boolean.TRUE.equals(em.getDoWeNeedArchiveRecords())) {
            sb.append("CREATE OR REPLACE VIEW `vw_").append(low).append("_archive` AS\n");
            sb.append("SELECT * FROM `").append(tableName).append("_archive`;\n\n");
        }

        sb.append("-- Notes:\n");
        sb.append("-- - These are simple SELECT * views to expose the full row shape.\n");
        sb.append("-- - Apply with: mysql -u root -padmin kkvat_automation < ./database/create_views.sql\n");
        sb.append("-- - Consider granting SELECT on the views to roles/users as appropriate.\n");
        return sb.toString();
    }

    private String buildMenuPermissionsSQL(EntityManagement em) {
        String table = em.getEntityTableName() != null ? em.getEntityTableName() : em.getEntityName();
        if (table == null) table = "generated_table";
        String parent = em.getParentMenu() != null ? em.getParentMenu() : "Administration";
        String role = em.getWhichRoleIsEligible() != null ? em.getWhichRoleIsEligible() : "ADMIN";
        // derive friendly names/links
        // Display name prefers explicit entityName, otherwise derive from entityTableName
        String displayName = em.getEntityName() != null && !em.getEntityName().isBlank() ? capitalize(em.getEntityName()) : capitalize(table.replaceFirst("(?i)^tbl_", ""));
        String routeLink = "/" + table.replaceAll("_", "-");
        String menuName = table; // use entityTableName exactly as provided in payload

        StringBuilder sb = new StringBuilder();
        sb.append("-- Insert menu item and grant permission for generated entity ").append(table.toUpperCase()).append("\n");
        sb.append("INSERT INTO menu_items (name, display_name, description, route_link, icon_name, parent_menu_item_id, menu_order)\n");
        sb.append("VALUES\n");
        sb.append("('").append(menuName).append("', '").append(displayName).append("', 'Generated menu for entity ").append(table.toUpperCase()).append("', '").append(routeLink).append("', 'table_chart', (SELECT id FROM menu_items WHERE display_name = '").append(parent).append("' LIMIT 1), 999)\n");
        sb.append("ON DUPLICATE KEY UPDATE id = id;\n\n");

        sb.append("INSERT INTO role_menu_items (role_id, menu_item_id)\n");
        sb.append("SELECT (SELECT id FROM roles WHERE name = '").append(role).append("'), m.id FROM menu_items m WHERE m.name = '").append(menuName).append("'\n");
        sb.append("ON DUPLICATE KEY UPDATE id = id;\n\n");

        sb.append("-- Notes: replace '").append(role).append("' with payload `whichRoleIsEligible` if different.\n");
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

    private String tableNameSafe(EntityManagement em) {
        String candidate = null;
        if (em.getEntityTableName() != null && !em.getEntityTableName().isBlank()) candidate = em.getEntityTableName();
        else if (em.getEntityName() != null && !em.getEntityName().isBlank()) candidate = em.getEntityName();
        if (candidate == null) return "generated_table";
        return candidate.replaceAll("[^A-Za-z0-9]", "_");
    }

    /**
     * Remove all contents of the projectRoot/generated directory.
     */
    private void clearGeneratedRoot(Path projectRoot) {
        try {
            Path root = projectRoot.resolve("generated");
            if (Files.exists(root)) {
                Files.walk(root)
                        .sorted(java.util.Comparator.reverseOrder())
                        .forEach(p -> { try { Files.deleteIfExists(p); } catch (Exception ignored) {} });
            }
            Files.createDirectories(root);
        } catch (Exception e) {
            System.err.println("[GENERATOR] Failed to clear generated root: " + e.getMessage());
        }
    }

    private String capitalize(String s) {
        if (s == null || s.isBlank()) return "Generated";
        String cleaned = s.replaceAll("[^A-Za-z0-9]", "_");
        return Character.toUpperCase(cleaned.charAt(0)) + cleaned.substring(1);
    }

    private String buildEntityJava(EntityManagement em) {
        // Prefer dynamic entity generation if columns are provided
        java.util.List<java.util.Map<String, Object>> columns = parseColumns(em);
        if (columns != null && !columns.isEmpty()) {
            return buildDynamicEntityJava(em, columns);
        }
        String className = capitalize(em.getEntityName() == null ? tableNameSafe(em) : em.getEntityName());
        String table = em.getEntityTableName() != null ? em.getEntityTableName() : em.getEntityName();
        if (table == null) table = "generated_table";
        return "package com.kkvat.automation.generated;\n\n" +
                "import jakarta.persistence.*;\n\n" +
                "@Entity\n@Table(name=\"" + table + "\")\npublic class " + className + " {\n    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;\n    private String name;\n}\n";
    }

    private String buildRepositoryJava(EntityManagement em) {
        String className = capitalize(em.getEntityName() == null ? tableNameSafe(em) : em.getEntityName());
        // determine id type from columns (if explicit primary exists)
        String idType = "Long";
        java.util.List<java.util.Map<String, Object>> columns = parseColumns(em);
        if (columns != null && !columns.isEmpty()) {
            java.util.List<java.util.Map<String, Object>> pks = new java.util.ArrayList<>();
            for (java.util.Map<String, Object> c : columns) {
                Object primaryFlag = c.getOrDefault("column_primary", c.get("primary"));
                if (primaryFlag != null && ("1".equals(primaryFlag.toString()) || "true".equalsIgnoreCase(primaryFlag.toString()))) pks.add(c);
            }
            if (pks.size() == 1) {
                String dt = pks.get(0).getOrDefault("column_datatype", pks.get(0).getOrDefault("type", "string")).toString();
                idType = mapColumnType(dt);
            }
        }
        return "package com.kkvat.automation.generated;\n\nimport org.springframework.data.jpa.repository.JpaRepository;\nimport org.springframework.stereotype.Repository;\n\n@Repository\npublic interface " + className + "Repository extends JpaRepository<" + className + ", " + idType + "> {}\n";
    }

    private void buildFrontendComponent(EntityManagement em, Path projectRoot) throws IOException {
        String name = (em.getEntityName() != null ? em.getEntityName() : tableNameSafe(em)).toLowerCase();
        Path frontendDir = projectRoot.resolve("frontend/kkvat-frontend/src/app/generated/" + name);
        Files.createDirectories(frontendDir);
        Files.writeString(frontendDir.resolve(name + ".component.html"), "<p>Generated list for " + name + "</p>\n", StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        Files.writeString(frontendDir.resolve(name + ".service.ts"), "// minimal service for " + name + "\n", StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    /**
     * Helper to generate from an in-memory EntityManagement (no DB persistence)
     * Useful for testing steps 1-3 without creating a DB record.
     */
    public String generateFromDto(EntityManagement em) throws IOException {
        Path projectRoot = Path.of(System.getProperty("user.dir"));

        // Clear the whole generated folder before creating artifacts for this entity
        clearGeneratedRoot(projectRoot);
        String folderName = "entity_" + tableNameSafe(em).toLowerCase();
        Path target = projectRoot.resolve("generated").resolve(folderName);
        if (Files.exists(target)) {
            try {
                Files.walk(target)
                        .sorted(java.util.Comparator.reverseOrder())
                        .forEach(p -> {
                            try { Files.deleteIfExists(p); } catch (Exception ignored) {}
                        });
            } catch (Exception ignored) {}
        }
        Files.createDirectories(target);

        // Parse columns early so optional artifacts can use them
        java.util.List<java.util.Map<String, Object>> columns = parseColumns(em);

        Path ddlFile = target.resolve("create_" + tableNameSafe(em) + ".sql");
        System.out.println("[GENERATOR] Creating DDL file: " + ddlFile);
        Files.writeString(ddlFile, buildDDL(em), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        String table = tableNameSafe(em);
        Path triggersFile = target.resolve(table + "_triggers.sql");
        System.out.println("[GENERATOR] Creating triggers SQL: " + triggersFile);
        Files.writeString(triggersFile, buildTriggersSQL(em), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        Path storedProcsFile = target.resolve(table + "_stored_procs.sql");
        System.out.println("[GENERATOR] Creating stored procedures SQL: " + storedProcsFile);
        Files.writeString(storedProcsFile, buildStoredProceduresSQL(em), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        Path rollbackFile = target.resolve(table + "_rollback.sql");
        System.out.println("[GENERATOR] Creating rollback SQL: " + rollbackFile);
        Files.writeString(rollbackFile, buildRollbackSQL(em), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        // Optional additional artifacts (for generateFromDto testing)
        try {
            if (Boolean.TRUE.equals(em.getDoWeNeedAuditTable())) {
                Path auditFile = target.resolve(table + "_audit.sql");
                System.out.println("[GENERATOR] Creating audit DDL: " + auditFile);
                Files.writeString(auditFile, buildAuditDDL(em, columns), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            }
            if (Boolean.TRUE.equals(em.getDoWeNeedArchiveRecords())) {
                Path archiveFile = target.resolve(table + "_archive.sql");
                System.out.println("[GENERATOR] Creating archive DDL: " + archiveFile);
                Files.writeString(archiveFile, buildArchiveDDL(em, columns), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            }
            if (Boolean.TRUE.equals(em.getDoWeNeedWorkflow())) {
                Path instanceFile = target.resolve(table + "_instance.sql");
                System.out.println("[GENERATOR] Creating instance DDL: " + instanceFile);
                Files.writeString(instanceFile, buildInstanceDDL(em, columns), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            }
            if (Boolean.TRUE.equals(em.getDoWeNeedCreateView())) {
                Path viewsFile = target.resolve(table + "_create_views.sql");
                System.out.println("[GENERATOR] Creating views SQL: " + viewsFile);
                Files.writeString(viewsFile, buildCreateViewsSQL(em, columns), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            }
            Path storedImpl = target.resolve(table + "_stored_procs_impl.sql");
            System.out.println("[GENERATOR] Creating detailed stored procs SQL: " + storedImpl);
            Files.writeString(storedImpl, buildStoredProceduresImplSQL(em, columns), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            Path trigInstToAudit = target.resolve(table + "_create_trigger_instance_to_audit.sql");
            System.out.println("[GENERATOR] Creating trigger instance->audit SQL: " + trigInstToAudit);
            Files.writeString(trigInstToAudit, buildTriggerInstanceToAuditSQL(em, columns), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            Path menuPerm = target.resolve(table + "_menu_permissions.sql");
            System.out.println("[GENERATOR] Creating menu permissions SQL: " + menuPerm);
            Files.writeString(menuPerm, buildMenuPermissionsSQL(em), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (Exception ex) {
            System.err.println("[GENERATOR] Warning: failed to write optional artifacts: " + ex.getMessage());
        }

        // Generate Entity Java file using table name as filename for generateFromDto as well
        String tableFileBase = tableNameSafe(em);
        Path entityJavaFile = target.resolve(tableFileBase + ".java");
        System.out.println("[GENERATOR] Creating Entity Java: " + entityJavaFile);
        Files.writeString(entityJavaFile, buildEntityJava(em), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        Path repoJavaFile = target.resolve(tableFileBase + "_Repository.java");
        System.out.println("[GENERATOR] Creating Repository Java: " + repoJavaFile);
        Files.writeString(repoJavaFile, buildRepositoryJava(em), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        // Generate Service and Controller for generateFromDto as well
        String className = capitalize(em.getEntityName() == null ? tableNameSafe(em) : em.getEntityName());
        Path serviceJavaFile = target.resolve(tableFileBase + "_Service.java");
        System.out.println("[GENERATOR] Creating Service Java: " + serviceJavaFile);
        Files.writeString(serviceJavaFile, buildServiceJava(em), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        Path controllerJavaFile = target.resolve(tableFileBase + "_Controller.java");
        System.out.println("[GENERATOR] Creating Controller Java: " + controllerJavaFile);
        Files.writeString(controllerJavaFile, buildControllerJava(em), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        System.out.println("[GENERATOR] Creating frontend component for entity: " + em.getEntityName());
        buildFrontendComponent(em, projectRoot);

        System.out.println("[GENERATOR] Updating progress.json");
        updateProgress(target, 8, "COMPLETE", "ok", "Generation complete");

        System.out.println("[GENERATOR] Generation complete for entity: " + em.getEntityName());
        return "Artifacts generated at: " + target.toAbsolutePath().toString();
    }

}
