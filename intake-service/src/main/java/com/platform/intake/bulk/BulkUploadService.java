package com.platform.intake.bulk;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.platform.common.exception.InvalidEventException;
import com.platform.common.exception.ProcessingException;
import com.platform.common.model.EventEnvelope;
import com.platform.common.model.EventMetadata;
import com.platform.common.model.EventPayload;
import com.platform.common.model.EventPayload;
import com.platform.common.util.MdcUtil;
import com.platform.intake.service.EventIngestionService;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.HashMap;

@Service
public class BulkUploadService {
    private static final Logger logger = LoggerFactory.getLogger(BulkUploadService.class);

    private final ObjectMapper objectMapper;
    private final EventIngestionService ingestionService;
    private final BulkFileValidator validator;
    private final Path pendingDir;
    private final Path processedDir;
    private final Path failedDir;

    public BulkUploadService(EventIngestionService ingestionService,
            BulkFileValidator validator,
            @Value("${bulk.upload.dir:bulk}") String baseDir) {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.findAndRegisterModules();
        this.ingestionService = ingestionService;
        this.validator = validator;

        Path basePath = Paths.get(baseDir);
        this.pendingDir = basePath.resolve("pending");
        this.processedDir = basePath.resolve("processed");
        this.failedDir = basePath.resolve("failed");
        initDirs();
    }

