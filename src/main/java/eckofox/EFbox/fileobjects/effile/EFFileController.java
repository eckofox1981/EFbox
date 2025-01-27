package eckofox.EFbox.fileobjects.effile;

import eckofox.EFbox.user.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/file")
public class EFFileController {
    private EFFileService fileService;

    @PostMapping("/create")
    public ResponseEntity<?> addFile(@RequestBody MultipartFile file, @AuthenticationPrincipal User user, @RequestParam String parentID) {
        try {
            return ResponseEntity.ok("");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
