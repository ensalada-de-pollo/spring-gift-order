# spring-gift-order

### STEP1

- gradle build 시 테스트 코드에서 오류가 발생했습니다. 하드코딩 되어 있던 ID 부분들을 전부 객체에서 가져오는 방식으로 수정하여 build에 성공함을 확인하였습니다.
- ddl-auto 정책을 validate로 변경하였습니다.
- 테스트 코드에서 baseURL 변수는 전부 setUp 메서드 내부로 인라이닝 하였습니다.
- application.properties에서 관리하고 있는 `jwt.secret`, 이번 미션에서 추가된 `kakao.client-id`, `kakao.redirect-uri`
  는 환경변수로 받도록 하였습니다.

#### 카카오 로그인 구현 내용

##### RestTemplateConfig

kakao 로그인이라는 외부 서비스를 가져오기에 앞서 RestTemplate을 설정하는 설정 클래스를 작성하였습니다.

```java

@Bean
public RestTemplate restTemplate(RestTemplateBuilder builder) {
    return builder
        .requestFactory(() -> new BufferingClientHttpRequestFactory(
            new SimpleClientHttpRequestFactory()
        ))
        .connectTimeout(Duration.ofMillis(5000))
        .readTimeout(Duration.ofMillis(5000))
        .build();
}
```

`SimpleClientHttpRequestFactory`는 기본적으로 Http 요청을 보내는 팩토리입니다. 공부를 해본 내용으로는
`SimpleClientHttpRequestFactory`만 사용했을 때 응답 바디를 한 번만 읽을 수 있다고 합니다. 이 때 서비스 레이어에 도달하기 전 인터셉터같은 부분에서
응답 바디를 읽어버리면 서비스 레이어에서는 읽을 수 없습니다. 그렇기 때문에 응답 바디를 메모리에 기록해두어 필요할 때 다시 읽어올 수 있는
`BufferingClientRequestFactory`를 사용하여 requestFactory를 구성하였습니다.

또한, 연결 과정이나 응답을 읽는 과정에서 시간이 5초 이상 걸리면 타임아웃이 되도록 타임아웃 시간을 설정해두었습니다.

##### KakaoAuthService

kakao 토큰을 받아오고, 사용자의 정보를 가져오고, 이를 통해 사용자를 등록 또는 로그인하게 하는 로직을 전부 해당 서비스 클래스에 작성했습니다.

```java
String url = "https://kauth.kakao.com/oauth/token";
HttpHeaders headers = new HttpHeaders();
headers.

add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE);

MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
body.

add("grant_type","authorization_code");
body.

add("client_id",kakaoProperties.clientId());
    body.

add("redirect_uri",kakaoProperties.redirectUri());
    body.

add("code",authorizationCode);

HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

try{
ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
    url,
    HttpMethod.POST,
    request,
    new ParameterizedTypeReference<>() {
    }
);
```

과제 가이드에 제시되어있는 대로 헤더와 요청 바디를 구성하여 POST 메서드로 요청을 보냈습니다.

응답 바디에 존재하는 `access-token`을 반환하도록 메서드를 작성했고, REST 요청 중이나 응답에 문제가 발생하게 되면 적절한 예외 메세지를 반환하도록 하였습니다.

```java
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
```

사용자의 이메일을 가져오는 메서드입니다. 토큰을 발급받는 메서드로부터 얻은 토큰을 활용하여 GET 요청을 보냅니다. [사용자 정보를 가져오는 API](https://developers.kakao.com/tool/rest-api/open/get/v2-user-me)를 참고하여 해당 코드를 작성할 수 있었습니다.

사용자는 이메일 정보 제공에 동의하게 되고, 응답은 이메일을 포함시켜 전달하고, 메서드는 이를 토대로 얻은 이메일을 반환합니다.

```java
String kakaoAccessToken = requestToken(loginRequest.authorizationCode());
String email = getUserEmail(kakaoAccessToken);

Member member = memberRepository.findByEmail(email)
    .orElseGet(() -> memberRepository.save(new Member(email)));

String accessToken = jwtUtil.createAccessToken(member);

return new JwtResponse(
    accessToken,
    member.getId()
);
```

위 두 개의 메서드를 호출하여 얻은 사용자의 이메일로 가입 여부를 조회합니다.

가입되어있지 않은 회원이라면 DB에 이메일을 저장하여 회원가입을 하도록 작성했습니다. 이 때 회원가입은 로직을 간소화하여 단순하게 repository의 save 메서드를 호출하는 것으로 하였습니다.

이를 토대로 얻은 회원 정보로 토큰을 생성하고, jwt를 포함한 응답을 반환합니다.

카카오로 로그인을 하여 회원가입을 하는 회원은 비밀번호가 필요없기 때문에, 비밀번호는 null로 두었고, db schema도 password의 not null 제약조건을 삭제하였습니다.
