package sn.microbank.service;

import sn.microbank.dao.AccountDAO;
import sn.microbank.dao.OperationDAO;
import sn.microbank.model.Account;
import sn.microbank.model.Client;
import sn.microbank.model.Operation;
import sn.microbank.model.TypeOperation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Construit les données d'un relevé de compte pour une période donnée.
 */
public class StatementService {

    private static final DateTimeFormatter FORMAT_FR = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /** Données complètes d'un relevé (classe classique : lisible en EL depuis les JSP). */
    public static class Releve {

        private final Account compte;
        private final LocalDateTime du;
        private final LocalDateTime au;
        private final List<Operation> operations;
        private final BigDecimal totalDepots;
        private final BigDecimal totalRetraits;

        public Releve(Account compte, LocalDateTime du, LocalDateTime au,
                      List<Operation> operations, BigDecimal totalDepots,
                      BigDecimal totalRetraits) {
            this.compte = compte;
            this.du = du;
            this.au = au;
            this.operations = operations;
            this.totalDepots = totalDepots;
            this.totalRetraits = totalRetraits;
        }

        public Account getCompte() {
            return compte;
        }

        public LocalDateTime getDu() {
            return du;
        }

        public LocalDateTime getAu() {
            return au;
        }

        public List<Operation> getOperations() {
            return operations;
        }

        public BigDecimal getTotalDepots() {
            return totalDepots;
        }

        public BigDecimal getTotalRetraits() {
            return totalRetraits;
        }

        /** Période affichée sous forme lisible ("Historique complet" ou "dd/MM/yyyy - dd/MM/yyyy"). */
        public String getPeriodeAffichee() {
            if (du == null && au == null) {
                return "Historique complet";
            }
            String d = du == null ? "—" : du.toLocalDate().format(FORMAT_FR);
            String f = au == null ? "—" : au.toLocalDate().format(FORMAT_FR);
            return d + " - " + f;
        }
    }

    private final OperationService operationService = new OperationService();
    private final AccountDAO accountDAO = new AccountDAO();

    /**
     * @param accountId  identifiant du compte (obligatoire)
     * @param dateDu     borne inférieure (inclusive), peut être null
     * @param dateAu     borne supérieure (inclusive), peut être null
     */
    public Releve construire(Long accountId, LocalDate dateDu, LocalDate dateAu) {
        Account compte = accountDAO.findById(accountId);
        if (compte == null) {
            throw new ServiceException("Compte introuvable.");
        }

        LocalDateTime du = dateDu == null ? null : dateDu.atStartOfDay();
        LocalDateTime au = dateAu == null ? null : dateAu.plusDays(1).atStartOfDay().minusNanos(1);

        List<Operation> operations = operationService
                .historique(accountId, null, null, null, du, au, null, null, 0, Integer.MAX_VALUE)
                .getItems();

        OperationDAO.Totaux totaux = operationService.totaux(accountId, du, au);
        return new Releve(compte, du, au, operations, totaux.depots(), totaux.retraits());
    }

    /**
     * Génère une ligne CSV par opération :
     * Date;Reference;Type;Montant;Description
     *
     * @return le contenu complet du fichier CSV (avec en-têtes)
     */
    public String exporterCsv(Releve releve) {
        StringBuilder csv = new StringBuilder();
        csv.append("Date;Reference;Type;Montant;Description\n");
        DateTimeFormatter format = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        for (Operation op : releve.getOperations()) {
            csv.append(op.getDateOperation().format(format)).append(';')
               .append(op.getReference()).append(';')
               .append(op.getType()).append(';')
               .append(signeMontant(op)).append(';')
               .append(nettoyer(op.getDescription())).append('\n');
        }
        // BOM UTF-8 ajouté par la servlet pour compatibilité Excel.
        return csv.toString();
    }

    /** Montant préfixé du signe selon le sens de l'opération (+100000 / -20000). */
    public static String signeMontant(Operation op) {
        String signe = op.getType() == TypeOperation.RETRAIT
                || (op.getType() == TypeOperation.VIREMENT && op.getCompteDestination() == null)
                ? "-" : "+";
        return signe + op.getMontant().toPlainString();
    }

    private String nettoyer(String texte) {
        return texte == null ? "" : texte.replace(';', ',').replace('\n', ' ').replace('\r', ' ');
    }
}
