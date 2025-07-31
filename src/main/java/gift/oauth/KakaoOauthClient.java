package gift.oauth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gift.common.exceptions.LogInFailedException;
import gift.common.property.KakaoProperties;
import gift.oauth.dto.KakaoLoginResponse;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class KakaoOauthClient {

    private final RestTemplate restTemplate;
    private final KakaoProperties kakaoProperties;
    private final ObjectMapper objectMapper;
    private final Logger log = LoggerFactory.getLogger(KakaoOauthClient.class);

    public KakaoOauthClient(
        RestTemplate restTemplate,
        KakaoProperties kakaoProperties,
        ObjectMapper objectMapper
    ) {
        this.restTemplate = restTemplate;
        this.kakaoProperties = kakaoProperties;
        this.objectMapper = objectMapper;
    }

    protected String requestToken(String authorizationCode) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE);
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", kakaoProperties.clientId());
        body.add("redirect_uri", kakaoProperties.redirectUri());
        body.add("code", authorizationCode);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        Map response = requestApi(
            kakaoProperties.requestTokenUri(),
            HttpMethod.POST,
            request,
            Map.class
        );

        String accessToken;

        if ((accessToken = response.get("access_token").toString()) != null) {
            return accessToken;
        } else {
            throw new LogInFailedException("카카오 로그인 응답에서 토큰을 찾을 수 없습니다.");
        }
    }

    protected String extractEmailFromResponse(String kakaoAccessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE);
        headers.add("Authorization", "Bearer " + kakaoAccessToken);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            KakaoLoginResponse kakaoLoginResponse = objectMapper.readValue(
                requestApi(
                    kakaoProperties.requestUserInfoUri(),
                    HttpMethod.GET,
                    request,
                    String.class
                ),
                KakaoLoginResponse.class
            );

            String email;

            if ((email = kakaoLoginResponse.kakaoAccount().email()) == null) {
                throw new LogInFailedException("카카오 계정 이메일 정보를 찾을 수 없습니다.");
            }

            return email;
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
            throw new LogInFailedException("카카오 로그인 응답 처리 중 오류가 발생했습니다.");
        }
    }

    private <T> T requestApi(
        String uri,
        HttpMethod method,
        HttpEntity<?> request,
        Class<T> responseType
    ) {
        try {
            ResponseEntity<T> response = restTemplate.exchange(
                uri,
                method,
                request,
                responseType
            );

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                log.warn(response.toString());
                throw new LogInFailedException("카카오 로그인 요청에 실패하였습니다. " + response.getStatusCode());
            }

            return response.getBody();
        } catch (RestClientException e) {
            log.error(e.getMessage());
            throw new LogInFailedException("카카오 로그인 중 REST 요청 오류가 발생했습니다.");
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new LogInFailedException("카카오 로그인 중 예상치 못한 오류가 발생했습니다.");
        }
    }
}
