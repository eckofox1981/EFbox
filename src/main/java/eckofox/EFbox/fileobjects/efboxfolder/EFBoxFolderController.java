package eckofox.EFbox.fileobjects.efboxfolder;

import eckofox.EFbox.fileobjects.efboxfile.EFBoxFile;
import eckofox.EFbox.fileobjects.efboxfile.EFBoxFileDTO;
import eckofox.EFbox.user.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@RestController
@AllArgsConstructor
@RequestMapping("/folder")
public class EFBoxFolderController {
    private EFBoxFolderService folderService;

    /**
     * all methods send to EFBoxFolderService which returns accordingly
     * @param user will be used to check access rights in Service
     */

    @PostMapping("/create")
    public ResponseEntity<?> createFolder(@RequestParam String folderName, @AuthenticationPrincipal User user, @RequestParam String parentFolderID) {
        try {
            EFBoxFolderDTO efBoxFolderDTO = EFBoxFolderDTO.fromEFBoxFolder(folderService.createFolder(folderName, user, parentFolderID));
            return ResponseEntity.ok().body(efBoxFolderDTO);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/browse")
    public ResponseEntity<?> seeFolderContent(@RequestParam String folderID, @AuthenticationPrincipal User user) {
        try {
            EFBoxFolderDTO efBoxFolderDTO = EFBoxFolderDTO.fromEFBoxFolder(folderService.seeFolderContent(folderID, user));
            return ResponseEntity.ok(efBoxFolderDTO);
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
            EFBoxFolder folder = folderService.deleteFolder(folderID, user);
            return ResponseEntity.ok("Folder: " + folder.getName() + " deleted.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/change-name")
    public ResponseEntity<?> changeFolderName(@AuthenticationPrincipal User user, @RequestParam String folderID, @RequestParam String newName) {
        try {
            return ResponseEntity.ok(EFBoxFolderDTO.fromEFBoxFolder(folderService.changeFolderName(folderID, newName, user)));
        } catch (Exception e) {
            return ResponseEntity.unprocessableEntity().body(e.getMessage());
        }
    }

    @NoArgsConstructor
    @Data
    class SearchResponseDTO {
        private Collection<EFBoxFolderDTO> folders = new ArrayList<>();
        private Collection<EFBoxFileDTO> files = new ArrayList<>();
    }
}


