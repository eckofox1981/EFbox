package eckofox.EFbox.user;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/bossmang") //this to avoid classic admin-path
@RequiredArgsConstructor
public class AdminController {
    private final AdminService adminService;

    @PutMapping("/request-bossmang-status")
    public ResponseEntity<?> requestAdminStatus(@AuthenticationPrincipal User user, @RequestBody String secret)
            throws IllegalAccessException {
        return ResponseEntity.status(202).body(adminService.requestAdminStatus(user, secret));
    }

    @PutMapping("/request-log-access")
    public ResponseEntity<?> requestLogAccess(@AuthenticationPrincipal User user, @RequestBody String secret)
            throws IllegalAccessException {
        return ResponseEntity.status(202).body(adminService.requestLogAccess(user, secret));
    }

    @DeleteMapping("/revoke-admin-status")
    public ResponseEntity<?> revokeAdminStatus(@AuthenticationPrincipal User user, @RequestParam UUID revokedId) {
        return ResponseEntity.status(202).body(adminService.revokeAdminStatus(user, revokedId));
    }

    @DeleteMapping("/revoke-log-access")
    public ResponseEntity<?> revokeLogAccess(@AuthenticationPrincipal User user, @RequestParam UUID revokedId) {
        return ResponseEntity.status(202).body(adminService.revokeLogAccess(user, revokedId));
    }
}
