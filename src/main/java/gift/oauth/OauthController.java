package gift.oauth;

import gift.common.exceptions.LogInFailedException;
import gift.jwt.JwtResponse;
import gift.oauth.dto.KakaoLoginRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/api/oauth")
public class OauthController {

    private final KakaoAuthService kakaoAuthService;
    private final KakaoProperties kakaoProperties;

    public OauthController(KakaoAuthService kakaoAuthService, KakaoProperties kakaoProperties) {
        this.kakaoAuthService = kakaoAuthService;
        this.kakaoProperties = kakaoProperties;
    }

    @GetMapping("/authorize/kakao")
    public void redirectToKakao(HttpServletResponse response) {
        String kakaoUrl = UriComponentsBuilder
            .fromUriString(kakaoProperties.kakaoAuthorizeUri())
            .queryParam("response_type", "code")
            .queryParam("client_id", kakaoProperties.clientId())
            .queryParam("redirect_uri", kakaoProperties.redirectUri())
            .build()
            .toUriString();
        try {
            response.sendRedirect(kakaoUrl);
        } catch (Exception e) {
            throw new LogInFailedException("카카오 로그인 중 오류가 발생하였습니다.");
        }
    }

    @GetMapping
    public void login(
        @ModelAttribute KakaoLoginRequest kakaoLoginRequest, HttpServletResponse response) {
        JwtResponse jwt = kakaoAuthService.login(kakaoLoginRequest);
        try {
            response.sendRedirect("/?token=" + jwt.accessToken());
        } catch (Exception e) {
            throw new LogInFailedException(e.getMessage());
        }
    }
}
