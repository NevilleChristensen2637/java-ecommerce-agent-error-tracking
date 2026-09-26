package example;

import java.util.Objects;

public final class CheckoutService {
    interface FailureReporter { void capture(String orderId, Exception failure); }
    public record Order(String id, boolean paymentApproved, boolean stockAvailable) { }
    public record Result(String status, String receipt) { }
    private final FailureReporter reporter;

    public CheckoutService(FailureReporter reporter) { this.reporter = Objects.requireNonNull(reporter); }

    public Result checkout(Order order) {
        try {
            if (!order.paymentApproved()) return new Result("PAYMENT_REJECTED", "");
            if (!order.stockAvailable()) return new Result("BACKORDERED", "");
            return new Result("CONFIRMED", "receipt-" + order.id());
        } catch (Exception failure) {
            reporter.capture(order.id(), failure);
            return new Result("REVIEW_REQUIRED", "");
        }
    }
}
