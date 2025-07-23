package gift.product.dto.request;

import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.data.domain.Sort;

public record PageFindRequest(
    @PositiveOrZero(message = "0 이상의 정수를 입력해주세요.")
    int page,

    @PositiveOrZero(message = "0 이상의 정수를 입력해주세요.")
    int size,

    Sort.Direction direction,

    String criteria
) {

}
