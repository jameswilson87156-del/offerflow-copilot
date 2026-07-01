package com.offerflow.copilot.api;

import java.util.List;

import com.offerflow.copilot.copy.CopyPermissionAuditEvent;
import com.offerflow.copilot.copy.CopyPermissionRequest;
import com.offerflow.copilot.copy.CopyPermissionResult;
import com.offerflow.copilot.copy.CopyPermissionService;
import com.offerflow.copilot.copy.CopyTargetType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/copy-permissions")
public class CopyPermissionController {

    private final CopyPermissionService copyPermissionService;

    public CopyPermissionController(CopyPermissionService copyPermissionService) {
        this.copyPermissionService = copyPermissionService;
    }

    @PostMapping("/check")
    public CopyPermissionResult check(@RequestBody(required = false) CopyPermissionRequest request) {
        return copyPermissionService.check(request);
    }

    @GetMapping("/audit-events")
    public List<CopyPermissionAuditEvent> auditEvents(
            @RequestParam(required = false) CopyTargetType targetType,
            @RequestParam(required = false) String targetId) {
        return copyPermissionService.auditEvents(targetType, targetId);
    }
}
