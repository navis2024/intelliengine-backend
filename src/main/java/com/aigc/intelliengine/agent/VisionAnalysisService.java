package com.aigc.intelliengine.agent;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * 多模态视觉分析 -- 从MinIO读取帧缩略图，转base64内联发送给Kimi vision模型
 */
@Slf4j
@Service
public class VisionAnalysisService {

    @Value("${llm.api-key:}")
    private String apiKey;

    @Value("${llm.base-url:https://api.moonshot.cn/v1}")
    private String baseUrl;

    @Value("${llm.vision.model:kimi-k2.5}")
    private String model;

    @Value("${minio.bucket}")
    private String bucket;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final MinioClient minioClient;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build();

    public VisionAnalysisService(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    public String analyzeFrame(String thumbnailObject, String frameContext) {
        try {
            // MinIO presigned URLs use localhost -- external APIs can't reach them.
            // Read thumbnail bytes from MinIO and inline as base64 data URL.
            String dataUrl = readAndEncode(thumbnailObject);
            if (dataUrl == null) return null;

            Map<String, Object> systemMsg = Map.of("role", "system", "content",
                    "你是一个专业的视频画面分析助手。请用简洁的中文描述视频帧的画面内容，包括：镜头类型、场景、主体、光线、色调。50字以内。");
            Map<String, Object> userContent = Map.of("role", "user", "content", List.of(
                    Map.of("type", "image_url", "image_url", Map.of("url", dataUrl)),
                    Map.of("type", "text", "text",
                            "请描述这个视频帧的画面内容(" + frameContext + ")：")
            ));

            Map<String, Object> body = Map.of(
                    "model", model,
                    "messages", List.of(systemMsg, userContent),
                    "temperature", 1.0,
                    "max_tokens", 200
            );

            String json = objectMapper.writeValueAsString(body);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/chat/completions"))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(120))
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            // Retry once for rate limiting
            for (int attempt = 0; attempt < 2; attempt++) {
                if (attempt > 0) { Thread.sleep(3000); }
                HttpResponse<String> resp = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                if (resp.statusCode() == 200) {
                    Map<String, Object> result = objectMapper.readValue(resp.body(), Map.class);
                    List<Map<String, Object>> choices = (List<Map<String, Object>>) result.get("choices");
                    if (choices != null && !choices.isEmpty()) {
                        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                        return (String) message.get("content");
                    }
                }
                if (resp.statusCode() == 429 && attempt == 0) {
                    log.info("Vision API rate limited, retrying after 3s");
                    continue;
                }
                int len = resp.body().length();
                log.warn("Vision API status={} attempt={}: {}", resp.statusCode(), attempt,
                        resp.body().substring(0, Math.min(len, 100)));
                if (resp.statusCode() != 429) break;
            }
            return null;
        } catch (Exception e) {
            log.warn("Vision analysis exception: {}", e.toString());
            return null;
        }
    }

    private String readAndEncode(String objectName) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (var stream = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucket).object(objectName).build())) {
                byte[] buf = new byte[8192];
                int n;
                while ((n = stream.read(buf)) != -1) baos.write(buf, 0, n);
            }
            return "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (Exception e) {
            log.warn("MinIO read failed for {}: {}", objectName, e.getMessage());
            return null;
        }
    }
}
