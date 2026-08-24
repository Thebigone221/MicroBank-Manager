package sn.microbank.service;

import sn.microbank.dao.ClientDAO;
import sn.microbank.dao.PagedResult;
import sn.microbank.model.Statut;
import sn.microbank.model.Client;
import sn.microbank.util.ValidationUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

public class ClientService {

    private final ClientDAO clientDAO = new ClientDAO();

    public Map<String, String> valider(String nom, String prenom, String dateNaissance,
                                       String telephone, String email, String numeroPiece) {
        Map<String, String> erreurs = ValidationUtil.nouvellesErreurs();
        ValidationUtil.requis(erreurs, "nom", nom, "Le nom est obligatoire.");
        ValidationUtil.requis(erreurs, "prenom", prenom, "Le prénom est obligatoire.");
        ValidationUtil.requis(erreurs, "telephone", telephone, "Le téléphone est obligatoire.");
        ValidationUtil.requis(erreurs, "numeroPiece", numeroPiece, "Le numéro de pièce est obligatoire.");
        ValidationUtil.telephone(erreurs, "telephone", telephone);
        ValidationUtil.email(erreurs, "email", email);

        LocalDate naissance = ValidationUtil.date(dateNaissance);
        if (naissance != null && naissance.isAfter(LocalDate.now())) {
            erreurs.put("dateNaissance", "La date de naissance ne peut pas être dans le futur.");
        }
        return erreurs;
    }

    public Client creer(Map<String, String> valeurs) {
        Client client = new Client();
        remplir(client, valeurs);
        client.setDateCreation(LocalDateTime.now());
        client.setStatut(Statut.ACTIF);
        return clientDAO.save(client);
    }

    public Client modifier(Long id, Map<String, String> valeurs) {
        Client client = clientDAO.findById(id);
        if (client == null) {
            throw new ServiceException("Client introuvable.");
        }
        remplir(client, valeurs);
        return clientDAO.update(client);
    }

    private void remplir(Client client, Map<String, String> v) {
        client.setNom(v.get("nom").trim());
        client.setPrenom(v.get("prenom").trim());
        LocalDate naissance = ValidationUtil.date(v.get("dateNaissance"));
        if (naissance != null) {
            client.setDateNaissance(naissance);
        }
        client.setTelephone(v.get("telephone").trim());
        String email = v.get("email");
        client.setEmail((email == null || email.isBlank()) ? null : email.trim());
        String adresse = v.get("adresse");
        client.setAdresse((adresse == null || adresse.isBlank()) ? null : adresse.trim());
        client.setNumeroPiece(v.get("numeroPiece").trim());
    }

    public void modifierSansValidation(Client client) {
        clientDAO.update(client);
    }

    public void supprimer(Long id) {
        Client client = clientDAO.findById(id);
        if (client == null) {
            throw new ServiceException("Client introuvable.");
        }

        if (!client.getAccounts().isEmpty()) {
            throw new ServiceException(
                    "Impossible de supprimer ce client : il possède des comptes. Désactivez-le plutôt.");
        }
        clientDAO.delete(client);
    }

    public Client findById(Long id) {
        return clientDAO.findById(id);
    }

    public boolean existsNumeroPiece(String numeroPiece, Long idExclu) {
        return clientDAO.existsNumeroPiece(numeroPiece, idExclu);
    }

    public PagedResult<Client> lister(String terme, Statut statut, int page, int size) {
        return clientDAO.search(terme, statut, page, size);
    }
}
