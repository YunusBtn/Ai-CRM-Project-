package com.yunus.ai.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yunus.ai.config.AiProperties;
import com.yunus.ai.provider.dto.AiProviderRequest;
import com.yunus.ai.provider.dto.AiProviderResponse;
import com.yunus.exception.BusinessException;
import com.yunus.exception.ErrorType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * OpenAI Responses API mock testi.
 * Referans resmi OpenAI dokumani: https://platform.openai.com/docs/api-reference/responses
 */
class OpenAiProviderMockTest {

    private OpenAiProvider openAiProvider;
    private MockRestServiceServer mockServer;
    private AiProperties aiProperties;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        aiProperties = new AiProperties();
        aiProperties.setApiKey("test-openai-key");
        aiProperties.setModel("gpt-4o-mini");
        aiProperties.setMaxOutputTokens(500);

        RestClient.Builder restClientBuilder = RestClient.builder()
                .baseUrl("https://api.openai.com/v1")
                .defaultHeader("Authorization", "Bearer " + aiProperties.getApiKey());

        mockServer = MockRestServiceServer.bindTo(restClientBuilder).build();
        RestClient restClient = restClientBuilder.build();

        openAiProvider = new OpenAiProvider(objectMapper, restClient, aiProperties);
    }

    @Test
    @DisplayName("mock test: /responses endpointine Authorization basligi ve model alani ile istek gitmeli ve resmi dokuman yaniti dogru ayristirilmali")
    void generate_WhenValidRequest_ShouldCallResponsesEndpointWithAuthAndParseOfficialResponse_Mock() {
        // OpenAI Responses API Official Response Schema (https://platform.openai.com/docs/api-reference/responses)
        String officialOpenAiResponseJson = """
                {
                  "id": "resp_673b5f3a0124819198647bc394a1d4a9",
                  "object": "response",
                  "created_at": 1731944250,
                  "status": "completed",
                  "model": "gpt-4o-mini",
                  "output_text": "Merhaba! Siparisiniz hazirlaniyor, en kisa surede kargoya verilecektir.",
                  "usage": {
                    "input_tokens": 42,
                    "output_tokens": 18,
                    "total_tokens": 60
                  }
                }
                """;

        mockServer.expect(requestTo("https://api.openai.com/v1/responses"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", "Bearer test-openai-key"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.model").value("gpt-4o-mini"))
                .andExpect(jsonPath("$.input").value("Musteri mesajina yanit onerisi uret"))
                .andExpect(jsonPath("$.max_output_tokens").value(500))
                .andRespond(withSuccess(officialOpenAiResponseJson, MediaType.APPLICATION_JSON));

        AiProviderRequest request = new AiProviderRequest(
                "Musteri mesajina yanit onerisi uret",
                "gpt-4o-mini",
                500
        );

        AiProviderResponse response = openAiProvider.generate(request);

        mockServer.verify();
        assertThat(response).isNotNull();
        assertThat(response.content()).isEqualTo("Merhaba! Siparisiniz hazirlaniyor, en kisa surede kargoya verilecektir.");
        assertThat(response.inputTokenEstimate()).isEqualTo(42);
        assertThat(response.outputTokenEstimate()).isEqualTo(18);
    }

    @Test
    @DisplayName("mock test: API anahtari bos veya tanimsiz ise HTTP 503 SERVICE_UNAVAILABLE donmeli")
    void generate_WhenApiKeyMissing_ShouldThrowAiProviderErrorServiceUnavailable_Mock() {
        aiProperties.setApiKey("");

        AiProviderRequest request = new AiProviderRequest(
                "Musteri mesajina yanit onerisi uret",
                "gpt-4o-mini",
                500
        );

        assertThatThrownBy(() -> openAiProvider.generate(request))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> {
                    BusinessException bEx = (BusinessException) ex;
                    assertThat(bEx.getErrorType()).isEqualTo(ErrorType.AI_PROVIDER_ERROR);
                    assertThat(bEx.getErrorType().getHttpStatus().value()).isEqualTo(503);
                    assertThat(bEx.getMessage()).contains("OpenAI API anahtari yapilandirilmamis");
                });
    }
}