package eckofox.efbox.user;

import eckofox.efbox.exception.EmailNotSentException;
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
            throws IllegalAccessException, EmailNotSentException {
        return ResponseEntity.status(202).body(adminService.requestAdminStatus(user, secret));
    }

    @PutMapping("/request-log-access")
    public ResponseEntity<?> requestLogAccess(@AuthenticationPrincipal User user, @RequestBody String secret)
            throws IllegalAccessException, EmailNotSentException {
        return ResponseEntity.status(202).body(adminService.requestLogAccess(user, secret));
    }

    @PutMapping("/grant-admin-status")
    public ResponseEntity<?> grantAdminStatus(@AuthenticationPrincipal User owner, @RequestBody String userId) {
        return ResponseEntity.ok().body(adminService.grantAdminStatus(owner, UUID.fromString(userId)));
    }

    @PutMapping("/grant-log-access")
    public ResponseEntity<?> grantLogAccess(@AuthenticationPrincipal User admin, @RequestBody String userId) {
        return ResponseEntity.ok().body(adminService.grantLogAccess(admin, UUID.fromString(userId)));
    }

    @DeleteMapping("/revoke-admin-status")
    public ResponseEntity<?> revokeAdminStatus(@AuthenticationPrincipal User user, @RequestBody String revokedId) {
        return ResponseEntity.status(202).body(adminService.revokeAdminStatus(user, UUID.fromString(revokedId)));
    }

    @DeleteMapping("/revoke-log-access")
    public ResponseEntity<?> revokeLogAccess(@AuthenticationPrincipal User user, @RequestBody String revokedId) {
        return ResponseEntity.status(202).body(adminService.revokeLogAccess(user, UUID.fromString(revokedId)));
    }
}
