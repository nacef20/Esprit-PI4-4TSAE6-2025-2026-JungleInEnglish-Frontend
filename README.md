# Jungle In English - Frontend


Angular frontend for the Jungle In English project.

## 1. Overview

This Angular application consumes a microservices backend through the API Gateway.

- Local frontend: `http://localhost:4200`
- Backend API Gateway: `http://localhost:8222`
- Keycloak authentication: `http://localhost:8085`

The project is organized around several domains:

- `front-office`
- `back-office`
- `shop`
- `auth`

## 2. Tech Stack

- Angular 18.2.x
- TypeScript 5.5.x
- SCSS
- TailwindCSS
- RxJS
- Keycloak JS
- Chart.js / ng2-charts / ApexCharts
- STOMP + SockJS (real time)
- ZXing (QR scanner)
- Service Worker (PWA in production)

## 3. Prerequisites

1. Node.js 18+ (Node 20 LTS recommended)
2. npm
3. Backend running (at least Gateway + required services)
4. Keycloak available at `http://localhost:8085`

Check versions:

```bash
node -v
npm -v
```

## 4. Installation

From the frontend root:

```bash
npm install
```

## 5. Run in Development

```bash
npm start
```

Or:

```bash
ng serve
```

Application URL:

`http://localhost:4200`

## 6. Available Scripts

```bash
npm run start   # dev server
npm run build   # production build
npm run watch   # watch build in development mode
npm run test    # unit tests (Karma)
```

## 7. Environment Configuration

Main file:

- `src/environments/environment.ts`

Current values:

- `apiUrl`: `http://localhost:8222`
- `apiBaseUrl`: `http://localhost:8222`
- Keycloak URL: `http://localhost:8085`
- Realm: `jungleinenglish-realm`
- ClientId: `jungleinenglish-frontend`

Note:

- Keycloak is initialized during bootstrap in `src/main.ts` through `initKeycloak()`.

## 8. Routing and Modules

Main routes:

- `/`, `/front-office`, `/front`, `/courses` -> front-office
- `/admin`, `/back` -> back-office (protected by `AuthGuard`)
- `/shop` -> shop module

The wildcard route is currently commented out (diagnostic mode), which can affect handling of invalid URLs.

## 9. Production Build

```bash
npm run build
```

Output:

- `dist/school-management`

Service worker is enabled in production configuration (`ngsw-config.json`).

## 10. Quality and Tests

Unit tests:

```bash
npm run test
```

Quality tools present in dependencies:

- `stylelint`
- `postcss`

## 11. Troubleshooting

1. API / 404 / CORS errors
- Ensure backend is running and Gateway responds on `8222`.

2. Authentication errors
- Verify Keycloak (`8085`), realm, and frontend client settings.

3. Some features cannot connect
- Some Angular services still contain hardcoded localhost URLs.
- Verify consistency with `environment.apiBaseUrl`.

4. Blank screen at startup
- Check browser console for Keycloak bootstrap or routing errors.

## 12. Quick Commands

```bash
# install
npm install

# start dev
npm start

# production build
npm run build

# tests
npm run test
```

---


