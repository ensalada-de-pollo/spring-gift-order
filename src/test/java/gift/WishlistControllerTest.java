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
import org.junit.jupiter.api.BeforeAll;
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

    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private WishlistRepository wishlistRepository;

    private static String accessToken;
    private static Long memberId;

    private List<Product> products;

    @BeforeAll
    static void init(@LocalServerPort int port) {
        RestClient temp = RestClient.builder()
            .baseUrl("http://localhost:" + port + "/api/members/register")
            .build();

        var response = temp.post()
            .body(
                new RegisterRequest(
                    "test1234@gmail.com",
                    "test1234",
                    UserRole.NORMAL
                )
            )
            .retrieve()
            .toEntity(JwtResponse.class)
            .getBody();

        memberId = response.id();
        accessToken = response.accessToken();
    }

    @BeforeEach
    void setUp() {
        String baseURL = "http://localhost:" + port + "/api/wishes";
        products = new ArrayList<>();
        
        restClient = RestClient
            .builder()
            .baseUrl(baseURL)
            .defaultHeader("Authorization", "Bearer " + accessToken)
            .build();
        for (int i = 0; i < 5; i++) {
            products.add(productRepository.save(
                new Product(
                    "상품" + i,
                    10000L * i,
                    "testURL"
                )
            ));
        }
    }

    @AfterEach
    void tearDown() {
        wishlistRepository.deleteAll();
    }

    @Test
    void 위시_상품_추가_테스트() {
        // given
        WishAddRequest wishAddRequest = new WishAddRequest(products.getFirst().getId());

        // when
        var response = restClient.post()
            .body(wishAddRequest)
            .retrieve()
            .toEntity(WishResponse.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        WishResponse wishResponse = response.getBody();
        assertThat(wishResponse).isNotNull();
        assertThat(wishResponse.id()).isNotNull();
        assertThat(wishResponse.memberId()).isEqualTo(memberId);
        assertThat(wishResponse.productId()).isEqualTo(products.getFirst().getId());
    }

    @Test
    void 위시_상품_조회_테스트() {
        // given
        List<WishAddRequest> wishAddRequestList = new ArrayList<>();
        wishAddRequestList.add(new WishAddRequest(products.get(2).getId()));
        wishAddRequestList.add(new WishAddRequest(products.get(3).getId()));
        wishAddRequestList.add(new WishAddRequest(products.getFirst().getId()));
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
            assertThat(actual.memberId()).isEqualTo(memberId);
            assertThat(actual.productId()).isEqualTo(expected.productId());
        }
    }

    @Test
    void 위시_상품_삭제_테스트() {
        // given
        List<WishAddRequest> wishAddRequestList = new ArrayList<>();
        wishAddRequestList.add(new WishAddRequest(products.get(2).getId()));
        wishAddRequestList.add(new WishAddRequest(products.get(3).getId()));
        wishAddRequestList.add(new WishAddRequest(products.getFirst().getId()));
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
            .uri("/{wishId}", beforeResponse.getBody().getFirst().id())
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
        WishAddRequest firstRequest = new WishAddRequest(products.get(2).getId());
        WishAddRequest secondRequest = new WishAddRequest(products.get(2).getId());
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
        wishAddRequestList.add(new WishAddRequest(products.get(4).getId()));
        wishAddRequestList.add(new WishAddRequest(products.get(3).getId()));
        wishAddRequestList.add(new WishAddRequest(products.getFirst().getId()));
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
        System.out.println("상품 id: " + beforeResponse.getBody().getFirst().productId());
        var response = restClient.delete()
            .uri("http://localhost:" + port + "/api/products/{productId}",
                beforeResponse.getBody().getFirst().productId())
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
