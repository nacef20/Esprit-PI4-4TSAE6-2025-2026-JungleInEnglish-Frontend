# Spring Boot CORS Setup

Both your controllers already use `@CrossOrigin(origins = "*")`, which allows all origins. That works for development.

If you want to restrict CORS to your Angular app only (recommended for production), use the prompts below in each Spring project.

---

## Todo Module (port 8081)

**Paste this into Cursor when in your `todomodule` Spring project:**

```
Add CORS configuration to allow requests from my Angular app at http://localhost:4200.

Create a WebMvcConfigurer or CorsConfiguration that:
1. Allows origin http://localhost:4200
2. Allows methods GET, POST, PUT, DELETE, OPTIONS
3. Allows headers *
4. Apply it globally for /api/** paths

I already have @CrossOrigin(origins = "*") on my controllers - I want a centralized CORS config instead that only allows localhost:4200.
```

**Alternative – keep current setup:** Your `@CrossOrigin(origins = "*")` on `TodoController` and `ReminderController` is enough for development. No changes needed.

---

## Event Module (port 8082)

**Paste this into Cursor when in your `eventmodule` Spring project:**

```
Add CORS configuration to allow requests from my Angular app at http://localhost:4200.

Create a WebMvcConfigurer or CorsConfiguration that:
1. Allows origin http://localhost:4200
2. Allows methods GET, POST, PUT, DELETE, OPTIONS
3. Allows headers *
4. Apply it globally for /api/** paths

I already have @CrossOrigin(origins = "*") on my controllers - I want a centralized CORS config instead that only allows localhost:4200.
```

**Alternative – keep current setup:** Your `@CrossOrigin(origins = "*")` on `EvenementController` and `ParticipationController` is enough for development. No changes needed.

---

## Quick Start

1. Start Todo backend: `cd todomodule` → `mvn spring-boot:run` (port 8081)
2. Start Event backend: `cd eventmodule` → `mvn spring-boot:run` (port 8082)
3. Start Angular: `cd piang` → `ng serve` (port 4200)
4. Open http://localhost:4200
