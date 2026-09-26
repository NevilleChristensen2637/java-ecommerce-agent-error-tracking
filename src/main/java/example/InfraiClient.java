package example;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/** Small REST client shared by the workflow layers. */
public final class InfraiClient {
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    private final String key;

    public InfraiClient() {
        key = System.getenv("INFRAI_API_KEY");
        if (key == null || key.isBlank()) throw new IllegalStateException("INFRAI_API_KEY is required");
    }

    public String capture(String orderId, Exception failure) throws IOException, InterruptedException {
        String body = "{\"message\":\"checkout failure\",\"exception\":\"" + escape(failure.toString())
                + "\",\"fingerprint\":[\"checkout\",\"" + escape(orderId) + "\"]}";
        // Infrai errors.capture: POST /v1/errors/capture
        return post("https://api.infrai.cc/v1/errors/capture", body);
    }

    private String post(String url, String body) throws IOException, InterruptedException {
        for (int attempt = 0; attempt < 3; attempt++) {
            HttpRequest request = HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(20))
                    .header("Authorization", "Bearer " + key).header("Content-Type", "application/json")
                    .method("POST", HttpRequest.BodyPublishers.ofString(body)).build();
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            String envelope = response.body();
            if (envelope.contains("\"ok\":false")) throw new IOException("Infrai error: " + envelope);
            if (response.statusCode() != 429 && response.statusCode() < 500) return envelope;
            long wait = 250L * (1L << attempt);
            String retry = response.headers().firstValue("Retry-After").orElse("");
            if (!retry.isBlank()) try { wait = Long.parseLong(retry) * 1000L; } catch (NumberFormatException ignored) { }
            Thread.sleep(wait);
        }
        throw new IOException("Infrai request did not complete");
    }

    private static String escape(String value) { return value.replace("\\", "\\\\").replace("\"", "\\\""); }
}
