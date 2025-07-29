package gift.option.service;

import gift.common.event.OrderCreateEvent;
import gift.common.event.ProductDeleteEvent;
import gift.common.exceptions.AlreadyExistsException;
import gift.common.exceptions.FailedToDeleteException;
import gift.common.exceptions.FailedToFindException;
import gift.common.exceptions.OutOfStockException;
import gift.option.domain.Option;
import gift.option.dto.OptionAddRequest;
import gift.option.dto.OptionResponse;
import gift.option.dto.OptionUpdateRequest;
import gift.option.repository.OptionRepository;
import gift.product.domain.Product;
import gift.product.repository.ProductRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OptionService {

    private final OptionRepository optionRepository;
    private final ProductRepository productRepository;

    public OptionService(
        OptionRepository optionRepository,
        ProductRepository productRepository) {
        this.optionRepository = optionRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public OptionResponse addOption(Long productId, OptionAddRequest optionAddRequest) {

        Optional<Option> option =
            optionRepository.findByNameAndProductId(optionAddRequest.name(), productId);

        if (option.isPresent()) {
            throw new AlreadyExistsException("이미 존재하는 옵션명입니다.");
        }

        Product product = findProduct(productId);

        return convertToDTO(
            optionRepository.save(
                new Option(
                    optionAddRequest.name(),
                    optionAddRequest.quantity(),
                    product
                )
            )
        );
    }

    @Transactional(readOnly = true)
    public List<OptionResponse> getOptions(Long productId) {
        findProduct(productId);

        return optionRepository.findByProductId(productId)
            .stream()
            .map(this::convertToDTO)
            .toList();
    }

    @Transactional
    public OptionResponse updateOption(
        Long productId,
        Long optionId,
        OptionUpdateRequest optionUpdateRequest) {
        findProduct(productId);

        Option option =
            optionRepository.findById(optionId)
                .orElseThrow(() -> new FailedToFindException("존재하지 않는 옵션입니다."));

        option.update(optionUpdateRequest.name());

        return convertToDTO(optionRepository.save(option));
    }

    @Transactional
    public void deleteOption(Long productId, Long optionId) {
        int size = optionRepository.findByProductId(productId).size();

        if (size <= 1) {
            throw new FailedToDeleteException("상품은 최소 하나의 옵션을 가지고 있어야 합니다.");
        }

        Product product = findProduct(productId);

        if (!productId.equals(product.getId())) {
            throw new FailedToDeleteException("해당하는 상품의 옵션이 아닙니다.");
        }

        optionRepository.deleteById(optionId);
    }

    @EventListener
    @Transactional
    public void handleDeleteEvent(ProductDeleteEvent event) {
        optionRepository.deleteByProductId(event.id());
    }

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

    private Product findProduct(Long productId) {
        return productRepository.findById(productId)
            .orElseThrow(() -> new FailedToFindException("존재하지 않는 상품입니다."));
    }

    private OptionResponse convertToDTO(Option option) {
        return new OptionResponse(
            option.getId(),
            option.getName(),
            option.getQuantity(),
            option.getProduct().getId()
        );
    }
}
