package example;

public final class CheckoutServiceTest {
    public static void main(String[] args) {
        int[] reports = {0};
        CheckoutService service = new CheckoutService((id, error) -> reports[0]++);
        CheckoutService.Result result = service.checkout(new CheckoutService.Order("ord-test", true, false));
        if (!"BACKORDERED".equals(result.status()) || reports[0] != 0) throw new AssertionError(result);
        System.out.println("checkout decision test passed");
    }
}
