import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, switchMap, map, of, catchError } from 'rxjs';

export interface WeatherInfo {
  temperature_max: number;
  temperature_min: number;
  weathercode: number;
  windspeed: number;
  precipitation: number;
  condition: string;
  icon: string;
}

@Injectable({
  providedIn: 'root'
})
export class WeatherService {

  private geocodeUrl = 'https://geocoding-api.open-meteo.com/v1/search';
  private forecastUrl = 'https://api.open-meteo.com/v1/forecast';

  // Default: Tunis, Tunisia
  private defaultLat = 36.8065;
  private defaultLon = 10.1815;

  constructor(private http: HttpClient) {}

  /**
   * Fetches weather for a given city on a specific date (YYYY-MM-DD).
   * Falls back to Tunis coordinates if city geocoding fails.
   */
  getWeatherForCityAndDate(city: string, date: string): Observable<WeatherInfo | null> {
    const trimmedCity = (city || '').trim();

    if (!trimmedCity || !date) {
      return of(null);
    }

    const geocode$ = this.http.get<any>(`${this.geocodeUrl}?name=${encodeURIComponent(trimmedCity)}&count=1&language=en&format=json`).pipe(
      map(res => {
        if (res?.results?.length > 0) {
          return { lat: res.results[0].latitude, lon: res.results[0].longitude };
        }
        return { lat: this.defaultLat, lon: this.defaultLon };
      }),
      catchError(() => of({ lat: this.defaultLat, lon: this.defaultLon }))
    );

    return geocode$.pipe(
      switchMap(({ lat, lon }) => this.fetchForecast(lat, lon, date))
    );
  }

  /**
   * Fetches weather using literal coordinates and a specific date.
   */
  getWeatherByCoords(lat: number, lon: number, date: string): Observable<WeatherInfo | null> {
    return this.fetchForecast(lat, lon, date);
  }

  private fetchForecast(lat: number, lon: number, date: string): Observable<WeatherInfo | null> {
    const url = `${this.forecastUrl}?latitude=${lat}&longitude=${lon}&daily=weathercode,temperature_2m_max,temperature_2m_min,windspeed_10m_max,precipitation_sum&timezone=auto&start_date=${date}&end_date=${date}`;

    return this.http.get<any>(url).pipe(
      map(res => {
        const daily = res?.daily;
        if (!daily || !daily.time?.length) return null;

        const code = daily.weathercode[0];
        const { condition, icon } = this.interpretWeatherCode(code);

        return {
          temperature_max: Math.round(daily.temperature_2m_max[0]),
          temperature_min: Math.round(daily.temperature_2m_min[0]),
          weathercode: code,
          windspeed: Math.round(daily.windspeed_10m_max[0]),
          precipitation: daily.precipitation_sum[0],
          condition,
          icon
        } as WeatherInfo;
      }),
      catchError(() => of(null))
    );
  }

  private interpretWeatherCode(code: number): { condition: string; icon: string } {
    if (code === 0) return { condition: 'Clear Sky', icon: '☀️' };
    if (code <= 2) return { condition: 'Partly Cloudy', icon: '⛅' };
    if (code === 3) return { condition: 'Overcast', icon: '☁️' };
    if (code <= 49) return { condition: 'Foggy', icon: '🌫️' };
    if (code <= 57) return { condition: 'Drizzle', icon: '🌦️' };
    if (code <= 67) return { condition: 'Rain', icon: '🌧️' };
    if (code <= 77) return { condition: 'Snow', icon: '❄️' };
    if (code <= 82) return { condition: 'Rain Showers', icon: '🌧️' };
    if (code <= 86) return { condition: 'Snow Showers', icon: '🌨️' };
    if (code >= 95) return { condition: 'Thunderstorm', icon: '⛈️' };
    return { condition: 'Unknown', icon: '🌡️' };
  }
}
