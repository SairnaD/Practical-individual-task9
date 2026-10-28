package bootcamp.hibernate_practical.client;

import bootcamp.hibernate_practical.exception.ExternalApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.util.Map;

@Service
public class ExternalApiClient {
    private final RestClient restClient;
    private final String token;
    private final String path;

    public ExternalApiClient(RestClient externalApiRestClient,
                             @Value("${external.api.token:}") String token,
                             @Value("${external.api.path}") String path) {
        this.restClient = externalApiRestClient;
        this.token = token;
        this.path = path;
    }

    public Map<String, Object> checkStatus() {
        if (token == null || token.isBlank()) {
            throw new ExternalApiException(HttpStatus.SERVICE_UNAVAILABLE,
                    "External API token is not configured");
        }
        try {
            Map<String, Object> body = restClient.get()
                    .uri(path)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Object>>() {});
            boolean authenticated = body != null && Boolean.TRUE.equals(body.get("authenticated"));
            return Map.of("externalApiAuthenticated", authenticated);
        } catch (RestClientResponseException e) {
            int code = e.getStatusCode().value();
            if (code == 401 || code == 403) {
                throw new ExternalApiException(HttpStatus.BAD_GATEWAY,
                        "External API rejected our credentials (" + code + ")");
            }
            throw new ExternalApiException(HttpStatus.BAD_GATEWAY,
                    "External API returned an error: " + code);
        } catch (ResourceAccessException e) {
            throw new ExternalApiException(HttpStatus.GATEWAY_TIMEOUT,
                    "External API is unreachable or timed out");
        } catch (RestClientException e) {
            throw new ExternalApiException(HttpStatus.BAD_GATEWAY,
                    "Unexpected response from External API");
        }
    }
}