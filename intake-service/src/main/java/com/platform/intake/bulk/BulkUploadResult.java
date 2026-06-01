package com.platform.intake.bulk;

public class BulkUploadResult {

    private final String fileId;
    private final int eventCount;
    private final String fileName;

    public BulkUploadResult(String fileId, int eventCount, String fileName) {
        this.fileId = fileId;
        this.eventCount = eventCount;
        this.fileName = fileName;
    }

    public String getFileId() {
        return fileId;
    }

    public int getEventCount() {
        return eventCount;
    }

    public String getFileName() {
        return fileName;
    }
}
