# MicroBank Manager - Documentation fonctionnelle et technique

## 1. Présentation

MicroBank Manager est une application web destinée aux institutions de microfinance.
Elle permet de gérer les clients, leurs comptes (courant / épargne), les opérations
(dépôts, retraits, virements) et d'éditer des relevés de compte au format PDF ou CSV.

L'application respecte le pattern **MVC** :

| Couche | Rôle | Technologies |
|---|---|---|
| Modèle | Entités persistantes | JPA 3.1 / Hibernate 6.5 |
| Vue | Affichage | JSP + JSTL 3.0 + Bootstrap 5.3 |
| Contrôleur | Routage et orchestration | Servlets Jakarta EE 10 |

Les accès à la base passent exclusivement par des **DAO** ; toute la logique métier
(transactions, règles de gestion) est concentrée dans la couche **Service**.

## 2. Acteurs et sécurité

### Rôles

- **ADMIN** : toutes les fonctions + gestion des utilisateurs et des agences.
- **AGENT** : clients, comptes, opérations, relevés.

### Mécanismes de sécurité

- Mots de passe **jamais stockés en clair** : hachage SHA-256 à la création.
- Session HTTP : `AuthFilter` intercepte toutes les URL sauf `/login`, `/login.jsp`,
  `/assets/*` et redirige vers la page de connexion si aucun utilisateur n'est en session.
- Les routes `/users*` sont réservées au rôle ADMIN (contrôle dans `AuthFilter`).
- Les vues JSP sont placées sous `WEB-INF/views/` : inaccessible directement,
  uniquement via `RequestDispatcher.forward()`.
- Message flash en session après redirection (succès / erreur).

## 3. Fonctionnalités

### 3.1 Authentification

- Connexion par login/mot de passe (`POST /login`), déconnexion (`/logout`).
- En cas d'échec : message d'erreur sur le formulaire ; en cas de succès : tableau de bord.

### 3.2 Clients

| Action | Route |
|---|---|
| Liste + recherche (nom, prénom, téléphone, pièce) + pagination | `GET /clients` |
| Fiche détaillée avec ses comptes | `GET /clients/details?id=…` |
| Création / modification (validation : nom, prénom, téléphone, pièce **unique**) | `POST /clients/create`, `POST /clients/update` |
| Activation / désactivation | `POST /clients/delete` |
| Upload pièce d'identité *(bonus)* | `POST /clients/upload?id=…` |

La pièce d'identité (image ou PDF, 5 Mo max) est copiée hors de la webapp
(`~/microbank-uploads`) avec un nom non devinable (`client-<id>-piece.pdf`) ;
le chemin est servi par un servlet dédié (`GET /documents/client/<id>`), ce qui
évite toute exécution directe de fichier téléversé.

### 3.3 Comptes

| Action | Route |
|---|---|
| Liste filtrable (recherche, type, statut, agence) + pagination | `GET /accounts` |
| Ouverture avec dépôt initial facultatif | `POST /accounts/create` |
| Fiche du compte : titulaire, agence, solde, dernières opérations | `GET /accounts/details?id=…` |
| Blocage / clôture / réactivation | `GET /accounts/statut?id=…&statut=…` |

