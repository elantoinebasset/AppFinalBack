# Documentation API - Scheduler

## Vue d'ensemble

Cette API REST permet de gérer complètement une application de calendrier et d'emplois du temps avec les capacités suivantes:

- 👥 Gestion des utilisateurs
- 📅 Gestion des emplois du temps
- ✅ Gestion des événements/tâches

## Base URL

```
http://localhost:8080/api
```

## Modèles de Données

### User
Représente un utilisateur de l'application.

| Champ     | Type          | Description                           |
| --------- | ------------- | ------------------------------------- |
| id        | Long          | ID unique (généré)                    |
| username  | String        | Nom d'utilisateur unique              |
| email     | String        | Email unique                          |
| firstName | String        | Prénom                                |
| lastName  | String        | Nom                                   |
| password  | String        | Mot de passe (à hasher en production) |
| isActive  | Boolean       | Statut du compte                      |
| createdAt | LocalDateTime | Date de création                      |
| updatedAt | LocalDateTime | Date de mise à jour                   |

### Schedule
Représente un emploi du temps appartenant à un utilisateur.

| Champ       | Type               | Description              |
| ----------- | ------------------ | ------------------------ |
| id          | Long               | ID unique (généré)       |
| name        | String             | Nom de l'emploi du temps |
| description | String             | Description (optionnel)  |
| color       | String             | Couleur hex (#RRGGBB)    |
| user        | User               | Utilisateur propriétaire |
| isActive    | Boolean            | Statut actif             |
| createdAt   | LocalDateTime      | Date de création         |
| updatedAt   | LocalDateTime      | Date de mise à jour      |
| items       | List<ScheduleItem> | Liste des événements     |

### ScheduleItem
Représente un événement/tâche dans un emploi du temps.

| Champ       | Type          | Description                                |
| ----------- | ------------- | ------------------------------------------ |
| id          | Long          | ID unique (généré)                         |
| title       | String        | Titre de l'événement                       |
| description | String        | Description (optionnel)                    |
| startTime   | LocalDateTime | Date/heure de début                        |
| endTime     | LocalDateTime | Date/heure de fin                          |
| category    | String        | Catégorie (Réunion, Travail, Personnel...) |
| priority    | Integer       | 0=Low, 1=Medium, 2=High                    |
| isCompleted | Boolean       | Statut de complétion                       |
| location    | String        | Localisation (optionnel)                   |
| notes       | String        | Notes (optionnel)                          |
| schedule    | Schedule      | Emploi du temps parent                     |
| createdAt   | LocalDateTime | Date de création                           |
| updatedAt   | LocalDateTime | Date de mise à jour                        |

## Endpoints

### 👥 Utilisateurs

#### Créer un utilisateur
```http
POST /users
Content-Type: application/json

{
  "username": "john_doe",
  "email": "john@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "password": "password123"
}
```

**Response (201 Created):**
```json
{
  "id": 1,
  "username": "john_doe",
  "email": "john@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "isActive": true,
  "createdAt": "2024-06-22T10:30:00",
  "updatedAt": "2024-06-22T10:30:00"
}
```

#### Récupérer tous les utilisateurs
```http
GET /users
```

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "username": "john_doe",
    "email": "john@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "isActive": true,
    "createdAt": "2024-06-22T10:30:00",
    "updatedAt": "2024-06-22T10:30:00"
  }
]
```

#### Récupérer un utilisateur par ID
```http
GET /users/1
```

**Response (200 OK):**
```json
{
  "id": 1,
  "username": "john_doe",
  "email": "john@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "isActive": true,
  "createdAt": "2024-06-22T10:30:00",
  "updatedAt": "2024-06-22T10:30:00"
}
```

#### Récupérer un utilisateur par username
```http
GET /users/username/john_doe
```

#### Mettre à jour un utilisateur
```http
PUT /users/1
Content-Type: application/json

{
  "username": "john_doe_updated",
  "email": "john_updated@example.com",
  "firstName": "John",
  "lastName": "Doe Updated",
  "isActive": true
}
```

#### Désactiver un utilisateur
```http
POST /users/1/deactivate
```

#### Supprimer un utilisateur
```http
DELETE /users/1
```

---

### 📅 Emplois du Temps

#### Créer un emploi du temps
```http
POST /users/1/schedules
Content-Type: application/json

{
  "name": "Mon emploi du temps personnel",
  "description": "Gestion de mes activités",
  "color": "#FF5733"
}
```

**Response (201 Created):**
```json
{
  "id": 1,
  "name": "Mon emploi du temps personnel",
  "description": "Gestion de mes activités",
  "color": "#FF5733",
  "isActive": true,
  "createdAt": "2024-06-22T11:00:00",
  "updatedAt": "2024-06-22T11:00:00",
  "items": []
}
```

#### Récupérer tous les emplois du temps d'un utilisateur
```http
GET /users/1/schedules
```

#### Récupérer les emplois actifs d'un utilisateur
```http
GET /users/1/schedules/active
```

#### Récupérer un emploi du temps spécifique
```http
GET /users/1/schedules/1
```

#### Mettre à jour un emploi du temps
```http
PUT /users/1/schedules/1
Content-Type: application/json

