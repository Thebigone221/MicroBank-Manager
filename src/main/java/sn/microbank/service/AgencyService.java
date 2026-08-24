package sn.microbank.service;

import sn.microbank.dao.AgencyDAO;
import sn.microbank.model.Agency;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * Service de gestion des agences (Bonus 4).
 */
public class AgencyService {

    private static final Pattern CODE_VALIDE = Pattern.compile("^[A-Za-z0-9]{2,10}$");

    private final AgencyDAO agencyDAO = new AgencyDAO();

    public Map<String, String> valider(String code, String nom, String ville) {
        Map<String, String> erreurs = sn.microbank.util.ValidationUtil.nouvellesErreurs();
        if (code == null || code.isBlank()) {
            erreurs.put("code", "Le code est obligatoire.");
        } else if (!CODE_VALIDE.matcher(code.trim()).matches()) {
            erreurs.put("code", "Code invalide : 2 à 10 caractères alphanumériques.");
        }
        sn.microbank.util.ValidationUtil.requis(erreurs, "nom", nom, "Le nom est obligatoire.");
        return erreurs;
    }

    public Agency creer(String code, String nom, String ville) {
        if (agencyDAO.existsCode(code, null)) {
            throw new ServiceException("Ce code agence est déjà utilisé.");
        }
        return agencyDAO.save(new Agency(code.trim().toUpperCase(), nom.trim(),
                ville == null || ville.isBlank() ? null : ville.trim()));
    }

    public Agency modifier(Long id, String code, String nom, String ville) {
        Agency agence = agencyDAO.findById(id);
        if (agence == null) {
            throw new ServiceException("Agence introuvable.");
        }
        if (agencyDAO.existsCode(code, id)) {
            throw new ServiceException("Ce code agence est déjà utilisé par une autre agence.");
        }
        agence.setCode(code.trim().toUpperCase());
        agence.setNom(nom.trim());
        agence.setVille(ville == null || ville.isBlank() ? null : ville.trim());
        return agencyDAO.update(agence);
    }

    /** Une agence ne peut être supprimée que si aucun compte n'y est rattaché. */
    public void supprimer(Long id) {
        Agency agence = agencyDAO.findById(id);
        if (agence == null) {
            throw new ServiceException("Agence introuvable.");
        }
        if (!agence.getAccounts().isEmpty()) {
            throw new ServiceException(
                    "Impossible de supprimer cette agence : des comptes y sont encore rattachés.");
        }
        agencyDAO.delete(agence);
    }

    public Agency findById(Long id) {
        return agencyDAO.findById(id);
    }

    public java.util.List<Agency> toutes() {
        return agencyDAO.findAllOrdered();
    }
}
