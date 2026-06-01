package com.platform.intake.bulk;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.stream.Stream;

@Component
public class BulkFileScheduler {
    private static final Logger logger = LoggerFactory.getLogger(BulkFileScheduler.class);

    private final BulkUploadService bulkUploadService;
    private final Path pendingDir;

    public BulkFileScheduler(BulkUploadService bulkUploadService,
                             @Value("${bulk.upload.dir:bulk}") String baseDir) {
        this.bulkUploadService = bulkUploadService;
        this.pendingDir = Paths.get(baseDir).resolve("pending");
    }

    @Scheduled(fixedDelayString = "${bulk.upload.poll-interval-ms:10000}")
    public void processPendingFiles() {
        if (!Files.exists(pendingDir)) {
            return;
        }

        try (Stream<Path> files = Files.list(pendingDir)) {
            files.filter(Files::isRegularFile)
                    .sorted(Comparator.comparing(Path::getFileName))
                    .forEach(this::processSingleFile);
        } catch (IOException ex) {
            logger.error("Bulk file scan failed", ex);
        }
    }

    private void processSingleFile(Path file) {
        try {
            bulkUploadService.processFile(file);
        } catch (Exception ex) {
            logger.error("Bulk file processing failed: {}", file.getFileName(), ex);
        }
    }
}
