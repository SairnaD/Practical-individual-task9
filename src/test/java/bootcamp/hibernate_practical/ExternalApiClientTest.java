package bootcamp.hibernate_practical;

import bootcamp.hibernate_practical.client.ExternalApiClient;
import bootcamp.hibernate_practical.exception.ExternalApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class ExternalApiClientTest {

    private static final String BASE = "http://external.test";
    private static final String TOKEN = "test-token";

    private MockRestServiceServer server;
    private RestClient restClient;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl(BASE);
        server = MockRestServiceServer.bindTo(builder).build();
        restClient = builder.build();
    }

    private ExternalApiClient client(String token) {
        return new ExternalApiClient(restClient, token, "/bearer");
    }

    private void assertFails(ExternalApiClient client, HttpStatus expected, String messagePart) {
        assertThatThrownBy(client::checkStatus)
                .isInstanceOf(ExternalApiException.class)
                .satisfies(e -> {
                    ExternalApiException ex = (ExternalApiException) e;
                    assertThat(ex.getStatus()).isEqualTo(expected);
                    assertThat(ex.getMessage()).contains(messagePart);
                });
    }

    @Test
    void success_sendsBearerHeaderAndReturnsResult() {
        server.expect(requestTo(BASE + "/bearer"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("Authorization", "Bearer " + TOKEN))
                .andRespond(withSuccess("{\"authenticated\":true,\"token\":\"" + TOKEN + "\"}",
                        MediaType.APPLICATION_JSON));

        Map<String, Object> result = client(TOKEN).checkStatus();

        assertThat(result).containsEntry("externalApiAuthenticated", true);
        server.verify();
    }

    @Test
    void external401_isMappedTo502() {
        server.expect(requestTo(BASE + "/bearer")).andRespond(withStatus(HttpStatus.UNAUTHORIZED));

        assertFails(client(TOKEN), HttpStatus.BAD_GATEWAY, "rejected our credentials (401)");
    }

    @Test
    void external403_isMappedTo502() {
        server.expect(requestTo(BASE + "/bearer")).andRespond(withStatus(HttpStatus.FORBIDDEN));

        assertFails(client(TOKEN), HttpStatus.BAD_GATEWAY, "rejected our credentials (403)");
    }

    @Test
    void external500_isMappedTo502() {
        server.expect(requestTo(BASE + "/bearer")).andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        assertFails(client(TOKEN), HttpStatus.BAD_GATEWAY, "returned an error: 500");
    }

    @Test
    void networkFailureOrTimeout_isMappedTo504() {
        server.expect(requestTo(BASE + "/bearer"))
                .andRespond(withException(new IOException("Connection refused")));

        assertFails(client(TOKEN), HttpStatus.GATEWAY_TIMEOUT, "unreachable or timed out");
    }

    @Test
    void missingToken_isMappedTo503_andNoRequestIsSent() {
        assertFails(client(""), HttpStatus.SERVICE_UNAVAILABLE, "token is not configured");
        server.verify();
    }
}