package com.trackingpath.repositories;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trackingpath.entities.Users;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

@Repository
public class DeviceRepositoryImpl implements DeviceRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private ObjectMapper mapper;

    @Override
    public Map<String, Object> getAlldataobject(Users user, int draw, int start, int length, String search) {

        Map<String, String> tabHeading = getallobject(user.getId());
        if (tabHeading.isEmpty()) {
            return Map.of(
                    "draw", draw,
                    "recordsTotal", 0,
                    "recordsFiltered", 0,
                    "data", Collections.emptyList()
            );
        }

        String[] keys = tabHeading.keySet().toArray(new String[0]);
        String selectColumns = String.join(",", keys);

        String baseSql = " FROM devices d INNER JOIN livedata l ON d.id = l.deviceid ";

        // ✅ SEARCH LOGIC (NEW)
        String whereClause = "";

        if (search != null && !search.trim().isEmpty()) {

            String searchParam = "%" + search.toLowerCase() + "%";

            List<String> conditions = new ArrayList<>();

            for (String key : keys) {
                // 👇 Keep your dynamic column logic
                conditions.add("LOWER(CAST(" + key + " AS TEXT)) LIKE :search");
            }

            // optional: include ID
            conditions.add("CAST(d.id AS TEXT) LIKE :search");

            whereClause = " WHERE " + String.join(" OR ", conditions);

            search = searchParam; // overwrite safely
        }

        // ✅ TOTAL COUNT (UNCHANGED)
        Query countQuery = entityManager.createNativeQuery("SELECT COUNT(*) " + baseSql);
        int recordsTotal = ((Number) countQuery.getSingleResult()).intValue();

        // ✅ FILTERED COUNT (NEW)
        Query filteredCountQuery = entityManager.createNativeQuery(
                "SELECT COUNT(*) " + baseSql + whereClause
        );

        if (!whereClause.isEmpty()) {
            filteredCountQuery.setParameter("search", search);
        }

        int recordsFiltered = ((Number) filteredCountQuery.getSingleResult()).intValue();

        // ✅ DATA QUERY (UPDATED)
        String sql = "SELECT d.id as id, " + selectColumns + baseSql + whereClause + " LIMIT :length OFFSET :start";

        Query dataQuery = entityManager.createNativeQuery(sql);
        dataQuery.setParameter("length", length);
        dataQuery.setParameter("start", start);

        if (!whereClause.isEmpty()) {
            dataQuery.setParameter("search", search);
        }

        List<Object[]> rows = dataQuery.getResultList();

        List<Map<String, Object>> data = new ArrayList<>();

        for (Object[] row : rows) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", row[0]);

            for (int i = 0; i < keys.length; i++) {
                map.put(keys[i], row[i + 1]);
            }
            data.add(map);
        }

        return Map.of(
                "draw", draw,
                "recordsTotal", recordsTotal,
                "recordsFiltered", recordsFiltered, // ✅ FIXED
                "data", data
        );
    }

    // ---------- SAME JSON LOGIC ----------

    public Map<String, String> getallobject(long user_id) {
        String json = getObjectListJson(user_id);
        if (json == null || json.trim().isEmpty()) {
            return Collections.emptyMap();
        }
        try {
            return mapper.readValue(json, new TypeReference<Map<String, String>>() {});
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }

    public String getObjectListJson(long user_id) {
        try {
            Query q = entityManager.createNativeQuery("SELECT objectlist FROM users WHERE id = :id");
            q.setParameter("id", user_id);
            return (String) q.getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }
}