Règles :
- L'ouverture crée **le compte ET le dépôt initial dans une seule transaction**
  (si le dépôt échoue, le compte n'est pas créé).
- Numéro de compte généré automatiquement (`MB100001`, `MB100002`, …).
- Aucun retrait ni virement n'est possible sur un compte BLOQUÉ ou CLOTURÉ.

### 3.4 Opérations

| Action | Route |
|---|---|
| Historique global filtrable *(bonus : filtre combiné)* : type, période, agent | `GET /operations?accountId=&type=&du=&au=` |
| Dépôt / retrait | `POST /operations/deposit|withdraw?accountId=…` |
| Virement entre deux comptes | `POST /operations` |

Règles de gestion :
1. Le montant doit être strictement positif.
2. Un retrait exige un solde suffisant.
3. Un virement exige deux comptes distincts, tous deux ACTIFS, et un solde suffisant.
4. Chaque opération met à jour **les soldes et l'historique en une transaction** :
   en cas d'erreur, tout est annulé (`rollback`) - impossible d'avoir un compte
   débité sans crédit correspondant.

Chaque opération enregistre sa référence unique (`OP-00001`, …), son type, son montant,
l'agent responsable et l'horodatage.

### 3.5 Relevés de compte *(bonus 4 et 5)*

| Format | Route |
|---|---|
| PDF (OpenPDF) | `GET /statements/pdf?accountId=…&du=…&au=…` |
| CSV (séparateur `;`, BOM UTF-8 pour Excel) | `GET /statements/csv?accountId=…` |
| Version imprimable (`window.print()`, CSS `@media print`) | `GET /statements/print?accountId=…` |

Le relevé contient : identité du client, numéro et type de compte, période,
liste des opérations signées (+/-), totaux dépôts/retraits et solde final.

### 3.6 Tableau de bord enrichi *(bonus 2)*

Statistiques temps réel : nombre de clients (par statut), comptes actifs/bloqués,
solde total de l'institution, opérations du jour, dernières opérations.

### 3.7 Agences *(bonus 3)*

CRUD complet (ADMIN) : code unique, nom, adresse, téléphone. Chaque compte est
rattaché à une agence, ce qui permet le filtre par agence sur la liste des comptes.

### 3.8 Gestion des utilisateurs (ADMIN)

Création, modification, activation/désactivation des agents et administrateurs.
Un utilisateur désactivé ne peut plus se connecter. Suppression refusée s'il reste
des opérations rattachées à cet utilisateur (intégrité historique).

## 4. Architecture technique

### 4.1 Structure du projet

```
src/main/java/sn/microbank/
├── model/        User, Client, Account, Operation, Agency + énumérations
├── dao/          GenericDAO<T> + UserDAO, ClientDAO, AccountDAO, OperationDAO, AgencyDAO
├── service/      AuthService, ClientService, AccountService, OperationService,
│                 UserService, AgencyService, StatementService (+ StatementPdf)
├── controller/   9 servlets + AuthFilter + CharsetFilter
└── util/         FormatUtil, ValidationUtil, Flash, HashUtil, ServletUtil
```

### 4.2 Persistance

- Un seul `EntityManagerFactory` partagé (`persistence.xml`, unité `microbankPU`).
- `GenericDAO` centralise : `inTransaction(fn)` (commit/rollback automatique),
  `inTransactionVoid(fn)` pour les écritures, `inRead(fn)` pour les lectures.
- Requêtes JPQL paramétrées (**aucune concaténation** → pas d'injection SQL).
- Pagination SQL réelle (`setFirstResult` / `setMaxResults`) + comptage séparé.

### 4.3 Vues

- Fragment communs `header.jspf` / `footer.jspf` inclus statiquement.
- Taglibs JSTL 3.0 (`jakarta.tags.core`) et fonctions EL maison
  (`WEB-INF/functions.tld`) : `f:fcfa()` format monétaire FCFA,
  `f:nombre()` séparateur de milliers, `f:dateFr()` / `f:dateHeureFr()`.
- Bootstrap 5.3 et icônes servis localement (`assets/`) : l'application fonctionne
  hors ligne.

### 4.4 Flux d'une requête

```
Navigateur ──► CharsetFilter ──► AuthFilter (session + rôle)
           ──► Servlet (validation entrée)
           ──► Service (règles métier, @transaction manuelle via GenericDAO)
           ──► DAO (JPQL/JPA)
           ──◄ entités / DTO
Servlet ──forward──► WEB-INF/views/**.jsp ──► HTML
```

## 5. Base de données

5 tables générées par Hibernate :

| Table | Colonnes principales |
|---|---|
| `app_user` | id, login (unique), mot_de_passe (SHA-256), nom_complet, role (ADMIN/AGENT), actif |
| `client` | id, nom, prenom, telephone, numero_piece (unique), piece_identite, statut, date_creation… |
| `account` | id, numero_compte (unique), type (COURANT/EPARGNE), statut, solde, client_id → client, agency_id → agency |
| `operation` | id, reference (unique), type (DEPOT/RETRAIT/VIREMENT), montant, description, date_operation, compte_id → account, compte_destination_id → account, agent_id → app_user |
| `agency` | id, code (unique), nom, adresse, telephone |

Relations : un client possède plusieurs comptes ; un compte a plusieurs opérations ;
une opération référence un compte source, éventuellement un compte destination et l'agent.

`database.sql` fournit un jeu de données de démonstration prêt à l'emploi.

## 6. Installation

Voir le [README](../README.md#démarrage-rapide) : création de la base MySQL,
import de `database.sql`, construction Maven (`mvn clean package`),
déploiement du WAR dans Tomcat 10.1, démarrage sur <http://localhost:8080/microbank/>.

Comptes de test : `admin/admin123` (ADMIN), `agent/agent123` (AGENT).
