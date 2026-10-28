package bootcamp.hibernate_practical;

import bootcamp.hibernate_practical.client.ExternalApiClient;
import bootcamp.hibernate_practical.exception.ExternalApiException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
class ExternalApiTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ExternalApiClient externalApiClient;

    @Test
    @WithMockUser(roles = "USER")
    void externalApiSuccess_returns200() throws Exception {
        when(externalApiClient.checkStatus())
                .thenReturn(Map.of("externalApiAuthenticated", true));

        mockMvc.perform(get("/api/external/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.externalApiAuthenticated").value(true));
    }

    @Test
    @WithMockUser(roles = "USER")
    void externalApiRejectsCredentials_returns502() throws Exception {
        when(externalApiClient.checkStatus())
                .thenThrow(new ExternalApiException(HttpStatus.BAD_GATEWAY,
                        "External API rejected our credentials (401)"));

        mockMvc.perform(get("/api/external/status"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.message").value("External API rejected our credentials (401)"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void externalApiUnreachable_returns504() throws Exception {
        when(externalApiClient.checkStatus())
                .thenThrow(new ExternalApiException(HttpStatus.GATEWAY_TIMEOUT,
                        "External API is unreachable or timed out"));

        mockMvc.perform(get("/api/external/status"))
                .andExpect(status().isGatewayTimeout())
                .andExpect(jsonPath("$.message").value("External API is unreachable or timed out"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void externalApiTokenMissing_returns503() throws Exception {
        when(externalApiClient.checkStatus())
                .thenThrow(new ExternalApiException(HttpStatus.SERVICE_UNAVAILABLE,
                        "External API token is not configured"));

        mockMvc.perform(get("/api/external/status"))
                .andExpect(status().isServiceUnavailable());
    }
}