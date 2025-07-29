package gift.order.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record OrderRequest(
    @NotNull(message = "옵션을 선택해주세요.")
    Long optionId,

    @NotNull(message = "수량을 입력해주세요.")
    @Positive(message = "최수 수량은 1입니다.")
    Long quantity,

    String message
) {

}
