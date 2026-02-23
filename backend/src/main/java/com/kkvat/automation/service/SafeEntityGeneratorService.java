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
import java.util.Optional;

@Service
public class SafeEntityGeneratorService {

    private final EntityManagementRepository repository;
    private final EntityManagementService emService;
    private final DataSource dataSource;

    public SafeEntityGeneratorService(EntityManagementRepository repository,
                                      EntityManagementService emService,
                                      DataSource dataSource) {
        this.repository = repository;
        this.emService = emService;
        this.dataSource = dataSource;
    }

    @Transactional
    public String generate(Long id) throws IOException {
        Optional<EntityManagement> opt = repository.findById(id);
        if (opt.isEmpty()) throw new RuntimeException("EntityManagement not found");
        EntityManagement em = opt.get();

        Path projectRoot = Path.of(System.getProperty("user.dir"));
        String folder = em.getEntityTableName() != null ? em.getEntityTableName() : em.getEntityName();
        if (folder == null) folder = "generated";
        Path target = projectRoot.resolve("generated").resolve(folder);
        Files.createDirectories(target);

        // write minimal DDL and execute
        Path ddl = target.resolve("create_" + tableNameSafe(em) + ".sql");
        Files.writeString(ddl, buildDDL(em), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        try {
            executeSqlFiles(target);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        try (Connection c = dataSource.getConnection()) {
            if (!verifyTableExists(c, c.getCatalog(), tableNameSafe(em))) throw new RuntimeException("Table verify failed");
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        emService.setStatus(id, "GENERATED");
        return "OK";
    }

    private void executeSqlFiles(Path target) throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            Files.list(target)
                    .filter(p -> p.toString().toLowerCase().endsWith(".sql"))
                    .sorted()
                    .forEach(p -> {
                        try {
                            String sql = Files.readString(p);
                            ScriptUtils.executeSqlScript(conn, new ByteArrayResource(sql.getBytes()));
                        } catch (Exception e) { throw new RuntimeException(e); }
                    });
        }
    }

    private boolean verifyTableExists(Connection conn, String database, String tableName) throws java.sql.SQLException {
        PreparedStatement ps = null; ResultSet rs = null;
        try {
            String sql = "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = ? AND table_name = ?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, database);
            ps.setString(2, tableName);
            rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception ignored) {}
            try { if (ps != null) ps.close(); } catch (Exception ignored) {}
        }
    }

    private String buildDDL(EntityManagement em) {
        String table = em.getEntityTableName() != null ? em.getEntityTableName() : em.getEntityName();
        if (table == null) table = "generated_table";
        return "CREATE TABLE IF NOT EXISTS `" + table + "` (id BIGINT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(255));";
    }

    private String tableNameSafe(EntityManagement em) {
        String s = em.getEntityTableName() != null ? em.getEntityTableName() : em.getEntityName();
        if (s == null) return "generated_table";
        return s.replaceAll("[^A-Za-z0-9]", "_");
    }
}
