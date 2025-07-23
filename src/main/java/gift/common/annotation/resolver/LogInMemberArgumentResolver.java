package gift.common.annotation.resolver;

import gift.common.annotation.LogInMember;
import gift.common.exceptions.NullTokenException;
import gift.member.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public class LogInMemberArgumentResolver implements HandlerMethodArgumentResolver {

    private final MemberService memberService;

    public LogInMemberArgumentResolver(MemberService memberService) {
        this.memberService = memberService;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(LogInMember.class);
    }

    @Override
    public Object resolveArgument(
        MethodParameter parameter,
        ModelAndViewContainer mavContainer,
        NativeWebRequest webRequest,
        WebDataBinderFactory binderFactory
    ) throws Exception {
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        String resolvedToken = (String) request.getAttribute("resolvedToken");

        if (resolvedToken == null) {
            throw new NullTokenException("토큰 정보가 없습니다.");
        }

        return memberService.getIdFromToken(resolvedToken);
    }
}
