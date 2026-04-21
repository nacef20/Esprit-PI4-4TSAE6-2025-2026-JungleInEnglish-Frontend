package com.fluently.feedbackservice.dto;

public class ModerationResult {

    private boolean flagged;
    private String cleanedContent;

    public ModerationResult() {}

    public ModerationResult(boolean flagged, String cleanedContent) {
        this.flagged = flagged;
        this.cleanedContent = cleanedContent;
    }

    public boolean isFlagged() { return flagged; }
    public void setFlagged(boolean flagged) { this.flagged = flagged; }

    public String getCleanedContent() { return cleanedContent; }
    public void setCleanedContent(String cleanedContent) { this.cleanedContent = cleanedContent; }
}
