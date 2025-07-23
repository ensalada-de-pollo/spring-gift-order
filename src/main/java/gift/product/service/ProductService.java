package gift.product.service;

import gift.common.event.ProductDeleteEvent;
import gift.common.exceptions.FailedToFindException;
import gift.product.domain.Product;
import gift.product.dto.request.PageFindRequest;
import gift.product.dto.request.ProductSaveRequest;
import gift.product.dto.request.ProductUpdateRequest;
import gift.product.dto.response.ProductResponse;
import gift.product.repository.ProductRepository;
import java.util.List;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ApplicationEventPublisher eventPublisher;

    public ProductService(
        ProductRepository productRepository,
        ApplicationEventPublisher eventPublisher) {
        this.productRepository = productRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public ProductResponse save(ProductSaveRequest productSaveRequest) {
        return convertToDTO(
            productRepository.save(
                new Product(
                    productSaveRequest.name(),
                    productSaveRequest.price(),
                    productSaveRequest.imageURL()
                )
            )
        );
    }

    @Transactional(readOnly = true)
    public ProductResponse findById(Long id) {
        return convertToDTO(
            productRepository.findById(id)
                .orElseThrow(() -> new FailedToFindException("존재하지 않는 상품입니다."))
        );
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> findAllProducts() {
        return productRepository.findAll()
            .stream()
            .map(this::convertToDTO)
            .toList();
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> findPage(PageFindRequest pageFindRequest) {
        Pageable pageable = PageRequest.of(
            pageFindRequest.page(),
            pageFindRequest.size(),
            Sort.by(
                pageFindRequest.direction(),
                pageFindRequest.criteria()
            )
        );

        return productRepository.findAll(pageable)
            .map(this::convertToDTO);
    }

    @Transactional
    public ProductResponse update(Long id, ProductUpdateRequest productUpdateRequest) {
        Product product =
            productRepository.findById(id)
                .orElseThrow(() -> new FailedToFindException("존재하지 않는 상품입니다."));

        product.update(
            productUpdateRequest.name(),
            productUpdateRequest.price(),
            productUpdateRequest.imageURL()
        );

        return convertToDTO(
            productRepository.save(product)
        );
    }

    @Transactional
    public void delete(Long id) {
        eventPublisher.publishEvent(ProductDeleteEvent.of(id));
        productRepository.deleteById(id);
    }

    private ProductResponse convertToDTO(Product product) {
        return new ProductResponse(
            product.getId(),
            product.getName(),
            product.getPrice(),
            product.getImageURL()
        );
    }
}