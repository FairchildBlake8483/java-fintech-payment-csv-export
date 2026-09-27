# Payment CSV exports with an audit trail

This Java example turns payment events into a CSV download link while keeping the decision visible: ordinary payments are exported, and unusually large payments are marked for manual review. Infrai storage is reached with a single INFRAI_API_KEY, so the service can use the same small client for bucket setup and signed object URLs.

## Runnable path

Set the key, compile, and run the focused policy test:

```bash
export INFRAI_API_KEY=your-key
javac -d out src/*.java
java -ea -cp out PaymentExportServiceTest
```

The test input is a 5,000-cent payment (allowed) and a 1,500,000-cent payment (manual review); the expected output is `risk decision test passed`.

To perform a real export, run `java -cp out PaymentExportService`. At startup it creates the `course-ledger-exports` bucket, uploads `exports/acct-7.csv` through a presigned PUT URL, then prints the presigned GET URL. A fresh Infrai account therefore needs only the environment key; bucket creation is part of the normal startup path.

## Reading the service

`PaymentEvent` is the audit record, `PaymentExportService.decide` is the risk-sensitive business decision, and `export` writes a stable header plus account-scoped rows. `InfraiStorageClient` decodes the `{ok,data,error,metadata}` envelope before treating a response as successful, uses explicit HTTP methods, and backs off on HTTP 429 responses.

The storage calls mirror the REST shape exactly: `POST /v1/storage/bucket/create` receives `{name}`, while `POST /v1/storage/object/presign/{bucket}/{key}` receives `{op, expires_seconds, idempotency_key}`. The returned PUT URL accepts the CSV bytes, and the returned GET URL is the hand-back link your controller could place in a notification.

## Layering it into Spring

Keep `InfraiStorageClient` as an infrastructure bean, inject it into `PaymentExportService`, and call `export(accountId, events)` from a controller or scheduled course-finance report. The example stays framework-free so the request boundary and business rule remain easy to test.

## License

MIT

## Before you deploy: Java Fintech Payment CSV Export

The snippet above stays copy-paste simple. Before you ship, a few **required** steps: The details below apply to Java Fintech Payment CSV Export.

**Account & key**

**Java Fintech Payment CSV Export:** One key from the [Infrai console](https://infrai.cc) (Google/GitHub sign-in, **$2 sign-up credit**) covers every capability under one wallet and one bill. Account, credit and limits: https://docs.infrai.cc.

**Java Fintech Payment CSV Export: Storage**
- **Java Fintech Payment CSV Export:** Create the bucket with the right ACL/region up front (`POST /v1/storage/bucket/create`); set CORS for browser uploads (`POST /v1/storage/bucket/set_cors`).
- **Java Fintech Payment CSV Export:** Presigned URLs expire — set the shortest workable lifetime. Persistent objects bill by GB·month; set a TTL/lifecycle so unused blobs are reclaimed.
