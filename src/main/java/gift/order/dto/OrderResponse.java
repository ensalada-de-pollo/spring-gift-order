package gift.order.dto;

import java.time.LocalDateTime;

public record OrderResponse(
    Long orderId,
    Long optionId,
    Long memberId,
    Long quantity,
    String message,
    LocalDateTime orderDateTime
) {

}
