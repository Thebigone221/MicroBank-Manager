package sn.microbank.service;

import sn.microbank.dao.UserDAO;
import sn.microbank.model.Statut;
import sn.microbank.model.User;
import sn.microbank.util.HashUtil;

import java.util.Optional;

public class AuthService {

    private final UserDAO userDAO = new UserDAO();

    public User login(String login, String motDePasse) {
        if (login == null || login.isBlank() || motDePasse == null || motDePasse.isBlank()) {
            throw new ServiceException("Veuillez saisir votre login et votre mot de passe.");
        }

        Optional<User> trouve = userDAO.findByLogin(login);
        User user = trouve.orElseThrow(() -> new ServiceException("Login ou mot de passe incorrect."));

        String hashSaisi = HashUtil.sha256(motDePasse);
        if (!hashSaisi.equals(user.getMotDePasse())) {
            throw new ServiceException("Login ou mot de passe incorrect.");
        }
        if (user.getStatut() != Statut.ACTIF) {
            throw new ServiceException("Ce compte est désactivé. Contactez l'administrateur.");
        }
        return user;
    }
}
