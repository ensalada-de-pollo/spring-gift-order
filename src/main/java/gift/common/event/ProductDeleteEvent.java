package gift.common.event;

public record ProductDeleteEvent(Long id) {

    public static ProductDeleteEvent of(Long id) {
        return new ProductDeleteEvent(id);
    }
}
