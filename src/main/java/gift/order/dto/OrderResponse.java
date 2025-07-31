package gift.order.dto;

import gift.order.domain.Order;
import java.time.LocalDateTime;

public record OrderResponse(
    Long orderId,
    Long optionId,
    Long memberId,
    Long quantity,
    String message,
    LocalDateTime orderDateTime
) {

    public OrderResponse(Order order) {
        this(
            order.getId(),
            order.getOptionId(),
            order.getMemberId(),
            order.getQuantity(),
            order.getMessage(),
            order.getOrderDateTime()
        );
    }
}
