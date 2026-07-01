package com.offerflow.copilot.provider.contract;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class ProviderResponseSchemaRegistry {

    private final Map<ProviderTaskType, ProviderResponseSchema> schemas;

    public ProviderResponseSchemaRegistry(PromptContractRegistry promptContractRegistry) {
        EnumMap<ProviderTaskType, ProviderResponseSchema> next = new EnumMap<>(ProviderTaskType.class);
        for (PromptContract contract : promptContractRegistry.all()) {
            next.put(contract.taskType(), new ProviderResponseSchema(
                    contract.schemaVersion(),
                    contract.outputSchemaName(),
                    List.of("provider", "schemaVersion", "summary", "humanReviewRequired", "copyAllowed"),
                    List.of("evidenceRefs", "riskFlags", "fallbackReason", "boundaryNotice"),
                    2000,
                    false,
                    true));
        }
        schemas = Map.copyOf(next);
    }

    public ProviderResponseSchema get(ProviderTaskType taskType) {
        ProviderResponseSchema schema = schemas.get(taskType);
        if (schema == null) {
            throw new IllegalArgumentException("No ProviderResponseSchema for " + taskType);
        }
        return schema;
    }
}
