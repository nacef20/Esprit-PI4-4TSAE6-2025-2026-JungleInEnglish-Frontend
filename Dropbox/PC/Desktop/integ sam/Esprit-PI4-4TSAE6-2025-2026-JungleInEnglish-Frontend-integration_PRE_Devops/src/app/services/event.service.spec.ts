import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { EventService } from './event.service';
import { Evenement, Participation } from '../models/event.model';

fdescribe('EventService', () => {
  let service: EventService;
  let httpMock: HttpTestingController;

  const mockEvenement: Evenement = {
    eventId: 1,
    organizerId: 1,
    title: 'Test Event',
    description: 'This is a test event',
    level: 'A1',
    capacity: 50,
    startTime: '2026-05-15T10:00:00',
    endTime: '2026-05-15T12:00:00',
    location: 'Room 1',
    availableSeats: 50
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [EventService]
    });
    service = TestBed.inject(EventService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should fetch all events', () => {
    service.getEvents().subscribe((events) => {
      expect(events.length).toBe(1);
      expect(events[0].title).toEqual('Test Event');
    });

    const req = httpMock.expectOne('http://localhost:8222/api/evenements/find');
    expect(req.request.method).toBe('GET');
    req.flush([mockEvenement]);
  });

  it('should get available seats for an event', () => {
    service.getAvailableSeats(1).subscribe((res) => {
      expect(res.availableSeats).toBe(45);
    });

    const req = httpMock.expectOne('http://localhost:8222/api/evenements/1/available-seats');
    expect(req.request.method).toBe('GET');
    req.flush({ availableSeats: 45 });
  });

  it('should register user to event', () => {
    const mockParticipation: Participation = {
      participantId: 1,
      userId: 'user2',
      event: mockEvenement,
      present: false,
      registrationDate: '2026-05-15T10:00:00'
    };

    service.register('user2', 1).subscribe((participation) => {
      expect(participation.participantId).toBe(1);
    });

    const req = httpMock.expectOne('http://localhost:8222/api/participations/register');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ userId: 'user2', eventId: 1 });
    req.flush(mockParticipation);
  });

  it('should suggest best time slot', () => {
    const fromDate = '2026-05-15T08:00:00';
    const toDate = '2026-05-15T18:00:00';
    
    service.suggestTimeSlot(fromDate, toDate, 2, 'A1').subscribe((res) => {
      expect(res.suggestedTime).toBe('2026-05-15T14:00:00');
    });

    const req = httpMock.expectOne((request) => request.url === 'http://localhost:8222/api/evenements/suggest-time-slot');
    expect(req.request.method).toBe('GET');
    expect(req.request.params.get('durationInHours')).toBe('2');
    expect(req.request.params.get('level')).toBe('A1');
    req.flush({ suggestedTime: '2026-05-15T14:00:00' });
  });
});
