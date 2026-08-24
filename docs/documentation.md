# MicroBank Manager - Documentation fonctionnelle et technique

Cette note décrit ce que fait l'application, comment c'est construit, et pourquoi. Le README couvre l'installation ; ici on rentre dans le détail.

## 1. De quoi il s'agit

Une application web pour le guichet d'une institution de microfinance : inscrire des clients, ouvrir et gérer leurs comptes (courant ou épargne), enregistrer dépôts, retraits et virements, et sortir un relevé de compte propre quand le client en demande un.

Côté architecture, j'ai suivi le pattern MVC demandé, avec une séparation stricte entre les couches :

- les **servlets** reçoivent les requêtes, valident la saisie et choisissent la vue ;
- les **services** portent les règles de gestion et ouvrent les transactions ;
- les **DAO** parlent à la base via JPA, rien d'autre ;
- les **JSP** affichent, elles ne calculent rien de métier.

Les vues ne touchent jamais à l'EntityManager. C'est la règle que je me suis fixée dès le départ et elle a payé quand il a fallu changer des règles métier sans toucher aux JSP.

## 2. Qui peut faire quoi

Deux rôles seulement, ça suffit pour ce périmètre :

- **ADMIN** : tout, plus la gestion des utilisateurs du système et des agences ;
- **AGENT** : le quotidien du guichet, clients, comptes, opérations, relevés.

La sécurité repose sur plusieurs niveaux :

- Mots de passe hachés avec **PBKDF2-HMAC-SHA256**, sel aléatoire de 16 octets, 120 000 itérations. Le format stocké est `pbkdf2$iterations$salt$hash`. Les anciennes empreintes SHA-256 sont migrées automatiquement lors de la première connexion réussie.
- Protection contre le **force brute** : après 5 échecs pour un même couple login/adresse IP, blocage de 10 minutes.
- La session est recréée après authentification (protection contre la fixation de session), cookie `HttpOnly` + `SameSite=Lax`, expiration au bout de 30 minutes d'inactivité.
- Un filtre (`AuthFilter`) intercepte toutes les URL sauf la page de connexion et les ressources statiques : sans session valide, direction le formulaire de login. Les routes `/users*` exigent en plus le rôle ADMIN.
- En-têtes HTTP de sécurité posés par `CharsetFilter` : `X-Content-Type-Options: nosniff`, `X-Frame-Options: DENY`, `Referrer-Policy: no-referrer`, et une CSP qui interdit tout script extérieur au domaine.
- Toutes les valeurs saisies par les utilisateurs sont échappées dans les JSP avec `fn:escapeXml`.
- Les JSP vivent sous `WEB-INF/views/`, donc inaccessibles en direct ; seuls les forwards du contrôleur y mènent.

## 3. Les fonctionnalités en détail

### 3.1 Connexion

Formulaire classique (`POST /login`). Message générique en cas d'échec, volontairement : pas besoin d'aider un attaquant à deviner si le login existe. Après succès, redirection vers le tableau de bord avec un message flash.

### 3.2 Clients

| Action | Route |
|---|---|
| Liste, recherche (nom, prénom, téléphone, pièce), pagination | `GET /clients` |
| Fiche détaillée avec ses comptes | `GET /clients/details?id=…` |
| Création / modification | `POST /clients/create`, `POST /clients/update` |
| Activation / désactivation | `POST /clients/delete` |
| Upload de la pièce d'identité *(bonus)* | `POST /clients/upload?id=…` |

La validation refuse un numéro de pièce déjà utilisé : c'est l'identifiant réel du client dans une institution de microfinance, deux personnes ne doivent jamais partager le même dossier.

Pour la pièce d'identité (bonus), quelques précautions qui me tenaient à cœur :

- le serveur vérifie la **signature binaire** du fichier (PDF `%PDF`, PNG ou JPEG) plutôt que le type MIME annoncé par le navigateur, qui se falsifie en une ligne de curl ;
- taille limitée à 5 Mo ;
- le fichier part dans un dossier hors de la webapp (`~/microbank-uploads`) sous un nom imposé par le serveur (`client-<id>-piece.pdf`), jamais celui fourni par l'utilisateur ;
- le téléchargement passe par un servlet dédié (`GET /documents/client/<id>`) qui contrôle la session, donc pas de lien direct vers un fichier.

### 3.3 Comptes

| Action | Route |
|---|---|
| Liste filtrable (recherche, type, statut, agence) + pagination | `GET /accounts` |
| Ouverture avec dépôt initial facultatif | `POST /accounts/create` |
| Fiche du compte | `GET /accounts/details?id=…` |
| Blocage / clôture / réactivation | `GET /accounts/statut?id=…&statut=…` |

Points importants :

- l'ouverture crée le compte **et** son dépôt initial dans la même transaction : si le dépôt échoue, le compte n'existe pas ;
- les numéros sont générés côté serveur (MB100001, MB100002...) ;
- sur un compte BLOQUÉ ou CLOTURÉ, aucune opération n'est acceptée.

### 3.4 Opérations

| Action | Route |
|---|---|
| Historique filtrable : compte, type, période, montants *(bonus)* | `GET /operations?accountId=&type=&du=&au=` |
| Dépôt / retrait | `POST /operations/deposit|withdraw` |
| Virement | `POST /operations` |

Les règles de gestion, telles quelles :

