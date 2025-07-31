package gift.common.event;

import gift.member.domain.Member;
import gift.member.domain.enums.Oauth;
import gift.product.domain.Product;

public class OrderCreateEvent {

    private final Long optionId;

    private String optionName;

    private final Long quantity;

    private Product product;

    private final Member member;

    private final String message;

    public OrderCreateEvent(Long optionId, Long quantity, Member member, String message) {
        this.optionId = optionId;
        this.quantity = quantity;
        this.member = member;
        this.message = message;
    }

    public void addOptionName(String optionName) {
        this.optionName = optionName;
    }

    public void addProductId(Product product) {
        this.product = product;
    }

    public Long getOptionId() {
        return optionId;
    }

    public String getOptionName() {
        return optionName;
    }

    public Long getQuantity() {
        return quantity;
    }

    public Member getMember() {
        return member;
    }

    public Product getProduct() {
        return product;
    }

    public String getMessage() {
        return message;
    }

    public boolean isOauthNone() {
        return member.getOauth() == Oauth.NONE;
    }

    public boolean validateToken() {
        return member.validateToken();
    }

    public String getToken() {
        return member.getAccessToken();
    }
}
