package gift.wishlist.controller;

import gift.common.annotation.LogInMember;
import gift.member.domain.Member;
import gift.wishlist.dto.WishAddRequest;
import gift.wishlist.dto.WishResponse;
import gift.wishlist.service.WishlistService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/wishes")
public class WishlistController {

    private final WishlistService wishlistService;

    public WishlistController(WishlistService wishlistService) {
        this.wishlistService = wishlistService;
    }

    @PostMapping
    public ResponseEntity<WishResponse> addWish(
        @RequestBody WishAddRequest wishAddRequest,
        @LogInMember Member member) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(wishlistService.addWish(wishAddRequest, member));
    }

    @GetMapping
    public ResponseEntity<List<WishResponse>> findAll(
        @LogInMember Member member) {
        return ResponseEntity.ok(wishlistService.getWishes(member));
    }

    @DeleteMapping("/{wishId}")
    public ResponseEntity<String> deleteWish(
        @PathVariable Long wishId,
        @LogInMember Member member) {
        wishlistService.delete(wishId, member);

        return ResponseEntity.ok("위시리스트 삭제가 완료되었습니다.");
    }
}
