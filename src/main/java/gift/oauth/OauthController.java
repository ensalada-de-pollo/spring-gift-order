package gift.oauth;

import gift.jwt.JwtResponse;
import gift.oauth.dto.KakaoLoginRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/oauth")
public class OauthController {

    private final KakaoAuthService kakaoAuthService;

    public OauthController(KakaoAuthService kakaoAuthService) {
        this.kakaoAuthService = kakaoAuthService;
    }

    @PostMapping("/login/kakao")
    public ResponseEntity<JwtResponse> login(
        @RequestBody KakaoLoginRequest kakaoLoginRequest) {
        return ResponseEntity.ok(
            kakaoAuthService.login(kakaoLoginRequest));
    }
}
