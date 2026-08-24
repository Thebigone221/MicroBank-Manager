# MicroBank Manager

Application web de gestion pour une institution de microfinance, développée en **Java Jakarta EE** selon le pattern **MVC** : Servlets, JSP/JSTL, HttpSession, JPA/Hibernate et Bootstrap 5.

## Fonctionnalités

- **Authentification sécurisée** : mots de passe hachés SHA-256, sessions HTTP, rôles ADMIN / AGENT
- **Clients** : création, modification, recherche multi-critères, pagination, activation/désactivation
- **Comptes** : ouverture avec dépôt initial atomique, blocage/clôture, filtres par type/statut/agence
- **Opérations** : dépôts, retraits, virements - chaque opération est **une transaction unique** (tout ou rien), contrôle du solde et du statut du compte
- **Relevés de compte** : PDF (OpenPDF), export CSV compatible Excel, version imprimable
- **Bonus** : upload de la pièce d'identité (5 Mo max), filtre combiné des opérations, tableau de bord enrichi avec statistiques, gestion des agences rattachées aux comptes, relevé imprimable

## Technologies

| Élément | Choix |
|---|---|
| Java | 17 |
| Serveur | Apache Tomcat 10.1 (Jakarta EE 10) |
| Vues | JSP + JSTL 3.0 (`jakarta.tags.core`) |
| Persistance | JPA 3.1 / Hibernate 6.5 |
| Base de données | MySQL 8 |
| Front-end | Bootstrap 5.3 + Bootstrap Icons (locaux) |
| Build | Maven (WAR) |

## Démarrage rapide

### 1. Prérequis

- JDK 17+
- Maven 3.9+
- MySQL 8 en cours d'exécution sur `localhost:3306`

### 2. Créer la base de données

```sql
CREATE DATABASE microbank CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'microbank'@'localhost' IDENTIFIED BY 'microbank123';
GRANT ALL PRIVILEGES ON microbank.* TO 'microbank'@'localhost';
FLUSH PRIVILEGES;
```

Puis importer les données de démonstration :

```bash
mysql -u root microbank < database.sql
```

> Le schéma est créé automatiquement au premier démarrage (`hibernate.hbm2ddl.auto=update`).
> `database.sql` fournit les données de test (utilisateurs, agences, clients, comptes, opérations).

### 3. Construire et déployer

```bash
mvn clean package
cp target/microbank.war $CATALINA_HOME/webapps/
$CATALINA_HOME/bin/catalina.sh start        # Windows : catalina.bat start
```

Application disponible sur <http://localhost:8080/microbank/>

### Comptes de démonstration

| Login | Mot de passe | Rôle |
|---|---|---|
| admin | admin123 | ADMIN (accès utilisateurs + agences) |
| agent | agent123 | AGENT |

## Architecture

```
src/main/java/sn/microbank/
├── model/       Entités JPA : User, Client, Account, Operation, Agency (+ énumérations)
├── dao/         GenericDAO (EntityManagerFactory partagée) + DAO par entité
├── service/     Logique métier et transactions (AuthService, OperationService…)
├── controller/  Servlets (une route = un servlet) + filtres AuthFilter/CharsetFilter
└── util/        FormatUtil, ValidationUtil, Flash, HashUtil, ServletUtil

src/main/webapp/
├── WEB-INF/views/   Vues JSP protégées (accès uniquement via forward)
├── includes/        header.jspf / footer.jspf
├── assets/          Bootstrap, icônes et style.css locaux
└── WEB-INF/functions.tld   Fonctions EL personnalisées (format FCFA, dates FR)
```

Flux d'une requête : **Filtre auth → Servlet → Service (transaction) → DAO (JPA) → forward JSP**.

## Documentation

La documentation fonctionnelle et technique complète se trouve dans [`docs/documentation.md`](docs/documentation.md).

## Auteur

Projet de fin de module - Développement Web (Java EE).
