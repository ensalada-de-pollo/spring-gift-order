package gift.common;

import org.springframework.http.HttpStatus;

public record ErrorResult(HttpStatus status, String message) {

}
