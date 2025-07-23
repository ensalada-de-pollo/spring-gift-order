package gift.wishlist.service;

import gift.common.event.ProductDeleteEvent;
import gift.common.exceptions.AlreadyExistsException;
import gift.common.exceptions.FailedToDeleteException;
import gift.common.exceptions.FailedToFindException;
import gift.member.domain.Member;
import gift.member.repository.MemberRepository;
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

@Service
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final ProductRepository productRepository;
    private final MemberRepository memberRepository;

    public WishlistService(
        WishlistRepository wishlistRepository,
        ProductRepository productRepository,
        MemberRepository memberRepository
    ) {
        this.wishlistRepository = wishlistRepository;
        this.productRepository = productRepository;
        this.memberRepository = memberRepository;
    }

    @Transactional
    public WishResponse addWish(WishAddRequest wishAddRequest, Long memberId) {
        Long productId = wishAddRequest.productId();

        Optional<Wishlist> wishlist =
            wishlistRepository.findByMemberIdAndProductId(
                memberId,
                productId
            );

        if (wishlist.isPresent()) {
            throw new AlreadyExistsException("이미 위시리스트에 추가된 상품입니다.");
        }

        Product product =
            productRepository.findById(productId)
                .orElseThrow(() -> new FailedToFindException("존재하지 않는 상품입니다."));

        Member member =
            memberRepository.findById(memberId)
                .orElseThrow(() -> new FailedToFindException("존재하지 않는 회원입니다."));

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
    public List<WishResponse> getWishes(Long memberId) {
        return wishlistRepository.findByMemberId(memberId)
            .stream()
            .map(this::convertToDTO)
            .toList();
    }

    @Transactional
    public void delete(Long wishId, Long memberId) {
        Long id = wishlistRepository.getMemberIdById(wishId);

        if (!id.equals(memberId)) {
            throw new FailedToDeleteException("삭제 권한이 없습니다.");
        }

        wishlistRepository.deleteByIdAndMemberId(wishId, memberId);
    }

    @EventListener
    @Transactional
    public void handleDeleteEvent(ProductDeleteEvent event) {
        wishlistRepository.deleteByProductId(event.id());
    }

    private WishResponse convertToDTO(Wishlist wishlist) {
        return new WishResponse(
            wishlist.getId(),
            wishlist.getProduct().getId(),
            wishlist.getMember().getId()
        );
    }
}