    public BulkUploadResult handleUpload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidEventException("Bulk upload file is empty");
        }

        byte[] bytes = readBytes(file);
        List<EventEnvelope> events = parseAndValidate(bytes, file.getOriginalFilename());

        String fileId = UUID.randomUUID().toString();
        String safeName = sanitizeFileName(file.getOriginalFilename());
        String fileName = fileId + "-" + safeName;
        Path target = pendingDir.resolve(fileName);

        try {
            Files.write(target, bytes, StandardOpenOption.CREATE_NEW);
        } catch (IOException ex) {
            throw new ProcessingException("Failed to store bulk file: " + ex.getMessage());
        }

        logger.info("Bulk file stored: {} (events={})", fileName, events.size());
        return new BulkUploadResult(fileId, events.size(), fileName);
    }

    public void processFile(Path file) {
        if (file == null || !Files.exists(file)) {
            return;
        }

        try {
            byte[] bytes = Files.readAllBytes(file);
            List<EventEnvelope> events = parseAndValidate(bytes, file.getFileName().toString());

            for (EventEnvelope event : events) {
                MdcUtil.syncMdc(event);
                String correlationId = event.getMetadata().getCorrelationId();
                ingestionService.accept(event, correlationId);
            }

            Files.move(file, processedDir.resolve(file.getFileName()),
                    StandardCopyOption.REPLACE_EXISTING);
            logger.info("Bulk file processed: {} (events={})", file.getFileName(), events.size());
        } catch (Exception ex) {
            moveToFailed(file);
            throw new ProcessingException("Bulk file processing failed: " + ex.getMessage());
        }
    }

    private List<EventEnvelope> parseAndValidate(byte[] bytes, String fileName) {
        String ext = getExtension(fileName);
        List<EventEnvelope> events;

        try {
            if ("csv".equalsIgnoreCase(ext)) {
                events = parseCSV(bytes);
            } else if ("xlsx".equalsIgnoreCase(ext)) {
                events = parseXLSX(bytes);
            } else if ("json".equalsIgnoreCase(ext)) {
                events = objectMapper.readValue(bytes, new TypeReference<List<EventEnvelope>>() {
                });
            } else {
                throw new InvalidEventException("Unsupported file format: " + ext);
            }

            validator.validate(events);

            String batchId = UUID.randomUUID().toString();
            String traceId = UUID.randomUUID().toString();
            for (EventEnvelope event : events) {
                EventMetadata metadata = event.getMetadata();
                if (metadata == null) {
                    metadata = new EventMetadata();
                    event.setMetadata(metadata);
                }
                metadata.setBatchId(batchId);
                if (metadata.getTraceId() == null) {
                    metadata.setTraceId(traceId);
                }
                metadata.setRetryCount(0);
                metadata.setSource("BULK_UPLOAD");
            }

            return events;
        } catch (InvalidEventException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.error("Failed to parse bulk file: {}", fileName, ex);
            throw new InvalidEventException("Invalid bulk file format (" + ext + "): " + ex.getMessage());
        }
    }

    private List<EventEnvelope> parseCSV(byte[] bytes) throws IOException, CsvValidationException {
        List<EventEnvelope> events = new ArrayList<>();
        try (CSVReader reader = new CSVReader(
                new InputStreamReader(new ByteArrayInputStream(bytes), StandardCharsets.UTF_8))) {
            String[] header = reader.readNext();
            if (header == null)
                return events;

            Map<String, Integer> colMap = createColumnMap(header);
            String[] line;
            while ((line = reader.readNext()) != null) {
                events.add(mapRowToEvent(line, colMap));
            }
        }
        return events;
    }

    private List<EventEnvelope> parseXLSX(byte[] bytes) throws IOException {
        List<EventEnvelope> events = new ArrayList<>();
        try (InputStream is = new ByteArrayInputStream(bytes);
                Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);
            if (headerRow == null)
                return events;

            Map<String, Integer> colMap = createColumnMap(headerRow);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null)
                    continue;
                events.add(mapRowToEvent(row, colMap));
            }
        }
        return events;
    }

    private Map<String, Integer> createColumnMap(String[] header) {
        Map<String, Integer> map = new HashMap<>();
        for (int i = 0; i < header.length; i++) {
            map.put(header[i].trim().toLowerCase(), i);
        }
        return map;
    }

    private Map<String, Integer> createColumnMap(Row headerRow) {
        Map<String, Integer> map = new HashMap<>();
        for (Cell cell : headerRow) {
            map.put(cell.getStringCellValue().trim().toLowerCase(), cell.getColumnIndex());
        }
        return map;
    }

    private EventEnvelope mapRowToEvent(String[] values, Map<String, Integer> colMap) {
        EventEnvelope event = new EventEnvelope();
        event.setEventId(parseUUID(getValue(values, colMap, "eventid")));
        event.setProducerId(getValue(values, colMap, "producerid"));
        event.setEventType(getValue(values, colMap, "eventtype"));

        EventMetadata metadata = new EventMetadata();
        metadata.setCorrelationId(getValue(values, colMap, "correlationid"));
        metadata.setBatchId(getValue(values, colMap, "batchid"));
        metadata.setTraceId(getValue(values, colMap, "traceid"));
        event.setMetadata(metadata);

        String ts = getValue(values, colMap, "timestamp");
        event.setReceivedAt(ts != null ? Instant.parse(ts) : Instant.now());

        String payloadJson = getValue(values, colMap, "payload");
        if (payloadJson != null) {
            try {
                EventPayload payload = new EventPayload();
                payload.setData(objectMapper.readValue(payloadJson, new TypeReference<Map<String, Object>>() {
                }));
                event.setPayload(payload);
            } catch (Exception e) {
                logger.warn("Failed to parse payload JSON for event {}: {}", event.getEventId(), e.getMessage());
            }
        }
        return event;
    }

    private EventEnvelope mapRowToEvent(Row row, Map<String, Integer> colMap) {
        EventEnvelope event = new EventEnvelope();
        event.setEventId(parseUUID(getCellValue(row, colMap, "eventid")));
        event.setProducerId(getCellValue(row, colMap, "producerid"));
        event.setEventType(getCellValue(row, colMap, "eventtype"));

        EventMetadata metadata = new EventMetadata();
        metadata.setCorrelationId(getCellValue(row, colMap, "correlationid"));
        metadata.setBatchId(getCellValue(row, colMap, "batchid"));
        metadata.setTraceId(getCellValue(row, colMap, "traceid"));
        event.setMetadata(metadata);

        String ts = getCellValue(row, colMap, "timestamp");
        event.setReceivedAt(ts != null ? Instant.parse(ts) : Instant.now());

        String payloadJson = getCellValue(row, colMap, "payload");
        if (payloadJson != null) {
            try {
                EventPayload payload = new EventPayload();
                payload.setData(objectMapper.readValue(payloadJson, new TypeReference<Map<String, Object>>() {
                }));
                event.setPayload(payload);
            } catch (Exception e) {
                logger.warn("Failed to parse payload JSON for event {}: {}", event.getEventId(), e.getMessage());
            }
        }
        return event;
    }

    private String getValue(String[] values, Map<String, Integer> colMap, String key) {
        Integer idx = colMap.get(key);
        return (idx != null && idx < values.length) ? values[idx] : null;
    }

    private String getCellValue(Row row, Map<String, Integer> colMap, String key) {
        Integer idx = colMap.get(key);
        if (idx == null)
            return null;
        Cell cell = row.getCell(idx);
        if (cell == null)
            return null;
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf(cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> null;
        };
    }

    private UUID parseUUID(String val) {
        return val != null ? UUID.fromString(val) : null;
    }

    private String getExtension(String fileName) {
        if (fileName == null)
            return "";
        int idx = fileName.lastIndexOf('.');
        return idx > 0 ? fileName.substring(idx + 1) : "";
    }

    private byte[] readBytes(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException ex) {
            throw new ProcessingException("Failed to read bulk file: " + ex.getMessage());
        }
    }

    private void initDirs() {
        try {
            Files.createDirectories(pendingDir);
            Files.createDirectories(processedDir);
            Files.createDirectories(failedDir);
        } catch (IOException ex) {
            throw new ProcessingException("Failed to initialize bulk directories: " + ex.getMessage());
        }
    }

    private void moveToFailed(Path file) {
        try {
            Files.move(file, failedDir.resolve(file.getFileName()),
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            logger.error("Failed to move bulk file to failed directory: {}", file, ex);
        }
    }

    private String sanitizeFileName(String original) {
        if (original == null || original.trim().isEmpty()) {
            return "bulk.bin";
        }
        return original.replaceAll("[^A-Za-z0-9._-]", "_");
    }
}