1. montant strictement positif ;
2. un retrait exige un solde suffisant ;
3. un virement exige deux comptes différents, tous les deux ACTIFS, et un solde suffisant côté source ;
4. chaque opération écrit l'historique **et** met à jour les soldes dans une seule transaction, rollback complet au moindre problème.

Chaque opération porte une référence unique (OP-00001, OP-00002...), le type, le montant, l'agent responsable et l'horodatage. C'est ce qui permet de répondre à la question "qui a encaissé quoi" sans fouiller les logs.

### 3.5 Relevés (bonus)

Trois sorties pour le même document :

- **PDF** via OpenPDF : `GET /statements/pdf?accountId=…&du=…&au=…` ;
- **CSV** avec séparateur `;` et BOM UTF-8, pour qu'il s'ouvre correctement dans Excel : `GET /statements/csv?accountId=…` ;
- **version imprimable** qui appelle `window.print()` avec un CSS `@media print` : `GET /statements/print`.

Le contenu est identique partout : identité du client, compte, période, opérations signées (+/-), totaux dépôts et retraits, solde final. J'ai choisi OpenPDF plutôt qu'iText simplement parce qu'iText est sous licence AGV et OpenPDF reprend la dernière version libre.

### 3.6 Tableau de bord (bonus)

Ce qu'un responsable veut voir en arrivant le matin : nombre de clients, comptes actifs et bloqués, encours total, opérations du jour (dépôts et retraits), répartition courant/épargne, et les dernières écritures.

### 3.7 Agences (bonus)

CRUD réservé à l'ADMIN : code unique, nom, ville. Chaque compte est rattaché à une agence, ce qui alimente le filtre par agence sur la liste des comptes.

### 3.8 Utilisateurs (ADMIN)

Création, modification, activation et désactivation des comptes utilisateurs. Un utilisateur désactivé ne peut plus se connecter, mais ses opérations passées restent intactes : la suppression directe est refusée s'il existe des opérations rattachées à lui.

## 4. Vue technique

### 4.1 Organisation du code

```
src/main/java/sn/microbank/
├── model/        User, Client, Account, Operation, Agency + énumérations
├── dao/          GenericDAO<T> + un DAO par entité
├── service/      AuthService, ClientService, AccountService, OperationService,
│                 UserService, AgencyService, StatementService (+ StatementPdf)
├── controller/   9 servlets + AuthFilter + CharsetFilter
└── util/         FormatUtil, ValidationUtil, Flash, HashUtil, ServletUtil
```

### 4.2 Accès aux données

Une seule `EntityManagerFactory`, créée une fois pour toute la vie de l'application. Le `GenericDAO` expose trois entrées :

- `inTransaction(fn)` : ouvre, exécute, commit, rollback en cas d'exception ;
- `inTransactionVoid(fn)` : la même chose pour les traitements sans retour ;
- `inRead(fn)` : lecture simple hors transaction.

Toutes les requêtes sont en JPQL paramétré, il n'y a pas une seule concaténation de chaîne dans une requête. La pagination se fait en SQL (`setFirstResult`/`setMaxResults`) avec un comptage séparé, donc pas de chargement de tables entières en mémoire.

### 4.3 Les vues

Un `header.jspf` et un `footer.jspf` inclus statiquement dans chaque page. Quelques fonctions EL maison déclarées dans `WEB-INF/functions.tld` : formatage monétaire FCFA, séparateurs de milliers, dates en français. Bootstrap et ses icônes sont embarqués dans `assets/`, l'application marche sans Internet.

Le flux complet d'une requête :

```
Navigateur → CharsetFilter → AuthFilter → Servlet → Service → DAO
                                                        ↕ MySQL
Servlet → forward → WEB-INF/views/** → HTML renvoyé
```

## 5. Base de données

Cinq tables, générées par Hibernate :

| Table | Contenu principal |
|---|---|
| `app_user` | login unique, mot_de_passe (empreinte PBKDF2), nom, rôle, statut |
| `client` | identité, téléphone, numero_piece unique, pièce d'identité, statut |
| `account` | numero_compte unique, type COURANT/EPARGNE, statut, solde, client, agence |
| `operation` | référence unique, type DEPOT/RETRAIT/VIREMENT, montant, description, date, compte source, destination éventuelle, agent |
| `agency` | code unique, nom, ville |

Un client a plusieurs comptes, un compte a plusieurs opérations, une opération pointe vers son compte source, parfois un compte destination (virement) et toujours vers l'agent qui l'a saisie.

Le fichier `database.sql` recharge un jeu de démonstration cohérent : soldes recalculés à partir des opérations, dates étalées sur plusieurs mois, accents compris.

## 6. Difficultés rencontrées

Quelques pièges tombés en cours de route, pour mémoire :

- **EL 5 et les records Java** : les JSP affichaient des champs vides sur certaines pages. Cause : l'EL ne lit pas les records (pas de getter `getItems()`, juste `items()`). Remplacement par des classes classiques.
- **LazyInitializationException** : la fiche client parcourait `client.getAccounts()` après fermeture du contexte de persistance. Associations passées en EAGER, vu le faible volume de données c'est le bon compromis.
- **Accents cassés en base** : les imports SQL passés par le pipe PowerShell écrivaient des `?` à la place des accents. Réglé en important avec `mysql --default-character-set=utf8mb4` et en forçant l'encodage dans l'URL JDBC.
- **Routage `/clients/*`** : `getServletPath()` ne contient pas le sous-chemin, d'où des 404 mystérieux au début. Tous les servlets concatènent désormais `getPathInfo()`.
