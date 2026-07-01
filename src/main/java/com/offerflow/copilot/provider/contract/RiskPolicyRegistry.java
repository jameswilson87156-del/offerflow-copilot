package com.offerflow.copilot.provider.contract;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class RiskPolicyRegistry {

    private final Map<ProviderTaskType, RiskPolicy> policies;

    public RiskPolicyRegistry() {
        EnumMap<ProviderTaskType, RiskPolicy> next = new EnumMap<>(ProviderTaskType.class);
        for (ProviderTaskType taskType : ProviderTaskType.values()) {
            next.put(taskType, basePolicy());
        }
        policies = Map.copyOf(next);
    }

    public RiskPolicy get(ProviderTaskType taskType) {
        RiskPolicy policy = policies.get(taskType);
        if (policy == null) {
            throw new IllegalArgumentException("No RiskPolicy for " + taskType);
        }
        return policy;
    }

    public RiskPolicy get(String taskType) {
        return get(ProviderTaskType.from(taskType));
    }

    private RiskPolicy basePolicy() {
        return new RiskPolicy(
                "provider-risk-policy-v1",
                List.of(
                        "Offer 概率",
                        "录取概率",
                        "保证通过",
                        "保录",
                        "实时面试",
                        "代答",
                        "真实用户",
                        "真实客户",
                        "生产级稳定接入真实模型",
                        "真实 GPT",
                        "真实 DeepSeek"),
                List.of(
                        "不得输出 Offer 概率、录取概率或保证通过。",
                        "不得声称已经生产级稳定接入真实 GPT、DeepSeek 或中转站。",
                        "不得暗示自动投递、爬虫、招聘平台 API 接入或实时面试辅助。",
                        "不得编造真实用户、真实客户、真实手机号、邮箱、身份证或聊天记录。"),
                List.of(
                        "当前是 demo/local-rule/no-op。",
                        "不发起真实外部模型调用。",
                        "所有输出仍需 Human Review。",
                        "未确认前不允许复制为最终建议。"),
                true,
                true);
    }
}
