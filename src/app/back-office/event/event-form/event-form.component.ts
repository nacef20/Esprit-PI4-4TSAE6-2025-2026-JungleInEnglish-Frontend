import { Component, OnInit, OnDestroy } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { EventService } from '../../../services/event.service';
import { Evenement } from '../../../models/event.model';
import { AuthService } from '../../../auth/auth.service';
import { WeatherService, WeatherInfo } from '../../../services/weather.service';
import { Subscription, combineLatest, debounceTime, distinctUntilChanged } from 'rxjs';

@Component({
  selector: 'app-event-form',
  templateUrl: './event-form.component.html',
  styleUrls: ['./event-form.component.scss']
})
export class EventFormComponent implements OnInit, OnDestroy {
  selectedFile: File | null = null;
  
  // Suggestion feature
  showSuggester = false;
  suggestionParams = {
    fromDate: '',
    toDate: '',
    duration: 2
  };
  suggestedTime: string | null = null;
  event: any = {};
  eventForm: FormGroup;
  isEditMode = false;
  eventId?: number;
  isLoading = false;

  // Weather feature
  weather: WeatherInfo | null = null;
  weatherLoading = false;
  weatherError = false;
  weatherDate: string | null = null;
  weatherCity: string | null = null;
  private weatherSub?: Subscription;

  constructor(
    private fb: FormBuilder,
    private eventService: EventService,
    private authService: AuthService,
    private route: ActivatedRoute,
    private router: Router,
    private weatherService: WeatherService
  ) {
    this.eventForm = this.fb.group({
      title: ['', [Validators.required, Validators.minLength(3)]],
      description: ['', [Validators.required]],
      startTime: ['', [Validators.required]],
      endTime: ['', [Validators.required]],
      location: ['', [Validators.required]],
      capacity: [10, [Validators.required, Validators.min(1)]],
      imageUrl: [''],
      level: ['', [Validators.required]]
    });
  }

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      if (params['id']) {
        this.isEditMode = true;
        this.eventId = +params['id'];
        this.loadEvent(this.eventId);
      }
    });

    // React to startTime and location changes to fetch weather
    this.weatherSub = combineLatest([
      this.eventForm.get('startTime')!.valueChanges,
      this.eventForm.get('location')!.valueChanges
    ]).pipe(
      debounceTime(600),
      distinctUntilChanged((a, b) => a[0] === b[0] && a[1] === b[1])
    ).subscribe(([startTime, location]) => {
      this.fetchWeather(startTime, location);
    });
  }

  ngOnDestroy(): void {
    this.weatherSub?.unsubscribe();
  }

  fetchWeather(startTime: string, location: string): void {
    if (!startTime) {
      this.weather = null;
      return;
    }
    const dateStr = startTime.substring(0, 10); // YYYY-MM-DD
    
    const targetDate = new Date(dateStr);
    const now = new Date();
    const diffTime = targetDate.getTime() - now.getTime();
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));

    // Open-Meteo's standard forecast API only supports up to 16 days ahead.
    if (diffDays > 15 || diffDays < -90) {
      this.weatherDate = dateStr;
      this.weatherCity = location || 'Tunis';
      this.weather = null;
      this.weatherLoading = false;
      this.weatherError = true;
      return;
    }

    this.weatherDate = dateStr;
    this.weatherCity = location || 'Tunis';
    this.weatherLoading = true;
    this.weatherError = false;

    this.weatherService.getWeatherForCityAndDate(this.weatherCity, dateStr).subscribe({
      next: (data) => {
        this.weather = data;
        this.weatherLoading = false;
      },
      error: () => {
        this.weather = null;
        this.weatherLoading = false;
        this.weatherError = true;
      }
    });
  }

  loadEvent(id: number): void {
    this.isLoading = true;
    this.eventService.getEventById(id).subscribe({
      next: (event) => {
        this.event = event;
        this.eventForm.patchValue({
          ...event,
          startTime: event.startTime ? event.startTime.substring(0, 16) : '',
          endTime: event.endTime ? event.endTime.substring(0, 16) : ''
        });
        this.isLoading = false;
      },
      error: () => {
        alert('Erreur lors du chargement de l\'événement');
        this.router.navigate(['/admin/events']);
      }
    });
  }

  // Smart Scheduling Integration
  checkSuggestedWeather() {
    if (this.suggestedTime) {
      const location = this.eventForm.get('location')?.value || 'Tunis';
      this.fetchWeather(this.suggestedTime, location);
    }
  }

  onSuggestTime() {
    const level = this.eventForm.get('level')?.value;

    if (!this.suggestionParams.fromDate || !this.suggestionParams.toDate || !level) {
      alert('Veuillez remplir les dates de recherche et choisir un niveau.');
      return;
    }

    this.eventService.suggestTimeSlot(
      this.suggestionParams.fromDate + ':00', 
      this.suggestionParams.toDate + ':00', 
      this.suggestionParams.duration, 
      level
    ).subscribe({
      next: (res: any) => {
        this.suggestedTime = res.suggestedTime;
      },
      error: (err: any) => {
        const msg = err.error?.message || 'Aucun créneau disponible trouvé dans cette plage. Essayez d\'élargir vos dates.';
        alert(msg);
      }
    });
  }

  onFileSelected(event: any) {
    const file: File = event.target.files[0];
    if (file) {
      const reader = new FileReader();
      reader.onload = (e: any) => {
        const base64 = e.target.result;
        this.eventForm.patchValue({ imageUrl: base64 });
      };
      reader.readAsDataURL(file);
    }
  }

  applySuggestion() {
    if (this.suggestedTime) {
      const startStr = this.suggestedTime.substring(0, 16);
      const start = new Date(this.suggestedTime); // Parses as local time
      const durationHours = this.suggestionParams.duration || 2;
      const end = new Date(start.getTime() + durationHours * 60 * 60 * 1000);
      
      const pad = (n: number) => n < 10 ? '0' + n : n;
      const endStr = end.getFullYear() + '-' +
                     pad(end.getMonth() + 1) + '-' +
                     pad(end.getDate()) + 'T' +
                     pad(end.getHours()) + ':' +
                     pad(end.getMinutes());

      this.eventForm.patchValue({
        startTime: startStr,
        endTime: endStr
      });
      this.showSuggester = false;
    }
  }

  onSubmit(): void {
    if (this.eventForm.valid) {
      this.isLoading = true;
      const userInfo = this.authService.getUserInfo();
      const eventData = { 
        ...this.eventForm.value,
        organizerId: 1 // Default to 1 if user info not available, but should be from auth
      };

      if (userInfo && userInfo.id) {
        // If your backend expects a numeric ID but Keycloak gives UUID, 
        // ensure userservice is handling the mapping.
        // For now, if numeric is required and we aren't sure, we use 1.
        eventData.organizerId = 1; 
      }
      
      if (this.isEditMode && this.eventId) {
        this.eventService.updateEvent(this.eventId, eventData).subscribe({
          next: () => this.router.navigate(['/admin/events']),
          error: () => this.isLoading = false
        });
      } else {
        this.eventService.createEvent(eventData).subscribe({
          next: () => this.router.navigate(['/admin/events']),
          error: () => this.isLoading = false
        });
      }
    }
  }

  onCancel(): void {
    this.router.navigate(['/admin/events']);
  }
}
