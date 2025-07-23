package gift.common;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import org.springframework.stereotype.Component;

@Component
public class PasswordEncoder {

    public String encrypt(String email, String password) {
        try {
            KeySpec keySpec = new PBEKeySpec(
                password.toCharArray(),
                getSalt(email),
                85319,
                128);

            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            byte[] keyBytes = keyFactory.generateSecret(keySpec).getEncoded();

            return Base64.getEncoder().encodeToString(keyBytes);
        } catch (
            NoSuchAlgorithmException |
            UnsupportedEncodingException |
            InvalidKeySpecException e
        ) {
            throw new RuntimeException(e);
        }
    }

    private byte[] getSalt(String email)
        throws NoSuchAlgorithmException, InvalidKeySpecException, UnsupportedEncodingException {

        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(email.getBytes("UTF-8"));

        return digest.digest(hash);
    }
}
