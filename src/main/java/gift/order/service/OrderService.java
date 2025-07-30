package gift.order.service;

import gift.common.event.OrderCreateEvent;
import gift.member.domain.Member;
import gift.order.domain.Order;
import gift.order.dto.OrderRequest;
import gift.order.dto.OrderResponse;
import gift.order.repository.OrderRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ApplicationEventPublisher eventPublisher;

    public OrderService(
        OrderRepository orderRepository,
        ApplicationEventPublisher eventPublisher) {
        this.orderRepository = orderRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public OrderResponse save(Member member, OrderRequest orderRequest) {
        eventPublisher.publishEvent(
            new OrderCreateEvent(
                orderRequest.optionId(),
                orderRequest.quantity(),
                member,
                orderRequest.message()
            )
        );

        Order order = orderRepository.save(
            new Order(
                orderRequest.quantity(),
                orderRequest.optionId(),
                member.getId(),
                orderRequest.message()
            )
        );

        return new OrderResponse(order);
    }
}
