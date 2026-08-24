USE microbank;

CREATE USER IF NOT EXISTS 'microbank'@'localhost' IDENTIFIED BY 'microbank123';
GRANT ALL PRIVILEGES ON microbank.* TO 'microbank'@'localhost';
FLUSH PRIVILEGES;

INSERT INTO app_user (nom, prenom, login, mot_de_passe, role, statut, date_creation) VALUES
('GAYE', 'Abdoulaye', 'admin', 'pbkdf2$120000$AgHX4Ytsb9FVCvSt1OVQ9A==$1mTwJQBOGYb+odpvxXjnvsM+bOHqqXGlo+HmOziatJ8=', 'ADMIN', 'ACTIF', '2026-01-05 08:32:00'),
('DIOP', 'Awa',       'agent', 'pbkdf2$120000$oBtLQR99ci0ro62DWd6Fjg==$bd1qZsI2ShNl1BcEPbUR+pdB/EfxZXJY9+xW+FkcsWo=', 'AGENT', 'ACTIF', '2026-02-16 09:14:00');

INSERT INTO agency (code, nom, ville) VALUES
('DAK01', 'Agence Plateau',    'Dakar'),
('THI02', 'Agence Thiès Nord', 'Thiès');

INSERT INTO client (nom, prenom, date_naissance, telephone, email, adresse, numero_piece, date_creation, statut) VALUES
('GAYE',    'Fatou',   '1990-04-12', '77 512 34 56', 'fatou.gaye@mail.com',   'Sacré-Coeur 3, Dakar', '1234199000123', '2026-03-04 10:17:00', 'ACTIF'),
('DIOP',    'Moussa',  '1985-09-25', '76 445 88 21', 'moussa.diop@mail.com',  'Cité Gadaye, Dakar',   '1234198500456', '2026-03-27 15:42:00', 'ACTIF'),
('NDIAYE',  'Aminata', '1995-01-08', '70 123 45 67', 'aminata.ndiaye@mail.com','Grand Yoff, Dakar',    '1234199500789', '2026-05-12 11:06:00', 'ACTIF');

INSERT INTO account (numero_compte, type, solde, date_ouverture, statut, client_id, agency_id) VALUES
('MB100001', 'COURANT', 140000.00, '2026-03-10', 'ACTIF', 1, 1),
('MB100002', 'EPARGNE', 475000.00, '2026-04-02', 'ACTIF', 2, 1),
('MB100003', 'COURANT', 115000.00, '2026-06-01', 'ACTIF', 3, 2);

INSERT INTO operation (reference, type, montant, date_operation, description, compte_id, compte_destination_id, agent_id) VALUES
('OP-00001', 'DEPOT',    150000.00, '2026-03-10 09:12:00', 'Dépôt initial à l''ouverture du compte', 1, NULL, 1),
('OP-00002', 'RETRAIT',   20000.00, '2026-03-24 14:47:00', 'Retrait en espèces au guichet',           1, NULL, 1),
('OP-00003', 'DEPOT',    500000.00, '2026-04-02 10:31:00', 'Dépôt initial à l''ouverture du compte', 2, NULL, 2),
('OP-00004', 'VIREMENT',  25000.00, '2026-04-28 15:08:00', 'Virement vers le compte MB100001',       2, 1,    2),
('OP-00005', 'DEPOT',    100000.00, '2026-06-01 09:55:00', 'Dépôt initial à l''ouverture du compte', 3, NULL, 1),
('OP-00006', 'RETRAIT',   25000.00, '2026-06-19 11:26:00', 'Retrait frais de scolarité',             3, NULL, 2),
('OP-00007', 'DEPOT',     40000.00, '2026-07-07 16:42:00', 'Épargne mensuelle',                      3, NULL, 1),
('OP-00008', 'RETRAIT',   15000.00, '2026-07-21 10:19:00', 'Retrait DAB Lambaréné',                  1, NULL, 2);
