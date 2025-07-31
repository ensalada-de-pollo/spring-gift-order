package gift.message;

import com.fasterxml.jackson.annotation.JsonProperty;

public record OrderMessageRequest(
    @JsonProperty("PRODUCT_NAME")
    String productName,

    @JsonProperty("PRODUCT_IMAGE")
    String productImage,

    @JsonProperty("OPTIONS")
    String options,

    @JsonProperty("QUANTITY")
    Long quantity,

    @JsonProperty("MESSAGE")
    String message
) {

}
