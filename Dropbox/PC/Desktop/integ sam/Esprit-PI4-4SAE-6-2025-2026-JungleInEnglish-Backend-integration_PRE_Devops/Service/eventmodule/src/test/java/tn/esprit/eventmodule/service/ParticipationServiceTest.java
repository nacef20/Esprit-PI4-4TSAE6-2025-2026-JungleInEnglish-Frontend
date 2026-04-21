package tn.esprit.eventmodule.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.eventmodule.entity.Evenement;
import tn.esprit.eventmodule.entity.Participation;
import tn.esprit.eventmodule.repository.ParticipationRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ParticipationServiceTest {

    @Mock
    private ParticipationRepository participationRepository;

    @Mock
    private EvenementService evenementService;

    @Mock
    private QrCodeGeneratorService qrCodeGeneratorService;

    @InjectMocks
    private ParticipationService participationService;

    private Evenement event;
    private String userId = "user123";

    @BeforeEach
    void setUp() {
        event = new Evenement();
        event.setEventId(1L);
        event.setTitle("Test Event");
        event.setCapacity(10);
    }

    @Test
    void registerToEvent_ShouldCreateParticipation() {
        // Arrange
        when(evenementService.findById(1L)).thenReturn(event);
        when(evenementService.isFull(1L)).thenReturn(false);
        when(qrCodeGeneratorService.generateQrCodeBase64(any())).thenReturn("base64qrcode");
        when(participationRepository.save(any(Participation.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        Participation result = participationService.registerToEvent(userId, 1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getEvent()).isEqualTo(event);
        assertThat(result.getPublicToken()).isNotNull();
        assertThat(result.getQrCodeUrl()).isEqualTo("base64qrcode");
        verify(participationRepository).save(any(Participation.class));
    }

    @Test
    void registerToEvent_ShouldThrowException_WhenFull() {
        // Arrange
        when(evenementService.findById(1L)).thenReturn(event);
        when(evenementService.isFull(1L)).thenReturn(true);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            participationService.registerToEvent(userId, 1L);
        });
        assertThat(exception.getMessage()).isEqualTo("Event is full");
    }

    @Test
    void scanQrCode_ShouldMarkAttendance() {
        // Arrange
        Participation p = new Participation();
        p.setPresent(false);
        String token = "valid-token";
        when(participationRepository.findByPublicToken(token)).thenReturn(Optional.of(p));
        when(participationRepository.save(any(Participation.class))).thenReturn(p);

        // Act
        Participation result = participationService.scanQrCode(token);

        // Assert
        assertThat(result.isPresent()).isTrue();
        assertThat(result.getCheckInTime()).isNotNull();
        verify(participationRepository).save(p);
    }

    @Test
    void scanQrCode_ShouldThrowException_IfAlreadyCheckedIn() {
        // Arrange
        Participation p = new Participation();
        p.setPresent(true);
        String token = "valid-token";
        when(participationRepository.findByPublicToken(token)).thenReturn(Optional.of(p));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            participationService.scanQrCode(token);
        });
        assertThat(exception.getMessage()).isEqualTo("Participant already checked in!");
    }
}
