package gift.oauth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record KakaoLoginResponse(
    Long id,
    @JsonProperty("kakao_account") KakaoAccount kakaoAccount
) {

    public record KakaoAccount(String email) {

    }
}
