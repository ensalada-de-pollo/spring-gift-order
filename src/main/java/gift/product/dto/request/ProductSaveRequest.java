package gift.product.dto.request;

import gift.common.annotation.ProductNameValidation;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record ProductSaveRequest(
    @NotBlank(
        message = "상품 이름을 입력해주세요."
    )
    @ProductNameValidation
    String name,

    @NotNull(
        message = "상품 가격을 입력해주세요."
    )
    @PositiveOrZero(
        message = "올바른 상품 가격을 입력해주세요."
    )
    Long price,

    String imageURL
) {

}
