package com.moleep.toeic_master.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmbeddingService {

    private static final String EMBEDDING_API_URL = "http://172.10.5.41/embed";
    private static final String VENUE_EVAL_EMBED_URL = "http://172.10.5.41/venue/eval-embed";

    private final RestTemplate restTemplate;

    public float[] getEmbedding(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }

        try {
            Map<String, String> request = Map.of("text", text);
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(EMBEDDING_API_URL, request, Map.class);

            if (response != null && response.containsKey("embedding")) {
                @SuppressWarnings("unchecked")
                List<Double> embeddingList = (List<Double>) response.get("embedding");
                float[] embedding = new float[embeddingList.size()];
                for (int i = 0; i < embeddingList.size(); i++) {
                    embedding[i] = embeddingList.get(i).floatValue();
                }
                return embedding;
            }
        } catch (Exception e) {
            log.error("Failed to get embedding for text: {}", text, e);
        }
        return null;
    }

    public byte[] floatArrayToBytes(float[] floats) {
        if (floats == null) {
            return null;
        }
        ByteBuffer buffer = ByteBuffer.allocate(floats.length * 4);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        for (float f : floats) {
            buffer.putFloat(f);
        }
        return buffer.array();
    }

    public float[] bytesToFloatArray(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        float[] floats = new float[bytes.length / 4];
        for (int i = 0; i < floats.length; i++) {
            floats[i] = buffer.getFloat();
        }
        return floats;
    }

    public double cosineSimilarity(float[] a, float[] b) {
        if (a == null || b == null || a.length != b.length) {
            return 0.0;
        }

        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < a.length; i++) {
            dotProduct += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }

        if (normA == 0 || normB == 0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    public VenueEvalResult getVenueEvaluation(List<String> reviews) {
        if (reviews == null || reviews.isEmpty()) {
            return null;
        }

        try {
            Map<String, List<String>> request = Map.of("reviews", reviews);
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(VENUE_EVAL_EMBED_URL, request, Map.class);

            if (response != null && response.containsKey("evaluation") && response.containsKey("embedding")) {
                String evaluation = (String) response.get("evaluation");
                @SuppressWarnings("unchecked")
                List<Double> embeddingList = (List<Double>) response.get("embedding");
                float[] embedding = new float[embeddingList.size()];
                for (int i = 0; i < embeddingList.size(); i++) {
                    embedding[i] = embeddingList.get(i).floatValue();
                }
                return new VenueEvalResult(evaluation, embedding);
            }
        } catch (Exception e) {
            log.error("Failed to get venue evaluation for reviews", e);
        }
        return null;
    }

    public record VenueEvalResult(String evaluation, float[] embedding) {}
}
