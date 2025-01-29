package eckofox.EFbox.fileobjects.effolder;

import eckofox.EFbox.user.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
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
}


