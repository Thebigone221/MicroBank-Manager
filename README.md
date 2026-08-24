# MicroBank Manager

Application web de gestion pour une institution de microfinance, écrite en Java avec Jakarta EE (Servlets, JSP/JSTL, JPA/Hibernate) et un peu de Bootstrap pour l'habillage. C'est mon projet de fin de module de développement web.

L'idée : un guichet de microfinance peut ouvrir des comptes, encaisser des dépôts, payer des retraits et faire des virements entre clients, avec un vrai suivi de qui a fait quoi et quand.

## Ce que fait l'application

- Connexion avec deux rôles : ADMIN (tout) et AGENT (guichet)
- Gestion des clients : fiche complète, recherche multi-critères, pagination
- Ouverture de comptes courants ou d'épargne, blocage, clôture
- Dépôts, retraits et virements, chacun protégé par une transaction unique : si quelque chose échoue en cours de route, rien n'est écrit
- Relevé de compte exportable en PDF ou CSV, plus une version imprimable
- En bonus : upload de la pièce d'identité du client, filtre combiné sur l'historique des opérations, statistiques sur le tableau de bord, gestion des agences

## Environnement technique

| Élément | Choix |
|---|---|
| Java | 17 |
| Serveur | Tomcat 10.1 (Jakarta EE 10) |
| Vues | JSP + JSTL 3.0 |
| Persistance | JPA 3.1 / Hibernate 6.5 |
| Base de données | MySQL 8 |
| Front-end | Bootstrap 5.3 + Bootstrap Icons, servis localement |
| Build | Maven |

Bootstrap est embarqué dans le WAR volontairement : tout tourne hors ligne, pas besoin d'accès Internet le jour de la démo.

## Installation

Il faut un JDK 17, Maven, et MySQL qui tourne sur `localhost:3306`.

1. Créer la base :

```sql
CREATE DATABASE microbank CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'microbank'@'localhost' IDENTIFIED BY 'microbank123';
GRANT ALL PRIVILEGES ON microbank.* TO 'microbank'@'localhost';
FLUSH PRIVILEGES;
```

2. Importer les données de démonstration :

```bash
mysql --default-character-set=utf8mb4 -u root microbank < database.sql
```

Le flag `utf8mb4` n'est pas décoratif : sans lui, les accents passent mal selon la configuration du client MySQL. Le schéma, lui, est créé automatiquement par Hibernate au premier démarrage.

3. Construire et déployer :

```bash
mvn clean package
cp target/microbank.war $CATALINA_HOME/webapps/
$CATALINA_HOME/bin/catalina.sh start
```

L'application répond sur <http://localhost:8080/microbank/>

## Comptes de démonstration

| Login | Mot de passe | Rôle |
|---|---|---|
| admin | admin123 | ADMIN |
| agent | agent123 | AGENT |

Les mots de passe sont stockés hachés avec PBKDF2 (sel aléatoire, 120 000 itérations), donc impossible de les récupérer depuis la base. Le jeu de données `database.sql` contient déjà leurs empreintes.

## Comment c'est organisé

```
src/main/java/sn/microbank/
├── model/       Entités JPA : User, Client, Account, Operation, Agency
├── dao/         GenericDAO + un DAO par entité
├── service/     Toute la logique métier et les transactions
├── controller/  Servlets + filtres (authentification, encodage)
└── util/        Outils : validation, formatage, hachage, messages flash

src/main/webapp/
├── WEB-INF/views/   Les JSP, inaccessibles directement depuis le navigateur
├── includes/        header et footer communs
├── assets/          CSS, polices et icônes locaux
└── WEB-INF/functions.tld   Fonctions EL maison (format FCFA, dates en français)
```

Une requête traverse : filtre d'authentification → servlet → service (transaction) → DAO → forward vers la JSP. Les servlets restent minces, ils délègent tout le travail aux services.

## Quelques choix et difficultés

- **Transactions maison** : j'ai écrit un petit `GenericDAO` avec trois méthodes (`inTransaction`, `inTransactionVoid`, `inRead`) plutôt que d'exposer l'EntityManager partout. Un virement = une transaction, point.
- **EL et records** : les JSP ne savent pas lire les records Java (EL 5 ne passe pas par les composants d'accès canoniques). Deux classes intermédiaires ont dû être transformées en classes classiques avec getters.
- **Chargement paresseux** : premières pages affichées, puis `LazyInitializationException` dans les vues qui parcourent `client.getAccounts()`. Résolu en passant les associations concernées en `FetchType.EAGER`, le volume de données le permet largement.
- **Routage des servlets** : avec `@WebServlet("/clients/*")`, `getServletPath()` ne renvoie jamais le sous-chemin, il faut concaténer `getPathInfo()`. Erreur classique, corrigée dans chaque servlet.
- **Upload de fichiers** : le type MIME déclaré par le navigateur étant falsifiable, le serveur lit les premiers octets du fichier (signature PDF/PNG/JPEG) avant de décider. Les fichiers sont stockés hors de la webapp et servis par un servlet dédié.

La documentation détaillée (règles de gestion, routes, schéma de base) est dans [`docs/documentation.md`](docs/documentation.md).

## Auteur

Projet de fin de module, Développement Web (Java EE).
