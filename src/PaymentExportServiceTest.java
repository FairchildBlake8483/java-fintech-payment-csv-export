import java.time.Instant;

public final class PaymentExportServiceTest {
  public static void main(String[] args) {
    var small = new PaymentEvent("ok", "a", 5000, "USD", Instant.EPOCH);
    var large = new PaymentEvent("review", "a", 1_500_000, "USD", Instant.EPOCH);
    assert PaymentExportService.decide(small).allowed();
    assert !PaymentExportService.decide(large).allowed();
    System.out.println("risk decision test passed");
  }
}
