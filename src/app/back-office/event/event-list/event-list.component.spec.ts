import { ComponentFixture, TestBed } from '@angular/core/testing';
import { EventListComponent } from './event-list.component';
import { EventService } from '../../../services/event.service';
import { AuthService } from '../../../auth/auth.service';
import { Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { Evenement, Participation } from '../../../models/event.model';
import Swal from 'sweetalert2';
import { RouterTestingModule } from '@angular/router/testing';

fdescribe('EventListComponent', () => {
  let component: EventListComponent;
  let fixture: ComponentFixture<EventListComponent>;
  let mockEventService: any;
  let mockAuthService: any;
  let mockRouter: any;

  const mockEvents: Evenement[] = [
    {
      eventId: 1,
      organizerId: 1,
      title: 'Valid Event',
      description: 'Test description',
      level: 'A1',
      capacity: 50,
      startTime: '2026-05-15T10:00:00',
      endTime: '2026-05-15T12:00:00',
      location: 'Room 1',
      availableSeats: 25
    }
  ];

  beforeEach(async () => {
    mockEventService = jasmine.createSpyObj('EventService', ['getEvents', 'deleteEvent', 'getParticipantsByEvent']);
    mockEventService.getEvents.and.returnValue(of(mockEvents));
    mockEventService.deleteEvent.and.returnValue(of({}));

    mockAuthService = jasmine.createSpyObj('AuthService', ['isAdmin', 'getRoles', 'getUserInfo']);
    mockAuthService.isAdmin.and.returnValue(true);

    mockRouter = jasmine.createSpyObj('Router', ['navigate']);

    // Temporarily suppress confirm dialogs during tests
    spyOn(window, 'confirm').and.returnValue(true);
    spyOn(Swal, 'fire').and.stub();

    await TestBed.configureTestingModule({
      declarations: [EventListComponent],
      imports: [RouterTestingModule],
      providers: [
        { provide: EventService, useValue: mockEventService },
        { provide: AuthService, useValue: mockAuthService },
        { provide: Router, useValue: mockRouter }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(EventListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  afterEach(() => {
    // Spies are reset automatically by Jasmine
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load events on init', () => {
    expect(mockEventService.getEvents).toHaveBeenCalled();
    expect(component.events.length).toBe(1);
    expect(component.events[0].title).toBe('Valid Event');
    expect(component.isLoading).toBe(false);
  });

  it('should handle error when loading events fails', () => {
    mockEventService.getEvents.and.returnValue(throwError(() => new Error('Error')));
    component.loadEvents();
    
    expect(component.isLoading).toBe(false);
    expect(component.errorMessage).toBe('Erreur lors du chargement des événements.');
  });

  it('should navigate to create event', () => {
    component.createEvent();
    expect(mockRouter.navigate).toHaveBeenCalledWith(['/admin/events/new']);
  });

  it('should navigate to edit event', () => {
    component.editEvent(1);
    expect(mockRouter.navigate).toHaveBeenCalledWith(['/admin/events/edit', 1]);
  });

  it('should delete event and reload list', () => {
    component.deleteEvent(1);
    expect(window.confirm).toHaveBeenCalledWith('Êtes-vous sûr de vouloir supprimer cet événement ?');
    expect(mockEventService.deleteEvent).toHaveBeenCalledWith(1);
    // Since loadEvents is called again, getEvents should have been called twice (1 for init, 1 for delete reload)
    expect(mockEventService.getEvents).toHaveBeenCalledTimes(2);
  });

  it('should view participants successfully', () => {
    const mockParticipations: Participation[] = [
      { participantId: 1, userId: 'user1', event: mockEvents[0], present: true }
    ];
    mockEventService.getParticipantsByEvent.and.returnValue(of(mockParticipations));
    
    component.viewParticipants(1);
    expect(mockEventService.getParticipantsByEvent).toHaveBeenCalledWith(1);
    expect(Swal.fire).toHaveBeenCalled();
  });

  it('should show info message if no participants found', () => {
    mockEventService.getParticipantsByEvent.and.returnValue(of([]));
    
    component.viewParticipants(1);
    expect(Swal.fire).toHaveBeenCalledWith('Participants', 'Aucun participant inscrit pour le moment.', 'info');
  });

  it('should show error if fails to load participants', () => {
    mockEventService.getParticipantsByEvent.and.returnValue(throwError(() => new Error('Error')));
    
    component.viewParticipants(1);
    expect(Swal.fire).toHaveBeenCalledWith('Erreur', 'Impossible de récupérer la liste des participants.', 'error');
  });
});
