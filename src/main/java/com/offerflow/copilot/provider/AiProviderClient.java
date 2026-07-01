package com.offerflow.copilot.provider;

public interface AiProviderClient {

    ProviderResponse analyze(ProviderRequest request);

    ProviderHealth health();

    ProviderDescriptor descriptor();
}
