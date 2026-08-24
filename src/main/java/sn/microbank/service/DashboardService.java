package sn.microbank.service;

import sn.microbank.dao.AccountDAO;
import sn.microbank.dao.ClientDAO;
import sn.microbank.dao.OperationDAO;
import sn.microbank.model.CompteStatut;
import sn.microbank.model.Statut;
import sn.microbank.model.TypeCompte;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Service du tableau de bord : statistiques globales simples.
 */
public class DashboardService {

    private final ClientDAO clientDAO = new ClientDAO();
    private final AccountDAO accountDAO = new AccountDAO();
    private final OperationService operationService = new OperationService();

    /**
     * @return les chiffres clés affichés sur le tableau de bord.
     */
    public Map<String, Object> statistiques() {
        LocalDateTime debutJour = LocalDateTime.now().toLocalDate().atStartOfDay();

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalClients", clientDAO.count());
        stats.put("clientsActifs", clientDAO.countByStatut(Statut.ACTIF));
        stats.put("clientsNouveauMois", clientDAO.countCreatedSince(debutJour.minusDays(30)));
        stats.put("totalComptes", accountDAO.count());
        stats.put("comptesActifs", accountDAO.countByStatut(CompteStatut.ACTIF));
        stats.put("soldeTotal", accountDAO.sumSoldeActifs());
        stats.put("operationsDuJour", operationService.countDepuis(debutJour));

        // Bonus 3 : statistiques supplémentaires
        Map<TypeCompte, Long> parType = accountDAO.countByType();
        stats.put("comptesCourant", parType.getOrDefault(TypeCompte.COURANT, 0L));
        stats.put("comptesEpargne", parType.getOrDefault(TypeCompte.EPARGNE, 0L));

        OperationDAO.Totaux totauxJour = operationService.totaux(null, debutJour, null);
        stats.put("depotsDuJour", totauxJour.depots());
        stats.put("retraitsDuJour", totauxJour.retraits());
        stats.put("dernieresOperations", operationService.dernieres(8));

        // Comptes bloqués à surveiller
        stats.put("comptesBloques", accountDAO.countByStatut(CompteStatut.BLOQUE));

        BigDecimal soldeTotal = (BigDecimal) stats.get("soldeTotal");
        long total = (Long) stats.get("totalComptes");
        stats.put("soldeMoyen", total == 0 ? BigDecimal.ZERO
                : soldeTotal.divide(BigDecimal.valueOf(total), 2, java.math.RoundingMode.HALF_UP));
        return stats;
    }
}
