package gift.wishlist.service;

import gift.common.event.OrderCreateEvent;
import gift.common.event.ProductDeleteEvent;
import gift.common.exceptions.AlreadyExistsException;
import gift.common.exceptions.FailedToDeleteException;
import gift.common.exceptions.FailedToFindException;
import gift.member.domain.Member;
import gift.product.domain.Product;
import gift.product.repository.ProductRepository;
import gift.wishlist.domain.Wishlist;
import gift.wishlist.dto.WishAddRequest;
import gift.wishlist.dto.WishResponse;
import gift.wishlist.repository.WishlistRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final ProductRepository productRepository;

    public WishlistService(
        WishlistRepository wishlistRepository,
        ProductRepository productRepository
    ) {
        this.wishlistRepository = wishlistRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public WishResponse addWish(WishAddRequest wishAddRequest, Member member) {
        Long productId = wishAddRequest.productId();

        Optional<Wishlist> wishlist =
            wishlistRepository.findByMemberIdAndProductId(
                member.getId(),
                productId
            );

        if (wishlist.isPresent()) {
            throw new AlreadyExistsException("이미 위시리스트에 추가된 상품입니다.");
        }

        Product product =
            productRepository.findById(productId)
                .orElseThrow(() -> new FailedToFindException("존재하지 않는 상품입니다."));

        return convertToDTO(
            wishlistRepository.save(
                new Wishlist(
                    product,
                    member
                )
            )
        );
    }

    @Transactional(readOnly = true)
    public List<WishResponse> getWishes(Member member) {
        return wishlistRepository.findByMemberId(member.getId())
            .stream()
            .map(this::convertToDTO)
            .toList();
    }

    @Transactional
    public void delete(Long wishId, Member member) {
        Long id = wishlistRepository.getMemberIdById(wishId);

        if (!id.equals(member.getId())) {
            throw new FailedToDeleteException("삭제 권한이 없습니다.");
        }

        wishlistRepository.deleteByIdAndMemberId(wishId, member.getId());
    }

    @EventListener
    @Transactional
    public void handleProductDeleteEvent(ProductDeleteEvent event) {
        wishlistRepository.deleteByProductId(event.id());
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleOrderCreateEvent(OrderCreateEvent event) {
        wishlistRepository.deleteByProductIdAndMemberId(
            event.getProduct().getId(),
            event.getMember().getId()
        );
    }

    private WishResponse convertToDTO(Wishlist wishlist) {
        return new WishResponse(
            wishlist.getId(),
            wishlist.getProduct().getId(),
            wishlist.getMember().getId()
        );
    }
}