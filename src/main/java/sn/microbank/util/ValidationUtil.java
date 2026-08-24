package sn.microbank.util;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Validation des formulaires côté serveur.
 * Chaque méthode enregistre ses erreurs dans une Map (champ -> message)
 * que les JSP affichent à l'utilisateur.
 */
public final class ValidationUtil {

    private static final Pattern EMAIL = Pattern.compile("^[\\w.+-]+@[\\w-]+\\.[\\w.-]+$");
    private static final Pattern TELEPHONE = Pattern.compile("^[0-9+ ]{9,20}$");

    private ValidationUtil() {
    }

    /** Vérifie qu'une chaîne est présente (non nulle et non vide). */
    public static void requis(Map<String, String> erreurs, String champ, String valeur, String message) {
        if (valeur == null || valeur.isBlank()) {
            erreurs.put(champ, message);
        }
    }

    public static void email(Map<String, String> erreurs, String champ, String valeur) {
        if (valeur != null && !valeur.isBlank() && !EMAIL.matcher(valeur).matches()) {
            erreurs.put(champ, "Email invalide.");
        }
    }

    public static void telephone(Map<String, String> erreurs, String champ, String valeur) {
        if (valeur != null && !valeur.isBlank() && !TELEPHONE.matcher(valeur).matches()) {
            erreurs.put(champ, "Téléphone invalide (ex : 77 123 45 67).");
        }
    }

    /**
     * Convertit un montant saisi en BigDecimal positif.
     * Ajoute une erreur si la valeur est absente, non numérique ou <= 0.
     */
    public static java.math.BigDecimal montantPositif(Map<String, String> erreurs, String champ, String valeur) {
        if (valeur == null || valeur.isBlank()) {
            erreurs.put(champ, "Le montant est obligatoire.");
            return null;
        }
        try {
            java.math.BigDecimal montant = new java.math.BigDecimal(valeur.trim());
            if (montant.signum() <= 0) {
                erreurs.put(champ, "Le montant doit être strictement positif.");
                return null;
            }
            return montant;
        } catch (NumberFormatException e) {
            erreurs.put(champ, "Montant invalide.");
            return null;
        }
    }

    /** Convertit une date ISO (yyyy-MM-dd) en LocalDate, ou null si vide/invalide. */
    public static LocalDate date(String valeur) {
        if (valeur == null || valeur.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(valeur.trim());
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    public static Map<String, String> nouvellesErreurs() {
        return new HashMap<>();
    }
}
