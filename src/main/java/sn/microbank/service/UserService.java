package sn.microbank.service;

import sn.microbank.dao.PagedResult;
import sn.microbank.dao.UserDAO;
import sn.microbank.model.Role;
import sn.microbank.model.Statut;
import sn.microbank.model.User;
import sn.microbank.util.HashUtil;
import sn.microbank.util.ValidationUtil;

import java.util.Map;

/**
 * Service de gestion des utilisateurs (réservé à l'administrateur).
 */
public class UserService {

    private final UserDAO userDAO = new UserDAO();

    public Map<String, String> valider(String nom, String prenom, String login,
                                       String motDePasse, String role) {
        Map<String, String> erreurs = ValidationUtil.nouvellesErreurs();
        ValidationUtil.requis(erreurs, "nom", nom, "Le nom est obligatoire.");
        ValidationUtil.requis(erreurs, "prenom", prenom, "Le prénom est obligatoire.");
        ValidationUtil.requis(erreurs, "login", login, "Le login est obligatoire.");
        if (role == null || role.isBlank()) {
            erreurs.put("role", "Veuillez choisir un rôle.");
        }
        return erreurs;
    }

    public User creer(String nom, String prenom, String login, String motDePasse,
                      String roleStr) {
        if (userDAO.existsLogin(login, null)) {
            throw new ServiceException("Ce login est déjà utilisé.");
        }
        if (motDePasse == null || motDePasse.length() < 6) {
            throw new ServiceException("Le mot de passe doit contenir au moins 6 caractères.");
        }
        Role role = Role.valueOf(roleStr);
        User user = new User(nom.trim(), prenom.trim(), login.trim(),
                HashUtil.sha256(motDePasse), role);
        return userDAO.save(user);
    }

    public User modifier(Long id, String nom, String prenom, String login,
                         String motDePasse, String roleStr) {
        User user = userDAO.findById(id);
        if (user == null) {
            throw new ServiceException("Utilisateur introuvable.");
        }
        if (userDAO.existsLogin(login, id)) {
            throw new ServiceException("Ce login est déjà utilisé par un autre utilisateur.");
        }

        // Un administrateur ne peut pas se retirer lui-même son rôle s'il est le seul admin.
        if (user.getRole() == Role.ADMIN && !Role.ADMIN.name().equals(roleStr)
                && compterAdminsActifs() <= 1) {
            throw new ServiceException("Impossible : au moins un administrateur actif est requis.");
        }

        user.setNom(nom.trim());
        user.setPrenom(prenom.trim());
        user.setLogin(login.trim());
        user.setRole(Role.valueOf(roleStr));
        if (motDePasse != null && !motDePasse.isBlank()) {
            if (motDePasse.length() < 6) {
                throw new ServiceException("Le mot de passe doit contenir au moins 6 caractères.");
            }
            user.setMotDePasse(HashUtil.sha256(motDePasse));
        }
        return userDAO.update(user);
    }

    private long compterAdminsActifs() {
        return userDAO.search(null, Statut.ACTIF, 0, Integer.MAX_VALUE)
                .getItems().stream()
                .filter(u -> u.getRole() == Role.ADMIN)
                .count();
    }

    /** Active ou désactive un utilisateur. */
    public User basculerStatut(Long id) {
        User user = userDAO.findById(id);
        if (user == null) {
            throw new ServiceException("Utilisateur introuvable.");
        }
        if (user.getStatut() == Statut.ACTIF
                && user.getRole() == Role.ADMIN
                && compterAdminsActifs() <= 1) {
            throw new ServiceException("Impossible de désactiver le dernier administrateur actif.");
        }
        user.setStatut(user.getStatut() == Statut.ACTIF ? Statut.INACTIF : Statut.ACTIF);
        return userDAO.update(user);
    }

    public User findById(Long id) {
        return userDAO.findById(id);
    }

    public PagedResult<User> lister(String terme, Statut statut, int page, int size) {
        return userDAO.search(terme, statut, page, size);
    }
}
