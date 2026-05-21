package eckofox.EFbox.fileobjects.efboxfolder;


import eckofox.EFbox.user.User;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
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
                                          @RequestParam String parentFolderID)
            throws AccessDeniedException, NoSuchElementException  {
        EFBoxFolderDTO efBoxFolderDTO = EFBoxFolderDTO.fromEFBoxFolder(folderService.createFolder(folderName, user, parentFolderID));

        return ResponseEntity.ok().body(efBoxFolderDTO);

    }

    @GetMapping("/browse")
    public ResponseEntity<?> seeFolderContent(@RequestParam String folderID, @AuthenticationPrincipal User user)
            throws AccessDeniedException, NoSuchElementException  {
        EFBoxFolderDTO efBoxFolderDTO = EFBoxFolderDTO.fromEFBoxFolder(folderService.seeFolderContent(folderID, user));

        return ResponseEntity.ok(efBoxFolderDTO);
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
        return ResponseEntity.ok(folderService.searchInAllFolders(query, user));
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteFolder(@RequestParam String folderID, @AuthenticationPrincipal User user)
            throws AccessDeniedException, NoSuchElementException  {
        EFBoxFolder folder = folderService.deleteFolder(folderID, user);

        return ResponseEntity.status(202).body("Folder: " + folder.getName() + " deleted.");
    }

    @PutMapping("/change-name")
    public ResponseEntity<?> changeFolderName(
            @AuthenticationPrincipal User user, @RequestParam String folderID, @RequestParam String newName
    ) throws NoSuchElementException, AccessDeniedException {
            return ResponseEntity.ok(EFBoxFolderDTO.fromEFBoxFolder(folderService.changeFolderName(folderID, newName, user)));
    }
}


