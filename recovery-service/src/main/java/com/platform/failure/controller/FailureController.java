package com.platform.failure.controller;

import com.platform.common.model.FailedEventRecord;
import com.platform.failure.service.FailureService;
import com.platform.common.util.MdcUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/failures")
public class FailureController {

    private static final Logger logger = LoggerFactory.getLogger(FailureController.class);

    private final FailureService failureService;

    public FailureController(FailureService failureService) {
        this.failureService = failureService;
    }

    public ResponseEntity<Void> recordFailure(
            @RequestBody FailedEventRecord record) {

        MdcUtil.syncMdc(record.getEvent());
        logger.info("Recording failed event at stage={}", record.getStage());
        failureService.record(record);
        return ResponseEntity.accepted().build();
    }
}
