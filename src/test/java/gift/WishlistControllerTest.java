package gift;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;

import gift.common.ErrorResult;
import gift.jwt.JwtResponse;
import gift.member.domain.enums.UserRole;
import gift.member.dto.RegisterRequest;
import gift.product.domain.Product;
import gift.product.repository.ProductRepository;
import gift.wishlist.dto.WishAddRequest;
import gift.wishlist.dto.WishResponse;
import gift.wishlist.repository.WishlistRepository;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class WishlistControllerTest {

    @LocalServerPort
    private int port;

    private RestClient restClient;

    String baseURL;

    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private WishlistRepository wishlistRepository;

    private String accessToken;

    @BeforeEach
    void setUp() {
        baseURL = "http://localhost:" + port + "/api";
        restClient = RestClient
            .builder()
            .baseUrl(baseURL + "/wishes")
            .build();
        for (int i = 0; i < 5; i++) {
            productRepository.save(
                new Product(
                    "상품" + i,
                    10000L * i,
                    "testURL"
                )
            );
        }

        accessToken = restClient.post()
            .uri(baseURL + "/members/register")
            .body(
                new RegisterRequest(
                    "test@gmail.com",
                    "test1234",
                    UserRole.NORMAL
                )
            )
            .retrieve()
            .toEntity(JwtResponse.class)
            .getBody()
            .accessToken();

        restClient = RestClient
            .builder()
            .baseUrl(baseURL + "/wishes")
            .defaultHeader("Authorization", "Bearer " + accessToken)
            .build();
    }

    @AfterEach
    void tearDown() {
        wishlistRepository.deleteAll();
    }

    @Test
    void 위시_상품_추가_테스트() {
        // given
        WishAddRequest wishAddRequest = new WishAddRequest(3L);

        // when
        var response = restClient.post()
            .body(wishAddRequest)
            .retrieve()
            .toEntity(WishResponse.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        WishResponse wishResponse = response.getBody();
        assertThat(wishResponse).isNotNull();
        assertThat(wishResponse.id()).isEqualTo(1L);
        assertThat(wishResponse.memberId()).isEqualTo(1L);
        assertThat(wishResponse.productId()).isEqualTo(3L);
    }

    @Test
    void 위시_상품_조회_테스트() {
        // given
        List<WishAddRequest> wishAddRequestList = new ArrayList<>();
        wishAddRequestList.add(new WishAddRequest(3L));
        wishAddRequestList.add(new WishAddRequest(4L));
        wishAddRequestList.add(new WishAddRequest(1L));
        for (WishAddRequest wishAddRequest : wishAddRequestList) {
            restClient.post()
                .body(wishAddRequest)
                .retrieve()
                .toEntity(WishResponse.class);
        }

        // when
        var response = restClient.get()
            .retrieve()
            .toEntity(new ParameterizedTypeReference<List<WishResponse>>() {
            });

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<WishResponse> wishResponses = response.getBody();
        assertThat(wishResponses).isNotNull();
        assertThat(wishResponses.size()).isEqualTo(3);

        for (int i = 0; i < wishAddRequestList.size(); i++) {
            WishAddRequest expected = wishAddRequestList.get(i);
            WishResponse actual = wishResponses.get(i);
            assertThat(actual.memberId()).isEqualTo(1L);
            assertThat(actual.productId()).isEqualTo(expected.productId());
        }
    }

    @Test
    void 위시_상품_삭제_테스트() {
        // given
        List<WishAddRequest> wishAddRequestList = new ArrayList<>();
        wishAddRequestList.add(new WishAddRequest(3L));
        wishAddRequestList.add(new WishAddRequest(4L));
        wishAddRequestList.add(new WishAddRequest(1L));
        for (WishAddRequest wishAddRequest : wishAddRequestList) {
            restClient.post()
                .body(wishAddRequest)
                .retrieve()
                .toEntity(WishResponse.class);
        }
        var beforeResponse = restClient.get()
            .retrieve()
            .toEntity(new ParameterizedTypeReference<List<WishResponse>>() {
            });
        int beforeSize = beforeResponse.getBody().size();

        // when
        var response = restClient.delete()
            .uri("/{wishId}", 1L)
            .retrieve()
            .toEntity(String.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("위시리스트 삭제가 완료되었습니다.");
        var afterResponse = restClient.get()
            .retrieve()
            .toEntity(new ParameterizedTypeReference<List<WishResponse>>() {
            });
        int afterSize = afterResponse.getBody().size();
        assertThat(afterSize).isEqualTo(beforeSize - 1);
    }

    @Test
    void 같은_상품을_위시리스트에_추가하려는_경우() {
        // given
        WishAddRequest firstRequest = new WishAddRequest(3L);
        WishAddRequest secondRequest = new WishAddRequest(3L);
        restClient.post()
            .body(firstRequest)
            .retrieve()
            .toEntity(WishResponse.class);

        // when, then
        assertThatExceptionOfType(HttpClientErrorException.Conflict.class)
            .isThrownBy(() ->
                restClient.post()
                    .body(secondRequest)
                    .retrieve()
                    .toEntity(ErrorResult.class)
            )
            .withMessageContaining(
                "이미 위시리스트에 추가된 상품입니다.");
    }

    @Test
    void 상품을_삭제했을_때_위시도_함께_삭제() {
        // given
        List<WishAddRequest> wishAddRequestList = new ArrayList<>();
        wishAddRequestList.add(new WishAddRequest(3L));
        wishAddRequestList.add(new WishAddRequest(4L));
        wishAddRequestList.add(new WishAddRequest(1L));
        for (WishAddRequest wishAddRequest : wishAddRequestList) {
            restClient.post()
                .body(wishAddRequest)
                .retrieve()
                .toEntity(WishResponse.class);
        }
        var beforeResponse = restClient.get()
            .retrieve()
            .toEntity(new ParameterizedTypeReference<List<WishResponse>>() {
            });
        int beforeSize = beforeResponse.getBody().size();

        // when
        var response = restClient.delete()
            .uri(baseURL + "/products/{productId}", 4L)
            .retrieve()
            .toEntity(String.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("상품 삭제가 완료되었습니다.");
        var afterResponse = restClient.get()
            .retrieve()
            .toEntity(new ParameterizedTypeReference<List<WishResponse>>() {
            });
        int afterSize = afterResponse.getBody().size();
        assertThat(afterSize).isEqualTo(beforeSize - 1);
    }
}
