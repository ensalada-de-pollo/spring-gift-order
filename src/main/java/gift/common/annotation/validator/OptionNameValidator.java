package gift.common.annotation.validator;

import gift.common.annotation.OptionNameValidation;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class OptionNameValidator implements ConstraintValidator<OptionNameValidation, String> {

    private static final int MIN_LENGTH = 1;
    private static final int MAX_LENGTH = 100_000_000;
    private static final String ALLOWED_PATTERN = "^[ㄱ-ㅎㅏ-ㅣ가-힣a-zA-Z0-9\\(\\)\\[\\]\\+\\-\\&/_ ]*$";

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return isLengthValid(value, context)
            && isPatternValid(value, context);
    }

    private boolean isLengthValid(String value, ConstraintValidatorContext context) {
        if (MIN_LENGTH > value.length() || MAX_LENGTH < value.length()) {
            buildViolation(
                context,
                "옵션 이름은 공백을 포함하여 최대 50글자까지 입력 가능합니다."
            );
            return false;
        }
        return true;
    }

    private boolean isPatternValid(String value, ConstraintValidatorContext context) {
        if (!value.matches(ALLOWED_PATTERN)) {
            buildViolation(
                context,
                "옵션 이름은 한글, 영어, 숫자, 특수문자(( ), [ ], +, -, &, /, _) 외 다른 문자가 들어갈 수 없습니다."
            );
            return false;
        }
        return true;
    }

    private void buildViolation(ConstraintValidatorContext context, String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
    }
}
