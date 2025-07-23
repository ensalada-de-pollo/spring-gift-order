package gift.common.exceptions;

public class JwtValidationException extends RuntimeException {

    public JwtValidationException(String message) {
        super(message);
    }
}
