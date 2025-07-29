package gift.order.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long quantity;

    @JoinColumn(name = "option_id", nullable = false)
    private Long optionId;

    @JoinColumn(name = "member_id", nullable = false)
    private Long memberId;

    private String message;

    private LocalDateTime orderDateTime;

    protected Order() {

    }

    public Order(Long quantity, Long optionId, Long memberId, String message) {
        this.quantity = quantity;
        this.optionId = optionId;
        this.memberId = memberId;
        this.message = message;
        this.orderDateTime = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Long getQuantity() {
        return quantity;
    }

    public Long getOptionId() {
        return optionId;
    }

    public Long getMemberId() {
        return memberId;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getOrderDateTime() {
        return orderDateTime;
    }
}
