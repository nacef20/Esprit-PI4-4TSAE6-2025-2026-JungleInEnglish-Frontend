# Documentation API - Gestion de Planning

## 📋 Vue d'ensemble

Cette API REST permet de gérer le planning d'une école anglaise avec trois entités principales :
- **CreneauHoraire** : Gestion des créneaux horaires
- **PlanningSession** : Gestion des sessions de planning
- **Salle** : Gestion des salles

---

## 🔧 Endpoints API

### 1. Créneau Horaire (`/creneaux`)

#### Créer un créneau horaire
```http
POST /creneaux/add
Content-Type: application/json

{
  "heureDebut": "08:00:00",
  "heureFin": "10:00:00",
  "type": "MATIN",
  "jourSemaine": "LUNDI"
}
```

#### Modifier un créneau horaire
```http
PUT /creneaux/update
Content-Type: application/json

{
  "idCreneauHoraire": 1,
  "heureDebut": "09:00:00",
  "heureFin": "11:00:00",
  "type": "MATIN",
  "jourSemaine": "MARDI"
}
```

#### Récupérer tous les créneaux
```http
GET /creneaux/all
```

#### Récupérer un créneau par ID
```http
GET /creneaux/{idCreneauHoraire}
```

#### Supprimer un créneau
```http
DELETE /creneaux/delete/{idCreneauHoraire}
```

---

### 2. Salle (`/salles`)

#### Créer une salle
```http
POST /salles/add
Content-Type: application/json

{
  "nom_salle": "Salle A101",
  "capacite": 30,
  "disponibilite": "DISPONIBLE",
  "equipements": ["VIDEOPROJECTEUR", "TABLEAU_BLANC"],
  "createdBy": "admin"
}
```

#### Modifier une salle
```http
PUT /salles/update
Content-Type: application/json

{
  "id": 1,
  "nom_salle": "Salle A102",
  "capacite": 35,
  "disponibilite": "OCCUPEE",
  "equipements": ["VIDEOPROJECTEUR", "ORDINATEUR"],
  "updatedBy": "admin"
}
```

#### Récupérer toutes les salles
```http
GET /salles/all
```

#### Récupérer une salle par ID
```http
GET /salles/{idSalle}
```

#### Supprimer une salle
```http
DELETE /salles/delete/{idSalle}
```

---

### 3. Planning Session (`/plannings`)

#### Créer une session de planning
```http
POST /plannings/add
Content-Type: application/json

{
  "salle": {
    "id": 1
  },
  "creneau": {
    "idCreneauHoraire": 1
  },
  "groupeId": 101,
  "enseignantId": 201,
  "matiereId": 301,
  "datePlanning": "2026-02-20",
  "statut": "PLANIFIE",
  "observations": "Cours d'anglais niveau débutant",
  "createdBy": "admin"
}
```

#### Modifier une session de planning
```http
PUT /plannings/update
Content-Type: application/json

{
  "idPlanningSession": 1,
  "salle": {
    "id": 2
  },
  "creneau": {
    "idCreneauHoraire": 2
  },
  "groupeId": 102,
  "enseignantId": 202,
  "matiereId": 302,
  "datePlanning": "2026-02-21",
  "statut": "EN_COURS",
  "observations": "Cours modifié",
  "updatedBy": "admin"
}
```

#### Récupérer toutes les sessions
```http
GET /plannings/all
```

#### Récupérer une session par ID
```http
GET /plannings/{idPlanningSession}
```

#### Supprimer une session
```http
DELETE /plannings/delete/{idPlanningSession}
```

---

## 📊 Énumérations

### TypeCreneau
- **MATIN** : "08h-12h"
- **APRES_MIDI** : "14h-18h"
- **SOIREE** : "18h-22h"

### JourSemaine
- LUNDI
- MARDI
- MERCREDI
- JEUDI
- VENDREDI
- SAMEDI

### DisponibiliteSalle
- DISPONIBLE
- OCCUPEE
- EN_TRAVAUX
- FERMEE

### StatutPlanning
- PLANIFIE
- EN_COURS
- TERMINE
- ANNULE
- REPORTE

### Equipement
- **VIDEOPROJECTEUR** : "Vidéo projecteur"
- **TABLEAU_BLANC** : "Tableau blanc interactif"
- **WIFI** : "Connexion Wi-Fi"
- **ORDINATEUR** : "Poste informatique"
- **CLIMATISATION** : "Climatisation"

