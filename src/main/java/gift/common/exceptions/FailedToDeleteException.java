package gift.common.exceptions;

public class FailedToDeleteException extends RuntimeException {

    public FailedToDeleteException(String message) {
        super(message);
    }
}
