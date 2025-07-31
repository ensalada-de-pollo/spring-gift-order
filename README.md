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
headers.

add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE);
headers.

add("Authorization","Bearer "+kakaoAccessToken);

HttpEntity<Void> request = new HttpEntity<>(headers);

try{
ResponseEntity<String> response = restTemplate.exchange(
    url,
    HttpMethod.GET,
    request,
    String.class
);

    if(response.

getStatusCode().

is2xxSuccessful()){
KakaoLoginResponse kakaoLoginResponse = objectMapper.readValue(response.getBody(),
    KakaoLoginResponse.class);

String email;

        if((email =kakaoLoginResponse.

kakaoAccount().

email())==null){
    throw new

LogInFailedException("카카오 계정 이메일 정보를 찾을 수 없습니다.");
        }
```

사용자의 이메일을 가져오는 메서드입니다. 토큰을 발급받는 메서드로부터 얻은 토큰을 활용하여 GET 요청을
보냅니다. [사용자 정보를 가져오는 API](https://developers.kakao.com/tool/rest-api/open/get/v2-user-me)를 참고하여 해당
코드를 작성할 수 있었습니다.

사용자는 이메일 정보 제공에 동의하게 되고, 응답은 이메일을 포함시켜 전달하고, 메서드는 이를 토대로 얻은 이메일을 반환합니다.

```java
String kakaoAccessToken = requestToken(loginRequest.authorizationCode());
String email = getUserEmail(kakaoAccessToken);

Member member = memberRepository.findByEmail(email)
    .orElseGet(() -> memberRepository.save(new Member(email)));

String accessToken = jwtUtil.createAccessToken(member);

return new

JwtResponse(
    accessToken,
    member.getId()
);
```

위 두 개의 메서드를 호출하여 얻은 사용자의 이메일로 가입 여부를 조회합니다.

가입되어있지 않은 회원이라면 DB에 이메일을 저장하여 회원가입을 하도록 작성했습니다. 이 때 회원가입은 로직을 간소화하여 단순하게 repository의 save 메서드를 호출하는
것으로 하였습니다.

이를 토대로 얻은 회원 정보로 토큰을 생성하고, jwt를 포함한 응답을 반환합니다.

카카오로 로그인을 하여 회원가입을 하는 회원은 비밀번호가 필요없기 때문에, 비밀번호는 null로 두었고, db schema도 password의 not null 제약조건을
삭제하였습니다.

### STEP2

- `application-test.properties` 파일을 추가하여 `./gradlew build` 시 jwt.secret이 누락됨에 따라 발생하는 오류를 해결하였습니다.
- KakaoOauthClient의 두 메서드를 리팩토링하였습니다.
    - 두 메서드에서 RestTemplate을 호출하여 요청을 전송하는 부분이 중복되어  `requestApi` 메서드를 추가함으로써 공통된 부분을 메서드화 하였습니다.
    - 조건문을 수정하여 코드를 보다 간결하게 보이도록 하였습니다.
- KakaoAuthService 메서드의 `@Transcational` 옵션을 수정하였습니다.
- Kakao 로그인 후 accessToken을 전달하는 방식을 기존 parameter 방식에서 cookie 방식으로 변경하였습니다.

#### 주문하기 구현 내용

새로운 기능을 구현함에 따라 `Order` 테이블을 생성하였습니다.

```java
eventPublisher.publishEvent(  
    new OrderCreateEvent(
        orderRequest.optionId(),  
        orderRequest.quantity(),
        member,
        orderRequest.message()  
    ));
```

서비스에서는 저장을 하기 전 이벤트를 발행합니다. 주문을 수행하면서 관련된 테이블 `Option`, `Wishlist` 등의 작업을 수행하기 위함입니다.

```java

@EventListener
public synchronized void handleOrderCreateEvent(OrderCreateEvent event) {
    Option option = optionRepository.findById(event.getOptionId())
        .orElseThrow(() -> new FailedToFindException("해당 옵션이 존재하지 않습니다."));

    if (option.getQuantity() < event.getQuantity()) {
        throw new OutOfStockException("주문할 수 있는 수량을 초과하였습니다.");
    }

    option.subQuantity(event.getQuantity());

    optionRepository.save(option);

    event.addOptionName(option.getName());
    event.addProductId(option.getProduct());
}
```

option Service에서는 `@EventListener`로 발행된 이벤트를 받아 정상적으로 수행할 수 있는 주문인지 확인하는 역할을 합니다.
싱글스레드 내지 멀티스레드까지를 가정하고, `synchronized` 키워드를 붙여 동시성 문제를 해결해보려고 하였습니다.

```java

@TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
public void handleOrderCreateEvent(OrderCreateEvent event) {
    wishlistRepository.deleteByProductIdAndMemberId(
        event.getProduct().getId(),
        event.getMember().getId()
    );
}
```

wishlist Service에서는 주문하기를 완료하면서 `@TransactionalEventListener` 로 발행된 이벤트를 받아 wishlist에 저장된 위시 목록을
삭제하도록 하였습니다.

```java

@TransactionalEventListener
public void handleOrderCreateEvent(OrderCreateEvent event) {
```

Message Service에서는 주문하기를 완료하고 발행된 이벤트를 받아서 메세지를 전송하도록 하였습니다.
메세지를 전송하는 과정이 주문하기라는 과정에 크게 영향을 미치지 않게 하도록 위해 `AFTER_COMMIT` 으로 메세지가 전송되도록 하였습니다.

```java
if (event.getMember().getOauth().equals(Oauth.NONE)) {  
    throw new FailedToSendMessageException(  
        "주문이 완료되었지만, 카카오 회원이 아니므로 메세지를 전송할 수 없습니다.");  
}  
  
String kakaoAccessToken;  
  
if ((kakaoAccessToken = event.getMember().getAccessToken()) == null) {  
    throw new FailedToSendMessageException("유효하지 않은 카카오 토큰입니다.");  
}
```

이 때, 카카오 회원이 아니거나 accessToken이 존재하지 않는 경우 예외가 발생하도록 하였습니다.
