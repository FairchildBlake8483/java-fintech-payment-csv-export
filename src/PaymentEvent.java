import java.time.Instant;

public record PaymentEvent(String id, String accountId, long amountCents, String currency, Instant occurredAt) {}
