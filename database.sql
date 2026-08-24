CREATE DATABASE IF NOT EXISTS microbank CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE microbank;

CREATE USER IF NOT EXISTS 'microbank'@'localhost' IDENTIFIED BY 'microbank123';
GRANT ALL PRIVILEGES ON microbank.* TO 'microbank'@'localhost';
FLUSH PRIVILEGES;

CREATE TABLE IF NOT EXISTS app_user (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    nom           VARCHAR(60)  NOT NULL,
    prenom        VARCHAR(60)  NOT NULL,
    login         VARCHAR(40)  NOT NULL,
    mot_de_passe  VARCHAR(64)  NOT NULL COMMENT 'Hash SHA-256',
    role          VARCHAR(10)  NOT NULL COMMENT 'AGENT / ADMIN',
    statut        VARCHAR(10)  NOT NULL COMMENT 'ACTIF / INACTIF',
    date_creation DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_login (login)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS agency (
    id   BIGINT      NOT NULL AUTO_INCREMENT,
    code VARCHAR(10) NOT NULL,
    nom  VARCHAR(60) NOT NULL,
    ville VARCHAR(60),
    PRIMARY KEY (id),
    UNIQUE KEY uk_agency_code (code)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS client (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    nom            VARCHAR(60)  NOT NULL,
    prenom         VARCHAR(60)  NOT NULL,
    date_naissance DATE,
    telephone      VARCHAR(20)  NOT NULL,
    email          VARCHAR(80),
    adresse        VARCHAR(150),
    numero_piece   VARCHAR(40)  NOT NULL,
    piece_identite VARCHAR(255) COMMENT 'Fichier uploadé (Bonus 1)',
    date_creation  DATETIME(6)  NOT NULL,
    statut         VARCHAR(10)  NOT NULL COMMENT 'ACTIF / INACTIF',
    PRIMARY KEY (id),
    UNIQUE KEY uk_client_numero_piece (numero_piece)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS account (
    id              BIGINT        NOT NULL AUTO_INCREMENT,
    numero_compte   VARCHAR(20)   NOT NULL,
    type            VARCHAR(10)   NOT NULL COMMENT 'COURANT / EPARGNE',
    solde           DECIMAL(15,2) NOT NULL,
    date_ouverture  DATE          NOT NULL,
    statut          VARCHAR(10)   NOT NULL COMMENT 'ACTIF / BLOQUE / CLOTURE',
    client_id       BIGINT        NOT NULL,
    agency_id       BIGINT,
    PRIMARY KEY (id),
    UNIQUE KEY uk_account_numero (numero_compte),
    CONSTRAINT fk_account_client FOREIGN KEY (client_id) REFERENCES client (id),
    CONSTRAINT fk_account_agency FOREIGN KEY (agency_id) REFERENCES agency (id)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS operation (
    id                    BIGINT        NOT NULL AUTO_INCREMENT,
    reference             VARCHAR(20)   NOT NULL,
    type                  VARCHAR(10)   NOT NULL COMMENT 'DEPOT / RETRAIT / VIREMENT',
    montant               DECIMAL(15,2) NOT NULL,
    date_operation        DATETIME(6)   NOT NULL,
    description           VARCHAR(200),
    compte_id             BIGINT        NOT NULL,
    compte_destination_id BIGINT,
    agent_id              BIGINT        NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_operation_reference (reference),
    CONSTRAINT fk_operation_compte FOREIGN KEY (compte_id) REFERENCES account (id),
    CONSTRAINT fk_operation_compte_dest FOREIGN KEY (compte_destination_id) REFERENCES account (id),
    CONSTRAINT fk_operation_agent FOREIGN KEY (agent_id) REFERENCES app_user (id)
) ENGINE = InnoDB;

INSERT INTO app_user (nom, prenom, login, mot_de_passe, role, statut, date_creation) VALUES
('GAYE',    'Abdoulaye', 'admin', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'ADMIN', 'ACTIF', NOW()),
('DIOP',    'Awa',       'agent', 'f44d1ac9bf0c69b083380b86dbdf3b73797150e3cca4820ac399f7917e607647', 'AGENT', 'ACTIF', NOW());

INSERT INTO agency (code, nom, ville) VALUES
('DAK01', 'Agence Plateau',   'Dakar'),
('THI02', 'Agence Thiès Nord','Thiès');

INSERT INTO client (nom, prenom, date_naissance, telephone, email, adresse, numero_piece, date_creation, statut) VALUES
('GAYE', 'Fatou',     '1990-04-12', '77 512 34 56', 'fatou.gaye@mail.com',  'Sacré-Coeur 3, Dakar',   '1234199000123', NOW(), 'ACTIF'),
('DIOP', 'Moussa',    '1985-09-25', '76 445 88 21', 'moussa.diop@mail.com', 'Guédiawaye, Dakar',      '1234198500456', NOW(), 'ACTIF'),
('NDIAYE','Aminata',  '1995-01-08', '70 123 45 67', 'aminata.n@mail.com',   'Grand Yoff, Dakar',      '1234199500789', NOW(), 'ACTIF');

INSERT INTO account (numero_compte, type, solde, date_ouverture, statut, client_id, agency_id) VALUES
('MB100001', 'COURANT', 130000, CURDATE(), 'ACTIF', 1, 1),
('MB100002', 'EPARGNE', 500000, CURDATE(), 'ACTIF', 2, 1),
('MB100003', 'COURANT', 75000,  CURDATE(), 'ACTIF', 3, 2);

INSERT INTO operation (reference, type, montant, date_operation, description, compte_id, compte_destination_id, agent_id) VALUES
('OP-00001', 'DEPOT',   150000, NOW(), 'Dépôt initial à l''ouverture du compte', 1, NULL, 1),
('OP-00002', 'RETRAIT', 20000,  NOW(), 'Retrait en espèces',                     1, NULL, 1),
('OP-00003', 'DEPOT',   500000, NOW(), 'Dépôt initial à l''ouverture du compte', 2, NULL, 2),
('OP-00004', 'VIREMENT',25000,  NOW(), 'Virement → MB100001',                    2, 1,    2),
('OP-00005', 'DEPOT',   100000, NOW(), 'Dépôt initial à l''ouverture du compte', 3, NULL, 1),
('OP-00006', 'RETRAIT', 25000,  NOW(), 'Retrait au guichet',                     3, NULL, 1);
