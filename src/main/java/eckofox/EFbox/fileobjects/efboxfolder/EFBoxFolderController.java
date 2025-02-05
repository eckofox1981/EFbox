package eckofox.EFbox.fileobjects.efboxfolder;

import eckofox.EFbox.user.User;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/folder")
public class EFBoxFolderController {
    private EFBoxFolderService folderService;

    /**
     * all methods sends to EFBoxFolderService which returns accordingly
     */

    @PostMapping("/create")
    public ResponseEntity<?> createFolder(@RequestParam String folderName, @AuthenticationPrincipal User user, @RequestParam String parentFolderID) {
        try {
            return ResponseEntity.ok().body(folderService.createFolder(folderName, user, parentFolderID));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/browse")
    public ResponseEntity<?> seeFolderContent(@RequestParam String folderID, @AuthenticationPrincipal User user) {
        try {
            return ResponseEntity.ok(folderService.seeFolderContent(folderID, user));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/search/{query}")
    public ResponseEntity<?> searchWithQuery(@PathVariable String query, @AuthenticationPrincipal User user) {
        try {
            return ResponseEntity.ok(folderService.searchInAllFolders(query, user));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteFolder(@RequestParam String folderID, @AuthenticationPrincipal User user) {
        try {
            return ResponseEntity.ok(folderService.deleteFolder(folderID, user));
        } catch (IllegalAccessException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/change-name")
    public ResponseEntity<?> changeFolderName(@AuthenticationPrincipal User user, @RequestParam String folderID, @RequestParam String newName) {
        try {
            return ResponseEntity.ok(folderService.changeFolderName(folderID, newName, user));
        } catch (Exception e) {
            return ResponseEntity.unprocessableEntity().body(e.getMessage());
        }
    }
}


