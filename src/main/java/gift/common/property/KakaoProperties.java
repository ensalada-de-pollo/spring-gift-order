package gift.common.property;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "kakao")
public record KakaoProperties(
    String clientId,
    String redirectUri,
    String requestTokenUri,
    String requestUserInfoUri,
    String kakaoAuthorizeUri,
    String requestMessageToMeUri
) {

}
