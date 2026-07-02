package com.offerflow.copilot.api;

import java.util.List;

import com.offerflow.copilot.domain.ProviderStatus;
import com.offerflow.copilot.domain.ProviderTraceCenter;
import com.offerflow.copilot.provider.ProviderConfigCheck;
import com.offerflow.copilot.provider.ProviderExecutionService;
import com.offerflow.copilot.provider.ProviderResponse;
import com.offerflow.copilot.provider.ProviderRouter;
import com.offerflow.copilot.provider.ProviderSandboxRunRequest;
import com.offerflow.copilot.provider.contract.PromptContract;
import com.offerflow.copilot.provider.contract.PromptContractSummary;
import com.offerflow.copilot.provider.contract.ProviderContractService;
import com.offerflow.copilot.provider.contract.ProviderValidatedResult;
import com.offerflow.copilot.provider.contract.ProviderValidationRequest;
import com.offerflow.copilot.security.local.LocalActorContext;
import com.offerflow.copilot.security.local.LocalActorResolver;
import com.offerflow.copilot.security.local.LocalPermissionAuditService;
import com.offerflow.copilot.security.local.PermissionAction;
import com.offerflow.copilot.service.ProviderTraceService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/provider")
public class ProviderController {

    private final ProviderTraceService providerTraceService;
    private final ProviderRouter providerRouter;
    private final ProviderExecutionService providerExecutionService;
    private final ProviderContractService providerContractService;
    private final LocalActorResolver actorResolver;
    private final LocalPermissionAuditService permissionAuditService;

    public ProviderController(
            ProviderTraceService providerTraceService,
            ProviderRouter providerRouter,
            ProviderExecutionService providerExecutionService,
            ProviderContractService providerContractService,
            LocalActorResolver actorResolver,
            LocalPermissionAuditService permissionAuditService) {
        this.providerTraceService = providerTraceService;
        this.providerRouter = providerRouter;
        this.providerExecutionService = providerExecutionService;
        this.providerContractService = providerContractService;
        this.actorResolver = actorResolver;
        this.permissionAuditService = permissionAuditService;
    }

    @GetMapping("/status")
    public ProviderStatus status() {
        ProviderConfigCheck check = providerRouter.configCheck();
        return new ProviderStatus(
                check.providerMode(),
                check.openAiCompatibleConfigured(),
                check.deepSeekConfigured(),
                check.realCallEnabled(),
                check.rawResponseSave(),
                "local-rule",
                check.boundaryNotice());
    }

    @GetMapping("/settings")
    public ProviderTraceCenter.ProviderSettings settings() {
        return providerTraceService.settings();
    }

    @GetMapping("/traces")
    public ProviderTraceCenter.TraceIndex traces() {
        return providerTraceService.traces();
    }

    @GetMapping("/traces/{runId}")
    public ProviderTraceCenter.TraceRun trace(@PathVariable String runId) {
        return providerTraceService.trace(runId);
    }

    @GetMapping("/config-check")
    public ProviderConfigCheck configCheck() {
        return providerRouter.configCheck();
    }

    @PostMapping("/sandbox-run")
    public ProviderResponse sandboxRun(
            @RequestBody ProviderSandboxRunRequest request,
            @RequestHeader(value = "X-Demo-Actor", required = false) String headerActor,
            @RequestHeader(value = "X-Demo-Role", required = false) String headerRole,
            @RequestHeader(value = "X-Request-Id", required = false) String requestId) {
        ProviderSandboxRunRequest safeRequest = request == null
                ? new ProviderSandboxRunRequest(null, null, null, false, false, null, null)
                : request;
        LocalActorContext actorContext = actorResolver.resolve(
                safeRequest.actor(), safeRequest.actorRole(), headerActor, headerRole, requestId);
        permissionAuditService.requireAllowed(
                actorContext, PermissionAction.PROVIDER_SANDBOX_RUN, "PROVIDER_SANDBOX", safeRequest.taskType());
        return providerExecutionService.sandboxRun(withActor(safeRequest, actorContext));
    }

    @GetMapping("/contracts")
    public List<PromptContractSummary> contracts() {
        return providerContractService.summaries();
    }

    @GetMapping("/contracts/{taskType}")
    public PromptContract contract(@PathVariable String taskType) {
        return providerContractService.detail(taskType);
    }

    @PostMapping("/validate-response")
    public ProviderValidatedResult validateResponse(@RequestBody ProviderValidationRequest request) {
        return providerContractService.validate(request);
    }

    private ProviderSandboxRunRequest withActor(ProviderSandboxRunRequest request, LocalActorContext actorContext) {
        return new ProviderSandboxRunRequest(
                request.taskType(),
                request.inputText(),
                request.providerMode(),
                request.simulateFailure(),
                request.simulateTimeout(),
                actorContext.actor(),
                actorContext.actorRole().name());
    }
}
