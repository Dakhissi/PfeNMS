# Système de Gestion Réseau - Guide de Démarrage

## 📋 Table des Matières
- [Vue d'ensemble](#vue-densemble)
- [Prérequis](#prérequis)
- [Configuration de PostgreSQL](#configuration-de-postgresql)
- [Installation et Démarrage](#installation-et-démarrage)
- [Accès à l'Application](#accès-à-lapplication)
- [Configuration](#configuration)
- [Dépannage](#dépannage)
- [Documentation API](#documentation-api)

## 🌟 Vue d'ensemble

Ce projet est un système de gestion réseau basé sur Spring Boot qui permet de :
- **Surveiller des équipements réseau** via SNMP
- **Découvrir automatiquement** les périphériques réseau
- **Gérer les alertes** et notifications en temps réel
- **Recevoir et traiter** les traps SNMP
- **Naviguer dans les MIB** et objets SNMP
- **Créer des profils de surveillance** personnalisés (ICMP, UDP, IP)

## 🔧 Prérequis

Avant de commencer, assurez-vous d'avoir installé :

### Logiciels Requis
- **Java 21** ou version supérieure
- **Maven 3.6+** pour la gestion des dépendances
- **PostgreSQL 12+** ou Docker pour la base de données
- **Git** pour cloner le projet

### Vérification des Versions
```bash
# Vérifier Java
java -version

# Vérifier Maven
mvn -version

# Vérifier PostgreSQL (si installé localement)
psql --version

# Vérifier Docker (optionnel)
docker --version
```

## 🐘 Configuration de PostgreSQL

### Option 1 : Utiliser Docker (Recommandé)

#### Démarrage avec Docker Compose
```bash
# Naviguer vers le dossier du projet
cd c:\Users\Lenovo\Desktop\pfe\backend

# Démarrer uniquement PostgreSQL
docker-compose -f local-docker-compose.yml up db -d

# Ou utiliser le docker-compose principal (démarre aussi l'application)
docker-compose up -d
```

#### Informations de Connexion Docker
- **Hôte** : `localhost`
- **Port** : `5432`
- **Utilisateur** : `postgres`
- **Mot de passe** : `example`
- **Base de données** : `postgres` (par défaut)

### Option 2 : Installation PostgreSQL Locale

#### Installation sur Windows
1. Télécharger PostgreSQL depuis [postgresql.org](https://www.postgresql.org/download/)
2. Suivre l'assistant d'installation
3. Configurer le mot de passe pour l'utilisateur `postgres`

#### Configuration de la Base de Données
```sql
-- Se connecter à PostgreSQL en tant qu'utilisateur postgres
psql -U postgres -h localhost

-- Créer la base de données (optionnel, Spring Boot peut la créer automatiquement)
CREATE DATABASE network_management;

-- Créer un utilisateur spécifique (optionnel)
CREATE USER network_user WITH PASSWORD 'example';
GRANT ALL PRIVILEGES ON DATABASE network_management TO network_user;
```

#### Démarrage du Service PostgreSQL
```bash
# Windows (en tant qu'administrateur)
net start postgresql-x64-14

# Ou utiliser pgAdmin ou le service Windows
```

### Option 3 : PostgreSQL avec Docker (Manuel)
```bash
# Démarrer PostgreSQL avec Docker
docker run --name postgres-network-mgmt \
  -e POSTGRES_PASSWORD=example \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_DB=postgres \
  -p 5432:5432 \
  -d postgres:latest

# Vérifier que le conteneur fonctionne
docker ps
```

## 🚀 Installation et Démarrage

### Méthode 1 : Script de Démarrage Automatique (Windows)
```batch
# Utiliser le script fourni
run-dev.bat
```

Ce script va :
- Compiler l'application avec Maven
- Démarrer l'application avec le profil `dev`
- Créer automatiquement des utilisateurs de test
- Afficher les informations de connexion

### Méthode 2 : Démarrage Manuel

#### 1. Cloner et Préparer le Projet
```bash
# Naviguer vers le dossier
cd c:\Users\Lenovo\Desktop\pfe\backend

# Nettoyer et compiler
mvn clean compile
```

#### 2. Configuration des Variables d'Environnement (Optionnel)
```bash
# Windows PowerShell
$env:POSTGRES_DB_SERVER_ADDRESS="localhost"
$env:POSTGRES_DB_SERVER_PORT="5432"
$env:POSTGRES_USER="postgres"
$env:POSTGRES_PASSWORD="example"

# Windows CMD
set POSTGRES_DB_SERVER_ADDRESS=localhost
set POSTGRES_DB_SERVER_PORT=5432
set POSTGRES_USER=postgres
set POSTGRES_PASSWORD=example
```

#### 3. Démarrer l'Application
```bash
# Avec le profil dev (données de test)
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Ou sans profil spécifique
mvn spring-boot:run
```

### Méthode 3 : Utilisation de Docker Compose (Tout en un)
```bash
# Démarrer l'application complète (Base de données + Application)
docker-compose up -d

# Voir les logs
docker-compose logs -f

# Arrêter les services
docker-compose down
```

## 🌐 Accès à l'Application

### URLs Principales
- **Application principale** : http://localhost:8080
- **Documentation API (Swagger)** : http://localhost:8080/swagger-ui.html
- **Health Check** : http://localhost:8080/actuator/health
- **API Docs JSON** : http://localhost:8080/v3/api-docs

### Comptes Utilisateurs par Défaut (Profil dev)
```
Administrateur :
- Nom d'utilisateur : admin
- Mot de passe : admin123

Utilisateur :
- Nom d'utilisateur : user
- Mot de passe : user123
```

### Test de Connexion
```bash
# Test de l'API de santé
curl http://localhost:8080/actuator/health

# Test de connexion
curl -X POST http://localhost:8080/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

## ⚙️ Configuration

### Configuration de Base de Données (application.yml)
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/postgres
    username: postgres
    password: example
  jpa:
    hibernate:
      ddl-auto: create  # Recrée les tables à chaque démarrage
```

### Configuration SNMP
```yaml
app:
  trap-receiver:
    enabled: true
    port: 162              # Port standard pour les traps SNMP
    community: public      # Communauté SNMP par défaut
    auto-start: true
  monitoring:
    enabled: true
    interval: 300000       # Intervalle de surveillance (5 minutes)
    snmp-timeout: 5000     # Timeout SNMP (5 secondes)
```

### Variables d'Environnement Supportées
| Variable | Valeur par Défaut | Description |
|----------|-------------------|-------------|
| `POSTGRES_DB_SERVER_ADDRESS` | `localhost` | Adresse du serveur PostgreSQL |
| `POSTGRES_DB_SERVER_PORT` | `5432` | Port PostgreSQL |
| `POSTGRES_USER` | `postgres` | Nom d'utilisateur PostgreSQL |
| `POSTGRES_PASSWORD` | `example` | Mot de passe PostgreSQL |

## 🔍 Dépannage

### Problèmes Courants

#### 1. Erreur de Connexion à PostgreSQL
```
Error: Connection to localhost:5432 refused
```
**Solutions :**
- Vérifier que PostgreSQL est démarré
- Vérifier les credentials dans `application.yml`
- Tester la connexion : `psql -U postgres -h localhost`

#### 2. Port 8080 Déjà Utilisé
```
Error: Port 8080 is already in use
```
**Solutions :**
- Changer le port dans `application.yml` :
```yaml
server:
  port: 8081
```
- Ou arrêter l'application qui utilise le port 8080

#### 3. Erreur Java Version
```
Error: Unsupported class file major version
```
**Solution :** Installer Java 21 ou supérieur

#### 4. Erreur Maven
```
Error: Maven not found
```
**Solution :** Ajouter Maven au PATH ou utiliser le wrapper Maven :
```bash
# Windows
.\mvnw spring-boot:run

# Unix/Linux
./mvnw spring-boot:run
```

#### 5. Erreur de Pagination/Tri dans l'API
```
PropertyReferenceException: No property '["string"]' found for type 'Device'
```
**Solutions :**
- Utiliser des paramètres de tri valides : `id`, `name`, `status`, `type`, `createdAt`
- Éviter les caractères spéciaux dans les paramètres de tri
- Exemple d'URL correcte : `GET /api/devices?page=0&size=10&sortBy=name&sortDir=asc`

### Vérification de l'État
```bash
# Vérifier les processus
jps                              # Processus Java
docker ps                       # Conteneurs Docker
netstat -an | find "8080"      # Port application
netstat -an | find "5432"      # Port PostgreSQL
```

### Logs de Débogage
```bash
# Activer les logs détaillés
mvn spring-boot:run -Dspring-boot.run.profiles=dev -Dlogging.level.com.farukgenc=DEBUG

# Voir les logs Docker
docker-compose logs -f app
docker-compose logs -f db
```

## 📚 Documentation API

### Groupes d'API Disponibles
1. **Device Management API** - Gestion des équipements
2. **Device Sub-Components API** - Interfaces, unités système, profils
3. **MIB Management API** - Navigation et gestion des MIB
4. **Alert Management API** - Gestion des alertes
5. **Discovery & Monitoring API** - Découverte et surveillance
6. **Trap Management API** - Gestion des traps SNMP
7. **Authentication API** - Authentification et inscription

### Exemples d'Utilisation API

#### Authentification
```bash
# Connexion
curl -X POST http://localhost:8080/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'

# Réponse contient le token JWT
```

#### Gestion des Équipements
```bash
# Lister les équipements avec pagination
curl -X GET "http://localhost:8080/api/devices?page=0&size=10&sortBy=name&sortDir=asc" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# Lister tous les équipements (sans pagination)
curl -X GET http://localhost:8080/api/devices \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# Ajouter un équipement
curl -X POST http://localhost:8080/api/devices \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "name": "Router-01",
    "description": "Routeur principal",
    "deviceConfig": {
      "targetIp": "192.168.1.1",
      "snmpVersion": "V2C",
      "communityString": "public"
    }
  }'

# Paramètres de tri disponibles pour les équipements
# id, name, description, status, type, createdAt, updatedAt
```

### Fonctionnalités Principales

#### Surveillance SNMP
- **Polling automatique** toutes les 30 secondes
- **Surveillance des interfaces** réseau
- **Collecte des informations système**
- **Détection des changements d'état**

#### Gestion des Alertes
- **Génération automatique** d'alertes
- **Classification par sévérité**
- **Accusé de réception** et résolution
- **Historique complet**

#### Découverte Réseau
- **Scan de plages IP**
- **Découverte SNMP automatique**
- **Auto-configuration** des équipements

## 🎯 Prochaines Étapes

Une fois l'application démarrée :

1. **Connectez-vous** avec les comptes par défaut
2. **Ajoutez vos équipements** réseau
3. **Configurez les profils** de surveillance
4. **Activez la surveillance** automatique
5. **Consultez les alertes** et métriques
6. **Explorez la documentation API** avec Swagger

## 📞 Support

En cas de problème :
1. Vérifiez les logs de l'application
2. Consultez la section dépannage
3. Vérifiez la connectivité réseau
4. Validez la configuration PostgreSQL

---

**Bon déploiement ! 🚀**
