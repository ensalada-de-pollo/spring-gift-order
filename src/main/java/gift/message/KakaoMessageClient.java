package gift.message;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gift.common.exceptions.FailedToSendMessageException;
import gift.common.property.KakaoProperties;
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
public class KakaoMessageClient {

    private final RestTemplate restTemplate;
    private final KakaoProperties kakaoProperties;
    private final ObjectMapper objectMapper;
    private final Logger log = LoggerFactory.getLogger(KakaoMessageClient.class);

    public KakaoMessageClient(
        RestTemplate restTemplate,
        KakaoProperties kakaoProperties,
        ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.kakaoProperties = kakaoProperties;
        this.objectMapper = objectMapper;
    }

    public void sendMessage(
        OrderMessageRequest orderMessageRequest,
        String kakaoAccessToken) {

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE);
        headers.add("Authorization", "Bearer " + kakaoAccessToken);
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("template_id", 122907);

        try {
            String templateArgsJson = objectMapper.writeValueAsString(orderMessageRequest);
            body.add("template_args", templateArgsJson);
        } catch (JsonProcessingException e) {
            throw new FailedToSendMessageException("카카오 메세지의 직렬화 과정 중 오류가 발생했습니다.");
        }

        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);

        requestSendMessage(
            kakaoProperties.requestMessageToMeUri(),
            request
        );
    }

    private void requestSendMessage(
        String uri,
        HttpEntity<?> request
    ) {
        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                uri,
                HttpMethod.POST,
                request,
                Map.class
            );

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                log.warn(response.toString());
                throw new FailedToSendMessageException(
                    "카카오 메세지 전송에 실패하였습니다. " + response.getStatusCode());
            }

            Map responseBody = response.getBody();

            if (!responseBody.get("result_code").equals(0)) {
                throw new FailedToSendMessageException("알 수 없는 이유로 카카오 메세지 전송에 실패하였습니다.");
            }
        } catch (RestClientException e) {
            log.error(e.getMessage());
            throw new FailedToSendMessageException("카카오 메세지 전송 중 REST 요청 오류가 발생했습니다.");
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new FailedToSendMessageException("카카오 메세지 전송 중 예상치 못한 오류가 발생했습니다.");
        }
    }
}
