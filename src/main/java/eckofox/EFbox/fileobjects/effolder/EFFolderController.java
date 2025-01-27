package eckofox.EFbox.fileobjects.effolder;

import eckofox.EFbox.user.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@AllArgsConstructor
@RequestMapping("/folder")
public class EFFolderController {
    private EFFolderService folderService;

    @PostMapping("/create")
    public ResponseEntity<?> createFolder(@RequestParam String folderName, @AuthenticationPrincipal User user, @RequestParam String parentFolderID) {
        try {
            return ResponseEntity.ok(folderService.createFolder(folderName, user, parentFolderID));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}

@AllArgsConstructor
@Data
class EFFolderDTO {
    private UUID folderID;
    private String name;
    private List<EFFolder> folder;
}