{
  "name": "Mon emploi du temps mise à jour",
  "description": "Description modifiée",
  "color": "#3366FF",
  "isActive": true
}
```

#### Désactiver un emploi du temps
```http
POST /users/1/schedules/1/deactivate
```

#### Supprimer un emploi du temps
```http
DELETE /users/1/schedules/1
```

---

### ✅ Événements/Tâches

#### Créer un événement
```http
POST /schedules/1/items
Content-Type: application/json

{
  "title": "Réunion importante",
  "description": "Réunion d'équipe hebdomadaire",
  "startTime": "2024-06-22T14:00:00",
  "endTime": "2024-06-22T15:00:00",
  "category": "Réunion",
  "priority": 2,
  "location": "Bureau 101",
  "notes": "Apporter les documents du projet"
}
```

**Response (201 Created):**
```json
{
  "id": 1,
  "title": "Réunion importante",
  "description": "Réunion d'équipe hebdomadaire",
  "startTime": "2024-06-22T14:00:00",
  "endTime": "2024-06-22T15:00:00",
  "category": "Réunion",
  "priority": 2,
  "isCompleted": false,
  "location": "Bureau 101",
  "notes": "Apporter les documents du projet",
  "createdAt": "2024-06-22T11:15:00",
  "updatedAt": "2024-06-22T11:15:00"
}
```

#### Récupérer tous les événements d'un emploi du temps
```http
GET /schedules/1/items
```

#### Récupérer les événements à venir
```http
GET /schedules/1/items/upcoming
```

#### Récupérer les événements complétés
```http
GET /schedules/1/items/completed
```

#### Récupérer les événements par priorité
```http
GET /schedules/1/items/by-priority/2
```

Valeurs de priorité:
- `0` = Low
- `1` = Medium
- `2` = High

#### Récupérer un événement spécifique
```http
GET /schedules/1/items/1
```

#### Mettre à jour un événement
```http
PUT /schedules/1/items/1
Content-Type: application/json

{
  "title": "Réunion importante - Mise à jour",
  "description": "Réunion modifiée",
  "startTime": "2024-06-22T15:00:00",
  "endTime": "2024-06-22T16:00:00",
  "category": "Réunion",
  "priority": 1,
  "location": "Bureau 101",
  "notes": "Updated notes",
  "isCompleted": false
}
```

#### Marquer un événement comme complété
```http
POST /schedules/1/items/1/complete
```

#### Marquer un événement comme incomplet
```http
POST /schedules/1/items/1/incomplete
```

#### Supprimer un événement
```http
DELETE /schedules/1/items/1
```

---

### 🏥 Santé

#### Vérifier la santé de l'API
```http
GET /health
```

**Response (200 OK):**
```json
{
  "status": "UP",
  "message": "Scheduler API is running"
}
```

## Codes de Réponse HTTP

| Code | Signification                               |
| ---- | ------------------------------------------- |
| 200  | OK - Requête réussie                        |
| 201  | Created - Ressource créée                   |
| 204  | No Content - Succès sans contenu de réponse |
| 400  | Bad Request - Erreur dans la requête        |
| 404  | Not Found - Ressource non trouvée           |
| 500  | Internal Server Error - Erreur serveur      |

## Gestion des Erreurs

Les erreurs sont retournées au format JSON:

```json
{
  "message": "Description de l'erreur"
}
```

Exemple:
```http
POST /users
Content-Type: application/json

{
  "username": "john_doe",
  "email": "existing@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "password": "password123"
}
```

**Response (400 Bad Request):**
```json
{
  "message": "Email already exists: existing@example.com"
}
```

## Exemples avec cURL

### Créer un utilisateur
```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "marie_dupont",
    "email": "marie@example.com",
    "firstName": "Marie",
    "lastName": "Dupont",
    "password": "secure123"
  }'
```

### Créer un emploi du temps
```bash
curl -X POST http://localhost:8080/api/users/1/schedules \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Calendrier professionnel",
    "description": "Mon calendrier de travail",
    "color": "#1E90FF"
  }'
```

### Créer un événement
```bash
curl -X POST http://localhost:8080/api/schedules/1/items \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Présentation client",
    "description": "Présentation du projet Q3",
    "startTime": "2024-06-23T09:00:00",
    "endTime": "2024-06-23T10:30:00",
    "category": "Présentation",
    "priority": 2,
    "location": "Salle de conférence A",
    "notes": "Préparer slides"
  }'
```

### Marquer comme complété
```bash
curl -X POST http://localhost:8080/api/schedules/1/items/1/complete
```

### Obtenir les événements à venir
```bash
curl http://localhost:8080/api/schedules/1/items/upcoming
```

## Notes d'Implémentation

- Les timestamps sont en format ISO-8601 (LocalDateTime)
- Les couleurs doivent être au format hexadécimal (#RRGGBB)
- Les priorités: 0=Low, 1=Medium, 2=High
- Les horaires de fin doivent être après les horaires de début
- Tous les champs texte doivent être non-vides au moment de la création