---

## 🧪 Tests avec cURL

### Exemple : Créer un créneau
```bash
curl -X POST http://localhost:8080/creneaux/add \
  -H "Content-Type: application/json" \
  -d "{\"heureDebut\":\"08:00:00\",\"heureFin\":\"10:00:00\",\"type\":\"MATIN\",\"jourSemaine\":\"LUNDI\"}"
```

### Exemple : Créer une salle
```bash
curl -X POST http://localhost:8080/salles/add \
  -H "Content-Type: application/json" \
  -d "{\"nom_salle\":\"Salle A101\",\"capacite\":30,\"disponibilite\":\"DISPONIBLE\",\"equipements\":[\"VIDEOPROJECTEUR\"],\"createdBy\":\"admin\"}"
```

### Exemple : Créer une session de planning
```bash
curl -X POST http://localhost:8080/plannings/add \
  -H "Content-Type: application/json" \
  -d "{\"salle\":{\"id\":1},\"creneau\":{\"idCreneauHoraire\":1},\"groupeId\":101,\"enseignantId\":201,\"matiereId\":301,\"datePlanning\":\"2026-02-20\",\"statut\":\"PLANIFIE\",\"observations\":\"Cours d'anglais\",\"createdBy\":\"admin\"}"
```

### Exemple : Récupérer tous les créneaux
```bash
curl -X GET http://localhost:8080/creneaux/all
```

### Exemple : Modifier une salle
```bash
curl -X PUT http://localhost:8080/salles/update \
  -H "Content-Type: application/json" \
  -d "{\"id\":1,\"nom_salle\":\"Salle B201\",\"capacite\":40,\"disponibilite\":\"DISPONIBLE\",\"equipements\":[\"VIDEOPROJECTEUR\",\"WIFI\"],\"updatedBy\":\"admin\"}"
```

### Exemple : Supprimer un créneau
```bash
curl -X DELETE http://localhost:8080/creneaux/delete/1
```

---

## 📝 Codes de statut HTTP

- **200 OK** : Requête réussie
- **404 Not Found** : Ressource non trouvée
- **500 Internal Server Error** : Erreur serveur

---

## 🚀 Démarrage de l'application

```bash
# Compiler le projet
./mvnw.cmd clean install

# Lancer l'application
./mvnw.cmd spring-boot:run
```

L'application sera accessible sur : `http://localhost:8080`

---

## 📦 Structure du projet

```
src/main/java/tn/esprit/gestion_planning/
├── Controllers/
│   ├── CreneauHoraireController.java
│   ├── PlanningSessionController.java
│   └── SalleController.java
├── Services/
│   ├── ICreneauHoraireService.java
│   ├── CreneauHoraireServiceImpl.java
│   ├── IPlanningSessionService.java
│   ├── PlanningSessionServiceImpl.java
│   ├── ISalleService.java
│   └── SalleServiceImpl.java
├── Repositories/
│   ├── CreneauRepository.java
│   ├── PlanningSessionRepository.java
│   └── SalleRepository.java
└── Entites/
    ├── CreneauHoraire.java
    ├── PlanningSession.java
    ├── Salle.java
    ├── TypeCreneau.java
    ├── JourSemaine.java
    ├── DisponibiliteSalle.java
    ├── StatutPlanning.java
    └── Equipement.java
```

---

## 📋 Résumé des endpoints

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| POST | `/creneaux/add` | Créer un créneau horaire |
| PUT | `/creneaux/update` | Modifier un créneau horaire |
| GET | `/creneaux/all` | Récupérer tous les créneaux |
| GET | `/creneaux/{id}` | Récupérer un créneau par ID |
| DELETE | `/creneaux/delete/{id}` | Supprimer un créneau |
| POST | `/salles/add` | Créer une salle |
| PUT | `/salles/update` | Modifier une salle |
| GET | `/salles/all` | Récupérer toutes les salles |
| GET | `/salles/{id}` | Récupérer une salle par ID |
| DELETE | `/salles/delete/{id}` | Supprimer une salle |
| POST | `/plannings/add` | Créer une session de planning |
| PUT | `/plannings/update` | Modifier une session de planning |
| GET | `/plannings/all` | Récupérer toutes les sessions |
| GET | `/plannings/{id}` | Récupérer une session par ID |
| DELETE | `/plannings/delete/{id}` | Supprimer une session |
