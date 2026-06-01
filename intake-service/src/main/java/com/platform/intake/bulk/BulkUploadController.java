package com.platform.intake.bulk;

import com.platform.common.model.EventStatus;
import com.platform.common.model.ProcessingResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/events")
public class BulkUploadController {
    private static final Logger logger = LoggerFactory.getLogger(BulkUploadController.class);

    private final BulkUploadService bulkUploadService;

    public BulkUploadController(BulkUploadService bulkUploadService) {
        this.bulkUploadService = bulkUploadService;
    }

    @PostMapping("/bulk")
    public ResponseEntity<ProcessingResult> upload(@RequestParam("file") MultipartFile file) {
        logger.info("Received bulk upload file: {}", file != null ? file.getOriginalFilename() : "null");

        BulkUploadResult result = bulkUploadService.handleUpload(file);

        return ResponseEntity.accepted()
                .body(new ProcessingResult(
                        EventStatus.RECEIVED,
                        "Bulk file accepted. fileId=" + result.getFileId()
                                + " count=" + result.getEventCount()));
    }
}
