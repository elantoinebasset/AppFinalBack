Il s'agit de mon application de gestion d'emplois du temps construite avec Quarkus, un framework Java moderne et réactif, il s'agit pour ceux qui ne savent pas d'une sorte de dérive de Spring boot .

## Les Fonctionnalités

### Gestion des Utilisateurs
- Créer, lire, mettre à jour, supprimer des utilisateurs
- Authentification par username/email
- Activation/Désactivation de compte

### Gestion des Emplois du Temps
- Créer et organiser plusieurs emplois du temps
- Code couleur pour chaque emploi du temps
- Activation/Désactivation d'emplois du temps

### Gestion des Événements/Tâches
- Ajouter des événements avec horaires précis
- Gérer les priorités (Low, Medium, High)
- Marquer les tâches comme complétées
- Filtrer par date, priorité, statut
- Ajouter des détails (localisation, notes, catégories)

## L'Architecture

```
com.timescheduler/
├── entity/           # Entités JPA
│   ├── User.java
│   ├── Schedule.java
│   └── ScheduleItem.java
├── dto/              # Data Transfer Objects
│   ├── UserDTO.java
│   ├── ScheduleDTO.java
│   └── ScheduleItemDTO.java
├── repository/       # Repositories Panache
│   ├── UserRepository.java
│   ├── ScheduleRepository.java
│   └── ScheduleItemRepository.java
├── service/          # Services métier
│   ├── UserService.java
│   ├── ScheduleService.java
│   └── ScheduleItemService.java
├── resource/         # Endpoints REST
│   ├── UserResource.java
│   ├── ScheduleResource.java
│   ├── ScheduleItemResource.java
│   └── HealthResource.java
├── config/           # Configuration
│   └── RestConfiguration.java
└── SchedulerApplication.java
```

## Les technologies
Voici les différentes languages et technologies utilisé pour faire fonctionner cette app. Pour plus d'information n'hésité pas à regarder le POM.

- Quarkus 3.8.1
- JPA/Hibernate
- PostgreSQL
- JAX-RS
- Lombok
- MapStruct

## Les API Endpoints

### Utilisateurs
Infos API pour les tests
```
POST   /api/users                    # Créer un utilisateur
GET    /api/users                    # Lister tous les utilisateurs
GET    /api/users/{id}               # Récupérer un utilisateur
GET    /api/users/username/{username} # Récupérer par username
PUT    /api/users/{id}               # Mettre à jour un utilisateur
DELETE /api/users/{id}               # Supprimer un utilisateur
POST   /api/users/{id}/deactivate    # Désactiver un compte
```

### Emplois du Temps
```
POST   /api/users/{userId}/schedules           # Créer un emploi du temps
GET    /api/users/{userId}/schedules           # Lister les emplois
GET    /api/users/{userId}/schedules/active    # Lister les emplois actifs
GET    /api/users/{userId}/schedules/{id}      # Récupérer un emploi
PUT    /api/users/{userId}/schedules/{id}      # Mettre à jour
DELETE /api/users/{userId}/schedules/{id}      # Supprimer
POST   /api/users/{userId}/schedules/{id}/deactivate # Désactiver
```

### Événements/Tâches
```
POST   /api/schedules/{scheduleId}/items            # Créer un événement
GET    /api/schedules/{scheduleId}/items            # Lister les événements
GET    /api/schedules/{scheduleId}/items/upcoming   # Événements à venir
GET    /api/schedules/{scheduleId}/items/completed  # Événements complétés
GET    /api/schedules/{scheduleId}/items/by-priority/{priority} # Par priorité
GET    /api/schedules/{scheduleId}/items/{id}       # Récupérer un événement
PUT    /api/schedules/{scheduleId}/items/{id}       # Mettre à jour
DELETE /api/schedules/{scheduleId}/items/{id}       # Supprimer
POST   /api/schedules/{scheduleId}/items/{id}/complete   # Marquer complété
POST   /api/schedules/{scheduleId}/items/{id}/incomplete # Marquer incomplet
```

### Santé de l'Application
```
GET    /api/health                   # État de l'API
```

## Installation & Configuration
Ce sont les près requis pour faire fonctionner l'application. !!!! Je créerais bientôt une image Docker afin de télécharger tout d'un coup. 

### Prérequis
- Java 17+ <-- ICI Java 21 fonctionne aussi et est plus compatible avec lombok
- Maven 3.8+
- PostgreSQL 12+

### Base de Données
```bash
createdb scheduler_db
createuser postgres
```

### Configuration (application.properties)
```properties
quarkus.datasource.username=postgres
quarkus.datasource.password=postgres
quarkus.datasource.jdbc.url=jdbc:postgresql://localhost:5432/scheduler_db
```

### Démarrage
```bash
# Mode développement avec auto-reload
mvn quarkus:dev

# Build production
mvn clean package

# Démarrer l'application packagée
java -jar target/quarkus-scheduler-1.0.0-runner.jar
```

L'API sera accessible sur `http://localhost:8080/api`

## Exemples de Requêtes

### Créer un utilisateur
```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "email": "john@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "password": "password123"
  }'
```

### Créer un emploi du temps
```bash
curl -X POST http://localhost:8080/api/users/1/schedules \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Mon horaire personnel",
    "description": "Gestion de mon planning",
    "color": "#FF5733"
  }'
```

### Créer un événement
```bash
curl -X POST http://localhost:8080/api/schedules/1/items \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Réunion importante",
    "description": "Réunion d'\''équipe",
    "startTime": "2024-06-22T14:00:00",
    "endTime": "2024-06-22T15:00:00",
    "category": "Réunion",
    "priority": 2,
    "location": "Bureau 101",
    "notes": "Apporter les documents"
  }'
```

## Sécurité

Pour la production, implémentez:
- JWT ou OAuth2 pour l'authentification
- Hachage des mots de passe (BCrypt)
- Validation des entrées
- HTTPS
- Rate limiting
- Chiffrement des données sensibles

## Structure des Données

### User
```json
{
  "id": 1,
  "username": "john_doe",
  "email": "john@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "isActive": true,
  "createdAt": "2024-06-22T10:00:00",
  "updatedAt": "2024-06-22T10:00:00"
}
```

### Schedule
```json
{
  "id": 1,
  "name": "Mon emploi du temps",
  "description": "Planning personnel",
  "color": "#FF5733",
  "isActive": true,
  "createdAt": "2024-06-22T10:00:00",
  "updatedAt": "2024-06-22T10:00:00",
  "items": []
}
```

### ScheduleItem
```json
{
  "id": 1,
  "title": "Réunion",
  "description": "Réunion d'équipe",
  "startTime": "2024-06-22T14:00:00",
  "endTime": "2024-06-22T15:00:00",
  "category": "Réunion",
  "priority": 2,
  "isCompleted": false,
  "location": "Bureau 101",
  "notes": "Apporter documents",
  "createdAt": "2024-06-22T10:00:00",
  "updatedAt": "2024-06-22T10:00:00"
}
```

## Tests

```bash
# Lancer les tests
mvn test

# Lancer les tests avec couverture
mvn test jacoco:report
```

## Licence

MIT License

## Support
Si vous avez des questions sur ce code, n'hésite pas à passer par mon portfolio ( https://mainportfolioantoinebasset.netlify.app )pour que je puisse répondre à votre question.
Pour toute question ou problème concernant Quarkus, veuillez consulter la documentation Quarkus officielle:
https://quarkus.io/
# AppFinalBack
