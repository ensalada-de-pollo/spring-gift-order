package gift.wishlist.repository;

import gift.wishlist.domain.Wishlist;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, Long> {

    Optional<Wishlist> findByMemberIdAndProductId(Long memberId, Long productId);

    List<Wishlist> findByMemberId(Long memberId);

    @Query("SELECT w.member.id FROM Wishlist w WHERE w.id = :wishId")
    Long getMemberIdById(@Param("wishId") Long wishId);

    void deleteByIdAndMemberId(Long wishId, Long memberId);

    void deleteByProductId(Long productId);
}