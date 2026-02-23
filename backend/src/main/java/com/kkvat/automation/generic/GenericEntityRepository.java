package com.kkvat.automation.generic;

import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class GenericEntityRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public GenericEntityRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public int insert(String table, Map<String, Object> values) {
        if (values == null || values.isEmpty()) return 0;
        StringBuilder cols = new StringBuilder();
        StringBuilder params = new StringBuilder();
        MapSqlParameterSource mp = new MapSqlParameterSource();
        values.forEach((k, v) -> {
            if (cols.length() > 0) { cols.append(", "); params.append(", "); }
            cols.append(k);
            params.append(":" + k);
            mp.addValue(k, v);
        });
        String sql = "INSERT INTO " + table + " (" + cols + ") VALUES (" + params + ")";
        return jdbc.update(sql, mp);
    }

    public List<Map<String, Object>> findAll(String table) {
        String sql = "SELECT * FROM " + table;
        return jdbc.queryForList(sql, Map.of());
    }

    /**
     * Find a paginated page of rows with optional text search across text columns and optional sort.
     * Returns a map with keys: total (long) and items (List<Map<String,Object>>)
     */
    public Map<String, Object> findPage(String table, int page, int size, String sortColumn, String sortDir, String q) {
        int offset = (page) * size;
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("limit", size);
        params.addValue("offset", offset);

        // discover text columns for simple fullrow search
        String colsSql = "SELECT COLUMN_NAME FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = :table AND DATA_TYPE IN ('varchar','char','text','mediumtext','longtext')";
        List<String> textCols = jdbc.queryForList(colsSql, Map.of("table", table), String.class);

        String where = "";
        if (q != null && !q.isBlank() && !textCols.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append("(");
            for (int i = 0; i < textCols.size(); i++) {
                if (i > 0) sb.append(" OR ");
                sb.append(textCols.get(i) + " LIKE :q");
            }
            sb.append(")");
            where = "WHERE " + sb.toString();
            params.addValue("q", "%" + q + "%");
        }

        // total count
        String countSql = "SELECT COUNT(*) FROM " + table + " " + where;
        long total = jdbc.queryForObject(countSql, params, Long.class);

        // build order by safely
        String order = "";
        if (sortColumn != null && !sortColumn.isBlank()) {
            String dir = "ASC".equalsIgnoreCase(sortDir) ? "ASC" : "DESC".equalsIgnoreCase(sortDir) ? "DESC" : "ASC";
            order = " ORDER BY " + sortColumn + " " + dir;
        }

        String sql = "SELECT * FROM " + table + " " + where + order + " LIMIT :limit OFFSET :offset";
        List<Map<String, Object>> items = jdbc.queryForList(sql, params);
        return Map.of("total", total, "items", items);
    }

    public Map<String, Object> findById(String table, String pkName, Object id) {
        String sql = "SELECT * FROM " + table + " WHERE " + pkName + " = :id";
        List<Map<String, Object>> rows = jdbc.queryForList(sql, Map.of("id", id));
        return rows.isEmpty() ? null : rows.get(0);
    }

    public int update(String table, String pkName, Object id, Map<String, Object> values) {
        if (values == null || values.isEmpty()) return 0;
        StringBuilder set = new StringBuilder();
        MapSqlParameterSource mp = new MapSqlParameterSource();
        values.forEach((k, v) -> {
            if (set.length() > 0) set.append(", ");
            set.append(k + " = :" + k);
            mp.addValue(k, v);
        });
        mp.addValue("id", id);
        String sql = "UPDATE " + table + " SET " + set + " WHERE " + pkName + " = :id";
        return jdbc.update(sql, mp);
    }

    public int delete(String table, String pkName, Object id) {
        String sql = "DELETE FROM " + table + " WHERE " + pkName + " = :id";
        return jdbc.update(sql, Map.of("id", id));
    }
}
