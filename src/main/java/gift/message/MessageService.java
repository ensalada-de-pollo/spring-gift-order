package gift.message;

import gift.common.event.OrderCreateEvent;
import gift.common.exceptions.FailedToSendMessageException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
public class MessageService {

    private final KakaoMessageClient kakaoMessageClient;

    public MessageService(KakaoMessageClient kakaoMessageClient) {
        this.kakaoMessageClient = kakaoMessageClient;
    }

    @TransactionalEventListener
    public void handleOrderCreateEvent(OrderCreateEvent event) {
        OrderMessageRequest orderMessageRequest =
            new OrderMessageRequest(
                event.getProduct().getName(),
                event.getProduct().getImageURL(),
                event.getOptionName(),
                event.getQuantity(),
                event.getMessage()
            );

        if (event.isOauthNone()) {
            throw new FailedToSendMessageException(
                "주문이 완료되었지만, 카카오 회원이 아니므로 메세지를 전송할 수 없습니다.");
        }

        if (!event.validateToken()) {
            throw new FailedToSendMessageException("유효하지 않은 카카오 토큰입니다.");
        }

        kakaoMessageClient.sendMessage(orderMessageRequest, event.getToken());
    }
}
