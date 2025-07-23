package gift.option.dto;

import gift.common.annotation.OptionNameValidation;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record OptionAddRequest(
    @NotBlank(message = "옵션명을 입력해주세요.")
    @OptionNameValidation
    String name,

    @NotNull(message = "수량을 입력해주세요.")
    @Min(value = 1, message = "최솟값은 1 입니다.")
    @Max(value = 100_000_000, message = "최댓값은 100,000,000 입니다.")
    Long quantity) {

}
