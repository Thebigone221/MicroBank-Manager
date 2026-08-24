package sn.microbank.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utilitaires de hachage SHA-256 pour les mots de passe.
 * Le mot de passe n'est jamais stocké en clair dans la base.
 */
public final class HashUtil {

    private HashUtil() {
    }

    public static String sha256(String texte) {
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
            // SHA-256 est toujours disponible dans le JDK
            throw new IllegalStateException(e);
        }
    }
}
