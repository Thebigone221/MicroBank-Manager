package sn.microbank.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Fonctions de formatage exposées aux JSP via WEB-INF/functions.tld.
 */
public final class FormatUtil {

    private static final DateTimeFormatter DATE_FR = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATE_HEURE_FR = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DecimalFormat NOMBRE = new DecimalFormat("#,##0");

    static {
        // Espace comme séparateur de milliers (usage FCFA).
        java.text.DecimalFormatSymbols symboles = new java.text.DecimalFormatSymbols(Locale.FRANCE);
        symboles.setGroupingSeparator(' ');
        NOMBRE.setDecimalFormatSymbols(symboles);
    }

    private FormatUtil() {
    }

    /** 125000 -> "125 000". */
    public static String nombre(BigDecimal montant) {
        return montant == null ? "0" : NOMBRE.format(montant);
    }

    /** 125000 -> "125 000 FCFA". */
    public static String fcfa(BigDecimal montant) {
        return nombre(montant) + " FCFA";
    }

    public static String dateFr(LocalDate date) {
        return date == null ? "—" : date.format(DATE_FR);
    }

    public static String dateHeureFr(LocalDateTime dateHeure) {
        return dateHeure == null ? "—" : dateHeure.format(DATE_HEURE_FR);
    }
}
