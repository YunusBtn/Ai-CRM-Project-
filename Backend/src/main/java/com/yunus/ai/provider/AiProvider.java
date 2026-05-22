package com.yunus.ai.provider;

import com.yunus.ai.provider.dto.AiProviderRequest;
import com.yunus.ai.provider.dto.AiProviderResponse;

public interface AiProvider {

    AiProviderResponse generate(AiProviderRequest request);
}
