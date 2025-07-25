package gift.oauth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gift.common.exceptions.LogInFailedException;
import gift.jwt.JwtResponse;
import gift.jwt.JwtUtil;
import gift.member.domain.Member;
import gift.member.repository.MemberRepository;
import gift.oauth.dto.KakaoLoginRequest;
import gift.oauth.dto.KakaoLoginResponse;
import java.util.Map;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class KakaoAuthService {

    private final ObjectMapper objectMapper;
    private final KakaoProperties kakaoProperties;
    private final RestTemplate restTemplate;
    private final MemberRepository memberRepository;
    private final JwtUtil jwtUtil;

    public KakaoAuthService(ObjectMapper objectMapper, KakaoProperties kakaoProperties,
        RestTemplate restTemplate, MemberRepository memberRepository, JwtUtil jwtUtil) {
        this.objectMapper = objectMapper;
        this.kakaoProperties = kakaoProperties;
        this.restTemplate = restTemplate;
        this.memberRepository = memberRepository;
        this.jwtUtil = jwtUtil;
    }

    public JwtResponse login(KakaoLoginRequest loginRequest) {
        String kakaoAccessToken = requestToken(loginRequest.authorizationCode());
        String email = getUserEmail(kakaoAccessToken);

        Member member = memberRepository.findByEmail(email)
            .orElseGet(() -> memberRepository.save(new Member(email)));

        String accessToken = jwtUtil.createAccessToken(member);

        return new JwtResponse(
            accessToken,
            member.getId()
        );
    }

    private String requestToken(String authorizationCode) {
        String url = "https://kauth.kakao.com/oauth/token";
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE);
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", kakaoProperties.clientId());
        body.add("redirect_uri", kakaoProperties.redirectUri());
        body.add("code", authorizationCode);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                request,
                new ParameterizedTypeReference<>() {
                }
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                String accessToken;

                if (response.getBody() != null
                    && (accessToken = response.getBody().get("access_token").toString()) != null) {
                    return accessToken;
                } else {
                    throw new LogInFailedException("카카오 로그인 응답에서 토큰을 찾을 수 없습니다.");
                }

            } else {
                throw new LogInFailedException("카카오 로그인 요청에 실패하였습니다. " + response.getStatusCode());
            }
        } catch (RestClientException e) {
            throw new LogInFailedException("카카오 로그인 중 REST 요청 오류가 발생했습니다.");
        } catch (Exception e) {
            throw new LogInFailedException("카카오 로그인 중 예상치 못한 오류가 발생했습니다.");
        }
    }

    private String getUserEmail(String kakaoAccessToken) {
        String url = "https://kapi.kakao.com/v2/user/me";
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE);
        headers.add("Authorization", "Bearer " + kakaoAccessToken);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                request,
                String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                KakaoLoginResponse kakaoLoginResponse = objectMapper.readValue(response.getBody(),
                    KakaoLoginResponse.class);

                String email;

                if ((email = kakaoLoginResponse.kakaoAccount().email()) == null) {
                    throw new LogInFailedException("카카오 계정 이메일 정보를 찾을 수 없습니다.");
                }

                return email;
            } else {
                throw new LogInFailedException("카카오 로그인 요청에 실패하였습니다. " + response.getStatusCode());
            }
        } catch (JsonProcessingException e) {
            throw new LogInFailedException("카카오 로그인 응답 처리 중 오류가 발생했습니다.");
        }
    }
}
