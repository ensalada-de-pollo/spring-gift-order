package gift.kakao.message;

import gift.common.property.KakaoProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class MessageService {

    private final RestTemplate restTemplate;
    private final KakaoProperties kakaoProperties;

    public MessageService(
        RestTemplate restTemplate,
        KakaoProperties kakaoProperties) {
        this.restTemplate = restTemplate;
        this.kakaoProperties = kakaoProperties;
    }


}
