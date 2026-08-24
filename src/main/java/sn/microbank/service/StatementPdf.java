package sn.microbank.service;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import sn.microbank.model.Account;
import sn.microbank.model.Client;
import sn.microbank.model.CompteStatut;
import sn.microbank.model.TypeOperation;

import java.awt.Color;
import java.io.OutputStream;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Génération du relevé de compte PDF avec OpenPDF.
 * Contenu : client, compte, période, opérations, totaux, solde final.
 */
public class StatementPdf {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATE_HEURE = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final Font TITRE = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
    private static final Font SOUS_TITRE = FontFactory.getFont(FontFactory.HELVETICA, 11);
    private static final Font ENTETE_TABLEAU = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE);
    private static final Font CELLULE = FontFactory.getFont(FontFactory.HELVETICA, 9);
    private static final Color VERT_BANQUE = new Color(13, 92, 66);

    public void ecrire(StatementService.Releve releve, OutputStream sortie) {
        Document document = new Document(PageSize.A4, 36, 36, 40, 40);
        try {
            PdfWriter.getInstance(document, sortie);
            document.open();
            Account compte = releve.getCompte();
            Client client = compte.getClient();

            // En-tête
            Paragraph titre = new Paragraph("MICROBANK", TITRE);
            titre.setAlignment(Element.ALIGN_CENTER);
            document.add(titre);

            Paragraph sousTitre = new Paragraph("RELEVÉ DE COMPTE", SOUS_TITRE);
            sousTitre.setAlignment(Element.ALIGN_CENTER);
            sousTitre.setSpacingAfter(14);
            document.add(sousTitre);

            document.add(new Paragraph("Client  : " + client.getNomComplet(), CELLULE));
            document.add(new Paragraph("Compte  : " + compte.getNumeroCompte(), CELLULE));
            document.add(new Paragraph("Type    : " + traduireType(compte.getType()), CELLULE));
            document.add(new Paragraph("Agence  : "
                    + (compte.getAgency() != null ? compte.getAgency().getNom() : "—"), CELLULE));
            document.add(new Paragraph("Statut  : " + compte.getStatut(), CELLULE));
            Paragraph periode = new Paragraph("Période : " + releve.getPeriodeAffichee(), CELLULE);
            periode.setSpacingBefore(6);
            periode.setSpacingAfter(10);
            document.add(periode);

            // Tableau des opérations
            PdfPTable table = new PdfPTable(new float[]{2f, 1.8f, 1.5f, 2.2f, 3.5f});
            table.setWidthPercentage(100);
            ajouterEntete(table, "Date", "Référence", "Type", "Montant (FCFA)", "Description");

            for (var op : releve.getOperations()) {
                cellule(table, op.getDateOperation().format(DATE_HEURE), false);
                cellule(table, op.getReference(), false);
                cellule(table, String.valueOf(op.getType()), false);
                cellule(table, StatementService.signeMontant(op), true);
                cellule(table, op.getDescription() == null ? "" : op.getDescription(), false);
            }

            if (releve.getOperations().isEmpty()) {
                PdfPCell vide = new PdfPCell(new Phrase("Aucune opération sur la période.", CELLULE));
                vide.setColspan(5);
                vide.setHorizontalAlignment(Element.ALIGN_CENTER);
                vide.setPadding(10);
                table.addCell(vide);
            }
            document.add(table);

            // Totaux
            document.add(espace());
            PdfPTable totaux = new PdfPTable(new float[]{6f, 4f});
            totaux.setWidthPercentage(100);
            total(totaux, "Total des dépôts", releve.getTotalDepots());
            total(totaux, "Total des retraits", releve.getTotalRetraits());
            document.add(totaux);

            // Solde final
            Paragraph solde = new Paragraph(
                    "Solde du compte au " + java.time.LocalDate.now().format(DATE) + " : "
                            + String.format(Locale.FRANCE, "%,.0f FCFA",
                            compte.getSolde()).replace('\u00A0', ' '),
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, VERT_BANQUE));
            solde.setAlignment(Element.ALIGN_RIGHT);
            solde.setSpacingBefore(16);
            document.add(solde);

            Paragraph pied = new Paragraph("Document généré automatiquement par MicroBank Manager.",
                    FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 8));
            pied.setAlignment(Element.ALIGN_CENTER);
            pied.setSpacingBefore(20);
            document.add(pied);

            document.close();
        } catch (Exception e) {
            throw new IllegalStateException("Erreur lors de la génération du PDF : " + e.getMessage(), e);
        }
    }

    private void ajouterEntete(PdfPTable table, String... colonnes) {
        for (String colonne : colonnes) {
            PdfPCell cellule = new PdfPCell(new Phrase(colonne, ENTETE_TABLEAU));
            cellule.setBackgroundColor(VERT_BANQUE);
            cellule.setPadding(6);
            table.addCell(cellule);
        }
    }

    private void cellule(PdfPTable table, String valeur, boolean droite) {
        PdfPCell cellule = new PdfPCell(new Phrase(valeur == null ? "" : valeur, CELLULE));
        cellule.setHorizontalAlignment(droite ? Element.ALIGN_RIGHT : Element.ALIGN_LEFT);
        cellule.setPadding(4);
        table.addCell(cellule);
    }

    private void total(PdfPTable table, String libelle, java.math.BigDecimal montant) {
        PdfPCell gauche = new PdfPCell(new Phrase(libelle, CELLULE));
        gauche.setBorder(PdfPCell.NO_BORDER);
        gauche.setHorizontalAlignment(Element.ALIGN_RIGHT);
        gauche.setPadding(4);
        table.addCell(gauche);

        PdfPCell droite = new PdfPCell(new Phrase(
                String.format(Locale.FRANCE, "%,.0f FCFA", montant).replace('\u00A0', ' '),
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9)));
        droite.setBorder(PdfPCell.NO_BORDER);
        droite.setHorizontalAlignment(Element.ALIGN_RIGHT);
        droite.setPadding(4);
        table.addCell(droite);
    }

    private Paragraph espace() {
        Paragraph p = new Paragraph(" ");
        p.setSpacingBefore(8);
        return p;
    }

    private String traduireType(sn.microbank.model.TypeCompte type) {
        return type == sn.microbank.model.TypeCompte.COURANT ? "Compte courant" : "Compte épargne";
    }
}
