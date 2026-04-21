package tn.esprit.eventmodule.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.eventmodule.entity.Evenement;
import tn.esprit.eventmodule.repository.EvenementRepository;
import tn.esprit.eventmodule.repository.ParticipationRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EvenementServiceTest {

    @Mock
    private EvenementRepository evenementRepository;

    @Mock
    private ParticipationRepository participationRepository;

    @Mock
    private ImageStorageService imageStorageService;

    @InjectMocks
    private EvenementService evenementService;

    private Evenement event;

    @BeforeEach
    void setUp() {
        event = new Evenement();
        event.setEventId(1L);
        event.setTitle("Workshop");
        event.setCapacity(10);
        event.setLevel("A1");
        event.setStartTime(LocalDateTime.now().plusDays(1));
        event.setEndTime(LocalDateTime.now().plusDays(1).plusHours(2));
    }

    @Test
    void getAvailableSeats_ShouldReturnCapacityMinusParticipants() {
        // Arrange
        when(evenementRepository.findById(1L)).thenReturn(Optional.of(event));
        when(participationRepository.countByEvent_EventId(1L)).thenReturn(3L);

        // Act
        int available = evenementService.getAvailableSeats(1L);

        // Assert
        assertThat(available).isEqualTo(7); // 10 - 3
    }

    @Test
    void isFull_ShouldReturnTrue_WhenSeatsAreZero() {
        // Arrange
        when(evenementRepository.findById(1L)).thenReturn(Optional.of(event));
        when(participationRepository.countByEvent_EventId(1L)).thenReturn(10L);

        // Act
        boolean full = evenementService.isFull(1L);

        // Assert
        assertThat(full).isTrue();
    }

    @Test
    void suggestBestTimeSlot_ShouldReturnStartTime_WhenNoConflicts() {
        // Arrange
        LocalDateTime from = LocalDateTime.now().plusDays(1);
        LocalDateTime to = from.plusDays(1);
        when(evenementRepository.findEventsByLevelAndDateRange("A1", from, to)).thenReturn(Collections.emptyList());

        // Act
        LocalDateTime result = evenementService.suggestBestTimeSlot(from, to, 2, "A1");

        // Assert
        assertThat(result).isEqualTo(from);
    }
}
