package gift;

import static org.assertj.core.api.Assertions.assertThat;

import gift.jwt.JwtResponse;
import gift.member.domain.enums.UserRole;
import gift.member.dto.LogInRequest;
import gift.member.dto.MemberResponse;
import gift.member.dto.RegisterRequest;
import gift.member.repository.MemberRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class MemberControllerTest {

    @LocalServerPort
    private int port;

    private RestClient restClient;

    String baseURL;

    @Autowired
    private MemberRepository memberRepository;

    private RegisterRequest mockRegisterRequest;
    private LogInRequest mockLogInRequest;

    @BeforeEach
    void setUp() {
        baseURL = "http://localhost:" + port + "/api/members";
        restClient = RestClient.builder().baseUrl(baseURL).build();

        mockRegisterRequest = new RegisterRequest(
            "test@gmail.com",
            "test1234",
            UserRole.NORMAL
        );

        mockLogInRequest = new LogInRequest(
            "test@gmail.com",
            "test1234"
        );
    }

    @AfterEach
    void tearDown() {
        memberRepository.deleteAll();
    }

    @Test
    void 회원가입_테스트() {
        // given

        // when
        var response = restClient.post()
            .uri("/register")
            .body(mockRegisterRequest)
            .retrieve()
            .toEntity(JwtResponse.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        JwtResponse jwtResponse = response.getBody();
        assertThat(jwtResponse).isNotNull();
        assertThat(jwtResponse.id()).isEqualTo(1L);
    }

    @Test
    void 로그인_테스트() {
        // given
        restClient.post()
            .uri("/register")
            .body(mockRegisterRequest)
            .retrieve()
            .toEntity(JwtResponse.class);

        // when
        var response = restClient.post()
            .uri("/login")
            .body(mockLogInRequest)
            .retrieve()
            .toEntity(JwtResponse.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JwtResponse jwtResponse = response.getBody();
        assertThat(jwtResponse).isNotNull();
        assertThat(jwtResponse.id()).isEqualTo(1L);
    }

    @Test
    void 회원정보_조회_테스트() {
        // given
        restClient.post()
            .uri("/register")
            .body(mockRegisterRequest)
            .retrieve()
            .toEntity(JwtResponse.class);
        var accessToken = restClient.post()
            .uri("/login")
            .body(mockLogInRequest)
            .retrieve()
            .toEntity(JwtResponse.class)
            .getBody()
            .accessToken();

        // when
        var response = restClient.get()
            .headers(headers -> headers.setBearerAuth(accessToken))
            .retrieve()
            .toEntity(MemberResponse.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        MemberResponse memberResponse = response.getBody();
        assertThat(memberResponse).isNotNull();
        assertThat(memberResponse.id()).isEqualTo(1L);
        assertThat(memberResponse.email()).isEqualTo("test@gmail.com");
        assertThat(memberResponse.userRole()).isEqualTo(UserRole.NORMAL);
    }
}
