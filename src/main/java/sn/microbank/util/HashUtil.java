package sn.microbank.util;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;

public final class HashUtil {

    private static final int ITERATIONS = 120_000;
    private static final int LONGUEUR_SEL = 16;
    private static final int LONGUEUR_CLE = 256;

    private HashUtil() {
    }

    public static String hasher(String motDePasse) {
        byte[] sel = new byte[LONGUEUR_SEL];
        new SecureRandom().nextBytes(sel);
        byte[] cle = pbkdf2(motDePasse, sel);
        return "pbkdf2$" + ITERATIONS + "$"
                + Base64.getEncoder().encodeToString(sel) + "$"
                + Base64.getEncoder().encodeToString(cle);
    }

    public static boolean verifier(String motDePasse, String stocke) {
        if (motDePasse == null || stocke == null) {
            return false;
        }
        if (stocke.startsWith("pbkdf2$")) {
            String[] parties = stocke.split("\\$");
            if (parties.length != 4) {
                return false;
            }
            int iterations = Integer.parseInt(parties[1]);
            byte[] sel = Base64.getDecoder().decode(parties[2]);
            byte[] attendu = Base64.getDecoder().decode(parties[3]);
            byte[] calcule = pbkdf2(motDePasse, sel, iterations);
            return MessageDigest.isEqual(attendu, calcule);
        }
        return sha256Hex(motDePasse).equalsIgnoreCase(stocke);
    }

    public static boolean estAncienFormat(String stocke) {
        return stocke != null && !stocke.startsWith("pbkdf2$");
    }

    private static byte[] pbkdf2(String motDePasse, byte[] sel) {
        return pbkdf2(motDePasse, sel, ITERATIONS);
    }

    private static byte[] pbkdf2(String motDePasse, byte[] sel, int iterations) {
        try {
            KeySpec spec = new PBEKeySpec(motDePasse.toCharArray(), sel, iterations, LONGUEUR_CLE);
            SecretKeyFactory fabrique = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            return fabrique.generateSecret(spec).getEncoded();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static String sha256Hex(String texte) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(texte.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                sb.append(Character.forDigit((b >> 4) & 0xF, 16));
                sb.append(Character.forDigit(b & 0xF, 16));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
