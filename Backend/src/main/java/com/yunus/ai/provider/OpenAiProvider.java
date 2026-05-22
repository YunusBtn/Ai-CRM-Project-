package com.yunus.ai.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yunus.ai.provider.dto.AiProviderRequest;
import com.yunus.ai.provider.dto.AiProviderResponse;
import com.yunus.exception.BusinessException;
import com.yunus.exception.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OpenAiProvider implements AiProvider {

    private final ObjectMapper objectMapper;
    private final RestClient openAiRestClient;


    @Override
    public AiProviderResponse generate(AiProviderRequest request) {
        try {
            // OpenAI'a gönderilecek request body hazırlanır.
            Map<String, Object> body = buildRequestBody(request);


            // OpenAI Responses API endpointine POST isteği atılır.
            String responseBody = openAiRestClient.post()
                    .uri("/responses")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(String.class);

            // OpenAI cevabından content ve varsa usage bilgisi çıkarılır.
            return parseResponse(responseBody, request);

        } catch (RestClientException ex) {
            // HTTP isteği sırasında bağlantı, timeout veya provider kaynaklı hata oluşursa buraya düşer.
            throw new BusinessException(ErrorType.AI_PROVIDER_ERROR, "OpenAI provider error: " + ex.getMessage());

        } catch (Exception ex) {
            // Response parse edilemezse veya beklenmeyen bir hata oluşursa buraya düşer.
            throw new BusinessException(ErrorType.AI_INVALID_RESPONSE, "Invalid OpenAI response");
        }
    }

    private Map<String, Object> buildRequestBody(AiProviderRequest request) {
        // OpenAI Responses API'ye gönderilecek JSON body'yi map olarak hazırlıyoruz.
        Map<String, Object> body = new HashMap<>();

        // Kullanılacak model adı. Örnek: gpt-4o-mini
        body.put("model", request.model());

        // Prompt metni. AiPromptBuilder tarafından oluşturulur.
        body.put("input", request.promt());

        // AI'ın üretebileceği maksimum output token sayısı.
        body.put("max_output_tokens", request.maxOutputTokens());

        return body;
    }

    private AiProviderResponse parseResponse(String responseBody, AiProviderRequest request) {
        try {
            // JSON string'i JsonNode ağacına çeviriyoruz.
            JsonNode root = objectMapper.readTree(responseBody);

            // OpenAI Responses API çoğu durumda output_text alanını döner.
            String content = extractOutputText(root);

            // Content boşsa beklediğimiz formatta cevap alamamışız demektir.
            if (content == null || content.isBlank()) {
                throw new BusinessException(ErrorType.AI_INVALID_RESPONSE, "OpenAI response content is empty");
            }

            // Usage bilgisi varsa gerçek token değerlerini okuyoruz.
            Integer inputTokens = getIntegerOrEstimate(
                    root.path("usage").path("input_tokens"),
                    estimateTokenCount(request.promt())
            );

            // Usage bilgisi varsa output token değerini okuyoruz.
            Integer outputTokens = getIntegerOrEstimate(
                    root.path("usage").path("output_tokens"),
                    estimateTokenCount(content)
            );

            // Uygulama içinde kullanacağımız sade response modelini dönüyoruz.
            return new AiProviderResponse(
                    content.trim(),
                    inputTokens,
                    outputTokens
            );

        } catch (BusinessException ex) {
            // Kendi fırlattığımız exception'ı tekrar aynı şekilde yukarı fırlatıyoruz.
            throw ex;

        } catch (Exception ex) {
            // JSON parse veya beklenmeyen alan problemi varsa invalid response olarak ele alıyoruz.
            throw new BusinessException(ErrorType.AI_INVALID_RESPONSE, "Invalid OpenAI response");
        }
    }

    private String extractOutputText(JsonNode root) {
        // Responses API'de pratik kullanım için output_text alanı varsa önce onu okuyoruz.
        JsonNode outputTextNode = root.path("output_text");

        // output_text varsa ve boş değilse direkt onu döndürüyoruz.
        if (!outputTextNode.isMissingNode() && !outputTextNode.asText().isBlank()) {
            return outputTextNode.asText();
        }

        /*
         * Bazı Responses API cevaplarında çıktı output array içinde gelebilir.
         * Bu yüzden fallback olarak output[...].content[...].text alanlarını geziyoruz.
         */
        JsonNode outputArray = root.path("output");

        // output alanı array değilse content çıkaramayız.
        if (!outputArray.isArray()) {
            return null;
        }

        StringBuilder builder = new StringBuilder();

        // output array içindeki her item gezilir.
        for (JsonNode outputItem : outputArray) {
            JsonNode contentArray = outputItem.path("content");

            // content alanı array değilse bu item'ı geçiyoruz.
            if (!contentArray.isArray()) {
                continue;
            }

            // content array içindeki text alanlarını topluyoruz.
            for (JsonNode contentItem : contentArray) {
                JsonNode textNode = contentItem.path("text");

                if (!textNode.isMissingNode() && !textNode.asText().isBlank()) {
                    builder.append(textNode.asText()).append("\n");
                }
            }
        }

        // Hiç text bulunamadıysa null döner.
        if (builder.isEmpty()) {
            return null;
        }

        return builder.toString().trim();
    }

    private Integer getIntegerOrEstimate(JsonNode node, Integer fallback) {
        // Usage alanı varsa gerçek token bilgisini kullanıyoruz.
        if (node != null && node.isNumber()) {
            return node.asInt();
        }

        // Usage yoksa basit tahmin değerini dönüyoruz.
        return fallback;
    }

    private Integer estimateTokenCount(String text) {
        // Gerçek tokenizer kullanmıyoruz.
        // Basit maliyet görünürlüğü için yaklaşık hesap: 1 token ≈ 4 karakter.
        if (text == null || text.isBlank()) {
            return 0;
        }

        return Math.max(1, text.length() / 4);
    }
}