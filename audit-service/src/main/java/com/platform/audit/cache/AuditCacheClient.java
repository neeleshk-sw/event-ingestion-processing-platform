package com.platform.audit.cache;

import com.platform.common.model.AuditRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Component
public class AuditCacheClient {

    private static final Logger logger = LoggerFactory.getLogger(AuditCacheClient.class);
    private static final String KEY_PREFIX = "audit:";
    private static final Duration CACHE_TTL = Duration.ofHours(24);

    private final RedisTemplate<String, AuditRecord> redisTemplate;

    public AuditCacheClient(RedisTemplate<String, AuditRecord> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void cacheAudit(AuditRecord record) {
        if (record == null || record.getEventId() == null) {
            return;
        }
        String key = buildKey(record.getEventId());
        logger.info("Caching audit record for eventId: {} with key: {}", record.getEventId(), key);
        try {
            redisTemplate.opsForValue().set(key, record, CACHE_TTL);
        } catch (Exception e) {
            logger.error("Failed to cache audit record for eventId: {}", record.getEventId(), e);
        }
    }

    public Optional<AuditRecord> getAudit(UUID eventId) {
        if (eventId == null) {
            return Optional.empty();
        }
        String key = buildKey(eventId);
        logger.info("Fetching audit record from cache for eventId: {} with key: {}", eventId, key);
        try {
            AuditRecord record = redisTemplate.opsForValue().get(key);
            return Optional.ofNullable(record);
        } catch (Exception e) {
            logger.error("Failed to fetch audit record from cache for eventId: {}", eventId, e);
            return Optional.empty();
        }
    }

    private String buildKey(UUID eventId) {
        return KEY_PREFIX + eventId.toString();
    }
}
