package eckofox.EFbox.fileobjects.efboxfolder;


import eckofox.EFbox.user.User;
import eckofox.EFbox.user.UserController;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@AllArgsConstructor
@RequestMapping("/folder")
@Tag(name = "Folder")
public class EFBoxFolderController {
    private EFBoxFolderService folderService;

    /**
     * sends folder name to service for folder creation
     * non-cacheable
     *
     * @param user will be used to check access rights in Service
     * @param folderName sent to folder id
     * @param parentFolderID to locate new folder location (0 if root)
     * @return EntityModel with hypermedia or error message
     */
    @SuppressWarnings("DataFlowIssue")
    @PostMapping("/create")
    public ResponseEntity<?> createFolder(@RequestParam String folderName, @AuthenticationPrincipal User user,
                                          @RequestParam String parentFolderID) {
        try {
            EFBoxFolder efBoxFolder = folderService.createFolder(folderName, user, parentFolderID);

            EntityModel<EFBoxFolderDTO> efBoxFolderDTOEntityModel =
                    EntityModel.of(EFBoxFolderDTO.fromEFBoxFolder(efBoxFolder));

            if (!efBoxFolderDTOEntityModel.getContent().getParentFolderName().equals("")) {
                Link parentFolderLink = WebMvcLinkBuilder.linkTo(
                                WebMvcLinkBuilder.methodOn(EFBoxFolderController.class)
                                        .seeFolderContent(efBoxFolder.getParentFolder().getFolderID().toString(), user))
                        .withRel("parentFolder");
                efBoxFolderDTOEntityModel.add(parentFolderLink);
            }

            Link userLink = WebMvcLinkBuilder.linkTo(
                            WebMvcLinkBuilder.methodOn(UserController.class).showUserInfo(user))
                    .withRel("owner");

            efBoxFolderDTOEntityModel.add(userLink);

            return ResponseEntity.ok().body(efBoxFolderDTOEntityModel);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (IllegalAccessException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    /**
     * sends the folderID to service where user ownership is checked. Model is returned here then converted to
     * EntityModel
     * non-cacheable
     *
     * @param folderID of folder to be shown
     * @param user to check ownership in service
     * @return EntityModel with hypermedia or error message
     */
    @GetMapping("/browse")
    public ResponseEntity<?> seeFolderContent(@RequestParam String folderID, @AuthenticationPrincipal User user) {
        try {
            EFBoxFolder efBoxFolder = folderService.seeFolderContent(folderID, user);

            EntityModel<EFBoxFolderDTO> efBoxFolderDTOEntityModel =
                    EntityModel.of(EFBoxFolderDTO.fromEFBoxFolder(efBoxFolder));

            if (!efBoxFolderDTOEntityModel.getContent().getParentFolderName().isEmpty()) {
                Link parentFolderLink = WebMvcLinkBuilder.linkTo(
                                WebMvcLinkBuilder.methodOn(EFBoxFolderController.class)
                                        .seeFolderContent(efBoxFolder.getParentFolder().getFolderID().toString(), user))
                        .withRel("parentFolder");
                efBoxFolderDTOEntityModel.add(parentFolderLink);
            }

            Link userLink = WebMvcLinkBuilder.linkTo(
                            WebMvcLinkBuilder.methodOn(UserController.class).showUserInfo(user))
                    .withRel("owner");

            efBoxFolderDTOEntityModel.add(userLink);

            return ResponseEntity.ok(efBoxFolderDTOEntityModel);
        } catch (IllegalAccessException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    /**
     * search any file- or folderName matching the query and returns the results as a searchResponseDTO.
     * For cleaner code I convert the return into DTO in EFBoxService with stream.
     *
     * @param query a string trying to be matched in EFBoxFolderRepository and EFBoxFileRepository
     * @param user  used to filter out matches not belonging to the user
     * @return EntityModel with hypermedia or error message
     */
    @GetMapping("/search/{query}")
    public ResponseEntity<?> searchWithQuery(@PathVariable String query, @AuthenticationPrincipal User user) {
        try {
            SearchResponseDTO searchResponseDTO = folderService.searchInAllFolders(query, user);

            EntityModel<SearchResponseDTO> searchResponseDTOEntityModel = EntityModel.of(searchResponseDTO);

            Link userLink = WebMvcLinkBuilder.linkTo(
                            WebMvcLinkBuilder.methodOn(UserController.class).showUserInfo(user))
                    .withRel("owner");

            searchResponseDTOEntityModel.add(userLink);

            return ResponseEntity.ok(searchResponseDTOEntityModel);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * sends folderID of folder to be deleted to service for deletion. User ownership checked in service
     *
     * @param folderID of folder to be deleted
     * @param user to check ownership
     * @return String message
     */
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

    /**
     * sends new folder name and folderID to be updated to service. User ownership is then checked before updating name
     * non-cacheable
     *
     * @param user to check folder ownership
     * @param folderID of folder to be updated
     * @param newName of the folder
     * @return EntityModel with hypermedia or error message
     */
    @PutMapping("/change-name")
    public ResponseEntity<?> changeFolderName(@AuthenticationPrincipal User user, @RequestParam String folderID, @RequestParam String newName) {
        try {
            EFBoxFolder efBoxFolder = folderService.changeFolderName(folderID, newName, user);

            EntityModel<EFBoxFolderDTO> efBoxFolderDTOEntityModel =
                    EntityModel.of(EFBoxFolderDTO.fromEFBoxFolder(efBoxFolder));

            if (!efBoxFolderDTOEntityModel.getContent().getParentFolderName().equals("")) {
                Link parentFolderLink = WebMvcLinkBuilder.linkTo(
                                WebMvcLinkBuilder.methodOn(EFBoxFolderController.class)
                                        .seeFolderContent(efBoxFolder.getParentFolder().getFolderID().toString(), user))
                        .withRel("parentFolder");
                efBoxFolderDTOEntityModel.add(parentFolderLink);
            }

            Link userLink = WebMvcLinkBuilder.linkTo(
                            WebMvcLinkBuilder.methodOn(UserController.class).showUserInfo(user))
                    .withRel("owner");

            efBoxFolderDTOEntityModel.add(userLink);
            return ResponseEntity.ok(efBoxFolderDTOEntityModel);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (IllegalAccessException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.unprocessableEntity().body(e.getMessage());
        }
    }
}


