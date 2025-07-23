package gift.common.exceptions;

public class LogInFailedException extends RuntimeException {

    public LogInFailedException() {
        super("이메일 또는 비밀번호가 일치하지 않습니다.");
    }
}
