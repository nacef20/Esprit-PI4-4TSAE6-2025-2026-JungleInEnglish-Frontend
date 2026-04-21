package com.fluently.feedbackservice.dto;

import com.fluently.feedbackservice.entities.ReactionType;

public class ReactionRequest {

    private String studentId;
    private ReactionType reactionType;

    public ReactionRequest() {}

    public ReactionRequest(String studentId, ReactionType reactionType) {
        this.studentId = studentId;
        this.reactionType = reactionType;
    }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public ReactionType getReactionType() { return reactionType; }
    public void setReactionType(ReactionType reactionType) { this.reactionType = reactionType; }
}