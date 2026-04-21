package com.fluently.lessonsservice.dto.progress;


import lombok.Data;

@Data
public class AttachmentCompleteRequest {

    private Long attachmentId;



    public Long getAttachmentId() {
        return attachmentId;
    }

    public void setAttachmentId(Long attachmentId) {
        this.attachmentId = attachmentId;
    }
}
