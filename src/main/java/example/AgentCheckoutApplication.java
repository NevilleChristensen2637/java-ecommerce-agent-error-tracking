package example;

/** Executable Spring-style composition root for the checkout workflow. */
public final class AgentCheckoutApplication {
    public static void main(String[] args) throws Exception {
        InfraiClient client = new InfraiClient();
        CheckoutService.FailureReporter reporter = (id, failure) -> {
            try { client.capture(id, failure); } catch (Exception reportFailure) { reportFailure.printStackTrace(); }
        };
        CheckoutService service = new CheckoutService(reporter);
        CheckoutService.Result result = service.checkout(new CheckoutService.Order("ord-1007", true, true));
        reporter.capture("ord-1007", new IllegalStateException("sample checkout failure"));
        System.out.println(result.status() + " " + result.receipt());
    }
}
