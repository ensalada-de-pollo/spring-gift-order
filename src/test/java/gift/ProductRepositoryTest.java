package gift;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import gift.product.domain.Product;
import gift.product.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
public class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Test
    public void 상품_저장_테스트() {
        Product product = new Product("상품1", 1234L, "testurl");

        Product savedProduct = productRepository.save(product);

        assertAll(
            () -> assertThat(savedProduct.getId()).isNotNull(),
            () -> assertThat(savedProduct.getName()).isEqualTo(product.getName()),
            () -> assertThat(savedProduct.getPrice()).isEqualTo(product.getPrice()),
            () -> assertThat(savedProduct.getImageURL()).isEqualTo(product.getImageURL())
        );
    }
}