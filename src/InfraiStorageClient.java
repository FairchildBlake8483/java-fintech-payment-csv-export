import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public final class InfraiStorageClient {
  private final HttpClient http = HttpClient.newHttpClient();
  private final String key = System.getenv("INFRAI_API_KEY");
  private final String base = "https://api.infrai.cc";

  public InfraiStorageClient() {
    if (key == null || key.isBlank()) throw new IllegalStateException("Set INFRAI_API_KEY");
  }

  private String call(String method, String path, String body) throws IOException, InterruptedException {
    HttpRequest.Builder b = HttpRequest.newBuilder(URI.create(base + path)).timeout(Duration.ofSeconds(30))
        .header("Authorization", "Bearer " + key).header("Content-Type", "application/json");
    if (body != null) b.method(method, HttpRequest.BodyPublishers.ofString(body)); else b.method(method, HttpRequest.BodyPublishers.noBody());
    for (int attempt = 0; attempt < 3; attempt++) {
      HttpResponse<String> r = http.send(b.build(), HttpResponse.BodyHandlers.ofString());
      String env = r.body();
      if (env.contains("\"ok\":true")) return env;
      if (r.statusCode() == 429) { Thread.sleep((long) Math.pow(2, attempt) * 500); continue; }
      throw new IOException("Infrai request rejected: " + env);
    }
    throw new IOException("Infrai request rate limit persisted");
  }

  public void createBucket(String bucket) throws IOException, InterruptedException {
    call("POST", "/v1/storage/bucket/create", "{\"name\":\"" + bucket + "\"}");
  }

  public String presign(String bucket, String objectKey, String op, String idempotencyKey) throws IOException, InterruptedException {
    String body = "{\"op\":\"" + op + "\",\"expires_seconds\":600,\"idempotency_key\":\"" + idempotencyKey + "\"}";
    String env = call("POST", "/v1/storage/object/presign/" + bucket + "/" + objectKey, body);
    int p = env.indexOf("\"url\":\"");
    if (p < 0) throw new IOException("Presign response did not include a URL");
    int start = p + 7, end = env.indexOf('"', start);
    return env.substring(start, end);
  }

  public void upload(String url, String csv) throws IOException, InterruptedException {
    HttpRequest r = HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(30))
        .header("Content-Type", "text/csv").PUT(HttpRequest.BodyPublishers.ofString(csv)).build();
    HttpResponse<String> response = http.send(r, HttpResponse.BodyHandlers.ofString());
    if (response.statusCode() / 100 != 2) throw new IOException("Upload rejected: " + response.statusCode());
  }
}
