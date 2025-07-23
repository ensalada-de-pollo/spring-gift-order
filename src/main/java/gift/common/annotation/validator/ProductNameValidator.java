package gift.common.annotation.validator;

import gift.common.annotation.ProductNameValidation;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ProductNameValidator implements ConstraintValidator<ProductNameValidation, String> {

    private static final int MIN_LENGTH = 1;
    private static final int MAX_LENGTH = 15;
    private static final String ALLOWED_PATTERN = "^[ㄱ-ㅎㅏ-ㅣ가-힣a-zA-Z0-9\\(\\)\\[\\]\\+\\-\\&/_ ]*$";
    private static final String NOT_ALLOWED_WORD = "카카오";

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return isLengthValid(value, context)
            && isPatternValid(value, context)
            && isAllowedWordValid(value, context);
    }

    private boolean isLengthValid(String value, ConstraintValidatorContext context) {
        if (MIN_LENGTH > value.length() || MAX_LENGTH < value.length()) {
            buildViolation(
                context,
                "상품 이름은 공백을 포함하여 최대 15글자까지 입력할 수 있습니다."
            );
            return false;
        }
        return true;
    }

    private boolean isPatternValid(String value, ConstraintValidatorContext context) {
        if (!value.matches(ALLOWED_PATTERN)) {
            buildViolation(
                context,
                "상품 이름은 한글, 영어, 숫자, 특수문자(( ), [ ], +, -, &, /, _) 외 다른 문자가 들어갈 수 없습니다."
            );
            return false;
        }
        return true;
    }

    private boolean isAllowedWordValid(String value, ConstraintValidatorContext context) {
        if (value.contains(NOT_ALLOWED_WORD)) {
            buildViolation(
                context,
                "'카카오'가 포함된 문구는 담당 MD와 협의한 경우에만 사용할 수 있습니다."
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
