package eckofox.EFbox.fileobjects.efboxfolder;


import eckofox.EFbox.user.User;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@AllArgsConstructor
@RequestMapping("/folder")
public class EFBoxFolderController {
    private EFBoxFolderService folderService;

    /**
     * all methods send to EFBoxFolderService which returns accordingly
     *
     * @param user will be used to check access rights in Service
     */
    @PostMapping("/create")
    public ResponseEntity<?> createFolder(@RequestParam String folderName, @AuthenticationPrincipal User user,
                                          @RequestParam String parentFolderID) {
        try {
            EFBoxFolderDTO efBoxFolderDTO = EFBoxFolderDTO.fromEFBoxFolder(folderService.createFolder(folderName, user, parentFolderID));
            return ResponseEntity.ok().body(efBoxFolderDTO);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (IllegalAccessException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @GetMapping("/browse")
    public ResponseEntity<?> seeFolderContent(@RequestParam String folderID, @AuthenticationPrincipal User user) {
        try {
            EFBoxFolderDTO efBoxFolderDTO = EFBoxFolderDTO.fromEFBoxFolder(folderService.seeFolderContent(folderID, user));
            return ResponseEntity.ok(efBoxFolderDTO);
        } catch (IllegalAccessException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    /**
     * search any file- or foldername matching the query and returns the results as a searchResponseDTO.
     * For cleaner code I convert the return into DTO in EFBoxService with stream.
     *
     * @param query a string trying to be matched in EFBoxFolderRepository and EFBoxFileRepository
     * @param user  used to filter out matches not belonging to the user
     * @return ResponseEntity
     */
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
            return ResponseEntity.status(202).body("Folder: " + folder.getName() + " deleted.");
        } catch (IllegalAccessException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/change-name")
    public ResponseEntity<?> changeFolderName(@AuthenticationPrincipal User user, @RequestParam String folderID, @RequestParam String newName) {
        try {
            return ResponseEntity.ok(EFBoxFolderDTO.fromEFBoxFolder(folderService.changeFolderName(folderID, newName, user)));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (IllegalAccessException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.unprocessableEntity().body(e.getMessage());
        }
    }
}


