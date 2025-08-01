package gift.product.dto.request;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.data.domain.Sort;

public record PageFindRequest(
    @Nullable
    @PositiveOrZero(message = "0 이상의 정수를 입력해주세요.")
    Integer page,

    @Nullable
    @PositiveOrZero(message = "0 이상의 정수를 입력해주세요.")
    Integer size,

    Sort.Direction direction,

    String criteria
) {

}
