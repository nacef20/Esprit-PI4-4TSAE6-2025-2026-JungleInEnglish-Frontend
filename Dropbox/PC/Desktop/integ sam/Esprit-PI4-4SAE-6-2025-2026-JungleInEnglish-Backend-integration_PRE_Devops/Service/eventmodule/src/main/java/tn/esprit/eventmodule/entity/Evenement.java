package tn.esprit.eventmodule.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "evenement")
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Event entity - define all event attributes here")
public class Evenement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique event identifier", example = "1")
    private Long eventId;

    @NotBlank
    @Column(nullable = false)
    @Schema(description = "Event title", example = "Team Workshop", requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    @Schema(description = "Event description", example = "Annual team building workshop")
    private String description;

    @NotNull
    @Column(nullable = false)
    @Schema(description = "Event start date and time", example = "2026-02-15T09:00:00", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime startTime;

    @NotNull
    @Column(nullable = false)
    @Schema(description = "Event end date and time", example = "2026-02-15T17:00:00", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime endTime;

    @Schema(description = "Event location", example = "Conference Room A")
    private String location;

    @Schema(description = "Maximum number of participants", example = "50")
    private int capacity;

    @Column(name = "organizer_id", nullable = false)
    @NotNull
    @Schema(description = "ID of the user who organizes the event", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long organizerId;

    @Column(nullable = false)
    @Schema(description = "Whether a reminder has been sent to participants", example = "false")
    private boolean reminderSent = false;

    @Column(name = "image_url", columnDefinition = "MEDIUMTEXT")
    @Size(max = 16_000_000, message = "imageUrl must not exceed 16 million characters")
    @Schema(description = "Event image as URL or base64 data URL (e.g. data:image/jpeg;base64,...)")
    private String imageUrl;

    @NotBlank
    @Column(nullable = false)
    @Schema(description = "Level of students for this event, e.g. ALL, BEGINNER, etc.", example = "ALL", requiredMode = Schema.RequiredMode.REQUIRED)
    private String level;

    @JsonIgnore
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Participation> participations = new ArrayList<>();

    @Transient
    @Schema(description = "Available seats calculated dynamically")
    private Integer availableSeats;

    public Long getEventId() { return eventId; }
    public void setEventId(Long eventId) { this.eventId = eventId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    public Long getOrganizerId() { return organizerId; }
    public void setOrganizerId(Long organizerId) { this.organizerId = organizerId; }
    public boolean isReminderSent() { return reminderSent; }
    public void setReminderSent(boolean reminderSent) { this.reminderSent = reminderSent; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }
    public List<Participation> getParticipations() { return participations; }
    public void setParticipations(List<Participation> participations) { this.participations = participations; }
    public Integer getAvailableSeats() { return availableSeats; }
    public void setAvailableSeats(Integer availableSeats) { this.availableSeats = availableSeats; }
}
