import java.io.IOException;
import java.time.Instant;
import java.util.List;

public final class PaymentExportService {
  private final InfraiStorageClient storage;
  private final String bucket;

  public PaymentExportService(InfraiStorageClient storage, String bucket) throws IOException, InterruptedException {
    this.storage = storage; this.bucket = bucket; storage.createBucket(bucket);
  }

  public String export(String accountId, List<PaymentEvent> events) throws IOException, InterruptedException {
    StringBuilder csv = new StringBuilder("event_id,account_id,amount_cents,currency,occurred_at\n");
    for (PaymentEvent e : events) {
      if (!e.accountId().equals(accountId)) continue;
      csv.append(e.id()).append(',').append(e.accountId()).append(',').append(e.amountCents()).append(',')
          .append(e.currency()).append(',').append(e.occurredAt()).append('\n');
    }
    String objectKey = "exports/" + accountId + ".csv";
    String putUrl = storage.presign(bucket, objectKey, "put", "export-" + accountId);
    storage.upload(putUrl, csv.toString());
    return storage.presign(bucket, objectKey, "get", "download-" + accountId);
  }

  public static RiskDecision decide(PaymentEvent event) {
    return event.amountCents() > 1_000_000 ? new RiskDecision(false, "manual review") : new RiskDecision(true, "within policy");
  }

  public static void main(String[] args) throws Exception {
    var events = List.of(new PaymentEvent("p-1", "acct-7", 1250, "USD", Instant.parse("2026-01-02T10:15:30Z")));
    System.out.println(new PaymentExportService(new InfraiStorageClient(), "course-ledger-exports").export("acct-7", events));
  }
}
