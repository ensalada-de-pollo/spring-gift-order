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

    public Integer getPage() {
        if (page == null) {
            return 0;
        }

        return page;
    }

    public Integer getSize() {
        if (size == null) {
            return 5;
        }

        return size;
    }

    public Sort.Direction getDirection() {
        if (direction == null) {
            return Sort.Direction.ASC;
        }

        return direction;
    }

    public String getCriteria() {
        if (criteria == null) {
            return "id";
        }

        return criteria;
    }
}
