package gift.common;

import gift.common.exceptions.FailedToDeleteException;
import gift.common.exceptions.FailedToFindException;
import gift.common.exceptions.JwtValidationException;
import gift.common.exceptions.LogInFailedException;
import gift.common.exceptions.AlreadyExistsException;
import gift.common.exceptions.NullTokenException;
import java.util.stream.Collectors;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResult> handleMethodArgumentNotValidException(
        MethodArgumentNotValidException ex) {
        return new ResponseEntity<>(
            new ErrorResult(
                HttpStatus.BAD_REQUEST,
                ex.getBindingResult()
                    .getFieldErrors()
                    .stream()
                    .map(MessageSourceResolvable::getDefaultMessage)
                    .collect(Collectors.joining(" "))
            ),
            HttpStatus.BAD_REQUEST
        );
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResult> handleHttpMessageNotReadableException() {
        return new ResponseEntity<>(
            new ErrorResult(
                HttpStatus.BAD_REQUEST,
                "유효한 값을 입력해주세요."
            ),
            HttpStatus.BAD_REQUEST
        );
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(value = EmptyResultDataAccessException.class)
    public ResponseEntity<ErrorResult> handleEmptyResultDataAccessException(
        EmptyResultDataAccessException ex) {
        return new ResponseEntity<>(
            new ErrorResult(
                HttpStatus.NOT_FOUND,
                ex.getMessage()
            ),
            HttpStatus.NOT_FOUND
        );
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(value = JwtValidationException.class)
    public ResponseEntity<ErrorResult> handleJwtValidationException(
        JwtValidationException ex) {
        return new ResponseEntity<>(
            new ErrorResult(
                HttpStatus.UNAUTHORIZED,
                ex.getMessage()
            ),
            HttpStatus.UNAUTHORIZED
        );
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(value = LogInFailedException.class)
    public ResponseEntity<ErrorResult> handlePasswordMismatchException(
        LogInFailedException ex) {
        return new ResponseEntity<>(
            new ErrorResult(
                HttpStatus.UNAUTHORIZED,
                ex.getMessage()
            ),
            HttpStatus.UNAUTHORIZED
        );
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(value = AlreadyExistsException.class)
    public ResponseEntity<ErrorResult> handleMemberAlreadyExistsException(
        AlreadyExistsException ex) {
        return new ResponseEntity<>(
            new ErrorResult(
                HttpStatus.CONFLICT,
                ex.getMessage()
            ),
            HttpStatus.CONFLICT
        );
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(value = NullTokenException.class)
    public ResponseEntity<ErrorResult> handleNullTokenException(
        NullTokenException ex) {
        return new ResponseEntity<>(
            new ErrorResult(
                HttpStatus.UNAUTHORIZED,
                ex.getMessage()
            ),
            HttpStatus.UNAUTHORIZED
        );
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(value = FailedToDeleteException.class)
    public ResponseEntity<ErrorResult> handleFailedToDeleteException(
        FailedToDeleteException ex) {
        return new ResponseEntity<>(
            new ErrorResult(
                HttpStatus.FORBIDDEN,
                ex.getMessage()
            ),
            HttpStatus.FORBIDDEN
        );
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(value = FailedToFindException.class)
    public ResponseEntity<ErrorResult> handleFailedToFindException(
        FailedToFindException ex) {
        return new ResponseEntity<>(
            new ErrorResult(
                HttpStatus.NOT_FOUND,
                ex.getMessage()
            ),
            HttpStatus.NOT_FOUND
        );
    }
}
