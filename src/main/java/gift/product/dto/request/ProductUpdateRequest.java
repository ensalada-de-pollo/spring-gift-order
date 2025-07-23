package gift.product.dto.request;

import gift.common.annotation.ProductNameValidation;
import jakarta.validation.constraints.PositiveOrZero;

public record ProductUpdateRequest(
    @ProductNameValidation
    String name,

    @PositiveOrZero(
        message = "올바른 상품 가격을 입력해주세요."
    )
    Long price,

    String imageURL
) {

}
