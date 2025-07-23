package gift;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import gift.common.ErrorResult;
import gift.product.dto.request.ProductSaveRequest;
import gift.product.dto.request.ProductUpdateRequest;
import gift.product.dto.response.ProductResponse;
import gift.product.repository.ProductRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ProductApiControllerTest {

    @LocalServerPort
    private int port;

    private RestClient restClient;

    String baseURL;
    
    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    void setUp() {
        baseURL = "http://localhost:" + port + "/api/products";
        restClient = RestClient.builder().baseUrl(baseURL).build();
    }

    @AfterEach
    void cleanUp() {
        productRepository.deleteAll();
    }

    @Test
    void 상품_저장_테스트() {
        ProductSaveRequest saveReqDTO = new ProductSaveRequest(
            "저장 테스트",
            115000L,
            "test"
        );
        var response = restClient.post()
            .body(saveReqDTO)
            .retrieve()
            .toEntity(ProductResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        ProductResponse productResponse = response.getBody();
        assertThat(productResponse).isNotNull();
        assertThat(productResponse.name()).isEqualTo("저장 테스트");
        assertThat(productResponse.price()).isEqualTo(115000L);
    }

    @Test
    void 글자_수를_초과한_상품_저장_테스트() {
        ProductSaveRequest saveReqDTO = new ProductSaveRequest(
            "qwertyuikjhgafhskjnjwhq",
            20000L,
            "test"
        );

        assertThatExceptionOfType(HttpClientErrorException.BadRequest.class)
            .isThrownBy(() ->
                restClient.post()
                    .body(saveReqDTO)
                    .retrieve()
                    .toEntity(ErrorResult.class)
            )
            .withMessageContaining("상품 이름은 공백을 포함하여 최대 15글자까지 입력할 수 있습니다.");
    }

    @Test
    void 허용되지_않은_문자를_상품명으로_등록() {
        ProductSaveRequest saveReqDTO = new ProductSaveRequest(
            "!@#$%@#^$&*%",
            20000L,
            "test"
        );

        assertThatExceptionOfType(HttpClientErrorException.BadRequest.class)
            .isThrownBy(() ->
                restClient.post()
                    .body(saveReqDTO)
                    .retrieve()
                    .toEntity(ErrorResult.class)
            )
            .withMessageContaining(
                "상품 이름은 한글, 영어, 숫자, 특수문자(( ), [ ], +, -, &, /, _) 외 다른 문자가 들어갈 수 없습니다.");
    }

    @Test
    void 특정_단어가_포함된_상품명_등록() {
        ProductSaveRequest saveReqDTO = new ProductSaveRequest(
            "카카오프렌즈",
            20000L,
            "test"
        );

        assertThatExceptionOfType(HttpClientErrorException.BadRequest.class)
            .isThrownBy(() ->
                restClient.post()
                    .body(saveReqDTO)
                    .retrieve()
                    .toEntity(ErrorResult.class)
            )
            .withMessageContaining(
                "'카카오'가 포함된 문구는 담당 MD와 협의한 경우에만 사용할 수 있습니다.");
    }

    @Test
    void 상품_조회_테스트() {
        ProductSaveRequest saveReqDTO = new ProductSaveRequest(
            "조회 테스트",
            20000L,
            "test"
        );

        var postResponse = restClient.post()
            .body(saveReqDTO)
            .retrieve()
            .toEntity(ProductResponse.class);

        Long id = postResponse.getBody().id();

        var getResponse = restClient.get()
            .uri("/{id}", id)
            .retrieve()
            .toEntity(ProductResponse.class);

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        ProductResponse productResponse = getResponse.getBody();
        assertThat(productResponse).isNotNull();
        assertThat(productResponse.id()).isEqualTo(id);
        assertThat(productResponse.name()).isEqualTo("조회 테스트");
        assertThat(productResponse.price()).isEqualTo(20000L);
    }

    @Test
    void 상품_수정_테스트() {
        ProductSaveRequest saveReqDTO = new ProductSaveRequest(
            "수정 테스트",
            50000L,
            "test"
        );

        var createResponse = restClient.post()
            .body(saveReqDTO)
            .retrieve()
            .toEntity(ProductResponse.class);

        Long id = createResponse.getBody().id();

        ProductUpdateRequest updateReqDTO = new ProductUpdateRequest(
            "수정됨",
            1234123L,
            null
        );

        var updateResponse = restClient.patch()
            .uri("/{id}", id)
            .body(updateReqDTO)
            .retrieve()
            .toEntity(ProductResponse.class);

        assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        ProductResponse productResponse = updateResponse.getBody();
        assertThat(productResponse).isNotNull();
        assertThat(productResponse.id()).isEqualTo(id);
        assertThat(productResponse.name()).isEqualTo("수정됨");
        assertThat(productResponse.price()).isEqualTo(1234123L);
    }

    @Test
    void 상품_삭제_테스트() {
        ProductSaveRequest saveReqDTO = new ProductSaveRequest(
            "삭제 테스트",
            30000L,
            "test"
        );

        var createResponse = restClient.post()
            .body(saveReqDTO)
            .retrieve()
            .toEntity(ProductResponse.class);

        Long id = createResponse.getBody().id();

        var deleteResponse = restClient.delete()
            .uri("/{id}", id)
            .retrieve()
            .toEntity(String.class);

        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(deleteResponse.getBody()).contains("상품 삭제가 완료되었습니다.");

        assertThatExceptionOfType(HttpClientErrorException.NotFound.class)
            .isThrownBy(
                () ->
                    restClient.get()
                        .uri("/{id}", id)
                        .retrieve()
                        .toEntity(ErrorResult.class)
            );

    }
}
