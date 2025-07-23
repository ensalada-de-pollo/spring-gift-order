package gift.option.dto;

import gift.common.annotation.OptionNameValidation;
import jakarta.validation.constraints.NotBlank;

public record OptionUpdateRequest(
    @NotBlank(message = "옵션명을 입력해주세요.")
    @OptionNameValidation
    String name
) {

}
