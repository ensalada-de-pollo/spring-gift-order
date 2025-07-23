package gift;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import gift.common.ErrorResult;
import gift.option.dto.OptionAddRequest;
import gift.option.dto.OptionResponse;
import gift.option.dto.OptionUpdateRequest;
import gift.option.repository.OptionRepository;
import gift.product.domain.Product;
import gift.product.repository.ProductRepository;
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
public class OptionControllerTest {

    @LocalServerPort
    private int port;

    private RestClient restClient;

    String baseURL;

    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private OptionRepository optionRepository;

    @BeforeEach
    void setUp() {
        baseURL = "http://localhost:" + port + "/api/products";
        restClient = RestClient
            .builder()
            .baseUrl(baseURL)
            .build();

        productRepository.save(
            new Product(
                "상품1",
                12341234L,
                "testURL"
            )
        );
    }

    @AfterEach
    void tearDown() {
        optionRepository.deleteAll();
    }

    @Test
    void 옵션_저장_테스트() {
        // given
        OptionAddRequest optionAddRequest =
            new OptionAddRequest(
                "옵션1",
                900L
            );

        // when
        var response = restClient.post()
            .uri("/{productId}/options", 1)
            .body(optionAddRequest)
            .retrieve()
            .toEntity(OptionResponse.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        OptionResponse optionResponse = response.getBody();
        assertThat(optionResponse).isNotNull();
        assertThat(optionResponse.id()).isEqualTo(1L);
        assertThat(optionResponse.name()).isEqualTo("옵션1");
        assertThat(optionResponse.quantity()).isEqualTo(900L);
        assertThat(optionResponse.productId()).isEqualTo(1L);
    }

    @Test
    void 옵션_조회_테스트() {
        // given
        List<OptionAddRequest> requests = new ArrayList<>();
        requests.add(new OptionAddRequest("옵션1", 100L));
        requests.add(new OptionAddRequest("옵션2", 200L));
        requests.add(new OptionAddRequest("옵션3", 300L));
        for (OptionAddRequest request : requests) {
            restClient.post()
                .uri("/{productId}/options", 1)
                .body(request)
                .retrieve()
                .toEntity(OptionResponse.class);
        }

        // when
        var response = restClient.get()
            .uri("/{productId}/options", 1)
            .retrieve()
            .toEntity(new ParameterizedTypeReference<List<OptionResponse>>() {
            });

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<OptionResponse> list = response.getBody();
        assertThat(list).isNotNull();
        assertThat(list.size()).isEqualTo(3);
        for (int i = 0; i < 3; i++) {
            OptionResponse optionResponse = list.get(i);
            assertThat(optionResponse).isNotNull();
            assertThat(optionResponse.id()).isNotNull();
            assertThat(optionResponse.name()).isEqualTo(list.get(i).name());
            assertThat(optionResponse.quantity()).isEqualTo(list.get(i).quantity());
            assertThat(optionResponse.productId()).isEqualTo(1L);
        }
    }

    @Test
    void 옵션_이름_변경_테스트() {
        // given
        OptionAddRequest optionAddRequest = new OptionAddRequest("수정 전", 100L);
        restClient.post()
            .uri("/{productId}/options", 1)
            .body(optionAddRequest)
            .retrieve()
            .toEntity(OptionResponse.class);

        // when
        OptionUpdateRequest optionUpdateRequest = new OptionUpdateRequest("수정 후");
        var response = restClient.patch()
            .uri("/{productId}/options/{optionId}", 1, 1)
            .body(optionUpdateRequest)
            .retrieve()
            .toEntity(OptionResponse.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        OptionResponse optionResponse = response.getBody();
        assertThat(optionResponse).isNotNull();
        assertThat(optionResponse.id()).isEqualTo(1L);
        assertThat(optionResponse.name()).isEqualTo("수정 후");
        assertThat(optionResponse.quantity()).isEqualTo(100L);
        assertThat(optionResponse.productId()).isEqualTo(1L);
    }

    @Test
    void 옵션_삭제_테스트() {
        // given
        List<OptionAddRequest> requests = new ArrayList<>();
        requests.add(new OptionAddRequest("옵션1", 100L));
        requests.add(new OptionAddRequest("옵션2", 200L));
        requests.add(new OptionAddRequest("옵션3", 300L));
        for (OptionAddRequest request : requests) {
            restClient.post()
                .uri("/{productId}/options", 1)
                .body(request)
                .retrieve()
                .toEntity(OptionResponse.class);
        }
        var beforeResponse = restClient.get()
            .uri("/{productId}/options", 1)
            .retrieve()
            .toEntity(new ParameterizedTypeReference<List<OptionResponse>>() {
            });
        int beforeSize = beforeResponse.getBody().size();

        // when
        var response = restClient.delete()
            .uri("/{productId}/options/{optionId}", 1, 2)
            .retrieve()
            .toEntity(String.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("옵션 삭제가 완료되었습니다.");
        int afterSize = restClient.get()
            .uri("/{productId}/options", 1)
            .retrieve()
            .toEntity(new ParameterizedTypeReference<List<OptionResponse>>() {
            })
            .getBody().size();
        assertThat(afterSize).isEqualTo(beforeSize - 1);
    }

    @Test
    void 잘못된_옵션명을_등록하는_경우() {
        OptionAddRequest optionAddRequest = new OptionAddRequest(
            "!@#$%@#^$&*%",
            100L
        );

        assertThatExceptionOfType(HttpClientErrorException.BadRequest.class)
            .isThrownBy(() ->
                restClient.post()
                    .uri("/{productId}/options", 1)
                    .body(optionAddRequest)
                    .retrieve()
                    .toEntity(ErrorResult.class)
            )
            .withMessageContaining(
                "옵션 이름은 한글, 영어, 숫자, 특수문자(( ), [ ], +, -, &, /, _) 외 다른 문자가 들어갈 수 없습니다.");
    }

    @Test
    void 가능한_수량을_초과하는_경우() {
        OptionAddRequest optionAddRequest = new OptionAddRequest(
            "옵션",
            100_000_001L
        );

        assertThatExceptionOfType(HttpClientErrorException.BadRequest.class)
            .isThrownBy(() ->
                restClient.post()
                    .uri("/{productId}/options", 1)
                    .body(optionAddRequest)
                    .retrieve()
                    .toEntity(ErrorResult.class)
            )
            .withMessageContaining(
                "최댓값은 100,000,000 입니다.");
    }

    @Test
    void 옵션이_하나_남았을_때_삭제를_요청하는_경우() {
        OptionAddRequest optionAddRequest = new OptionAddRequest(
            "옵션",
            100L
        );
        restClient.post()
            .uri("/{productId}/options", 1)
            .body(optionAddRequest)
            .retrieve()
            .toEntity(OptionResponse.class);

        assertThatExceptionOfType(HttpClientErrorException.Forbidden.class)
            .isThrownBy(() ->
                restClient.delete()
                    .uri("/{productId}/options/{optionId}", 1, 1)
                    .retrieve()
                    .toEntity(ErrorResult.class)
            )
            .withMessageContaining(
                "상품은 최소 하나의 옵션을 가지고 있어야 합니다.");
    }
}
