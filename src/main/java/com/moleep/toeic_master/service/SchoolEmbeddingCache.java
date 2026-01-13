package com.moleep.toeic_master.service;

import com.moleep.toeic_master.entity.School;
import com.moleep.toeic_master.repository.SchoolRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class SchoolEmbeddingCache {

    private final SchoolRepository schoolRepository;
    private final EmbeddingService embeddingService;

    private final Map<Long, float[]> cache = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        loadAllEmbeddings();
    }

    private void loadAllEmbeddings() {
        log.info("Loading school embeddings into cache...");
        List<School> schools = schoolRepository.findAll();
        int count = 0;
        for (School school : schools) {
            if (school.getEmbedding() != null) {
                float[] embedding = embeddingService.bytesToFloatArray(school.getEmbedding());
                if (embedding != null) {
                    cache.put(school.getId(), embedding);
                    count++;
                }
            }
        }
        log.info("Loaded {} school embeddings into cache", count);
    }

    public void put(Long schoolId, float[] embedding) {
        if (embedding != null) {
            cache.put(schoolId, embedding);
        }
    }

    public float[] get(Long schoolId) {
        return cache.get(schoolId);
    }

    public void remove(Long schoolId) {
        cache.remove(schoolId);
    }

    public Map<Long, float[]> getAll() {
        return cache;
    }
}
