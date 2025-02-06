package eckofox.EFbox.fileobjects.efboxfile;

import eckofox.EFbox.user.User;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/file")
@AllArgsConstructor
public class EFBoxFileController {
    private EFBoxFileService fileService;


    /**
     * receives the request and passes it to Service
     * @param file spring's multipartfile format for practical handling
     * @param user used later to check access to folder in Service
     * @param parentID where the file will be saved
     * @return message
     */
    @PostMapping(path = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadFile(@RequestPart("file") MultipartFile file, @AuthenticationPrincipal User user,
                                        @RequestParam String parentID) {
        try {
            return ResponseEntity.ok(fileService.uploadFile(file, user, parentID));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * receives the request and passes it to Service
     * @param fileID to find the file in the database
     * @param user to check for Illegal access in Service
     * @return the file requested or an error message
     */
    @GetMapping("/download")
    public ResponseEntity<?> downloadFile(@RequestParam String fileID, @AuthenticationPrincipal User user) {
        try {
            EFBoxFileDTO file = fileService.getFile(fileID, user);
            byte[] fileContent = file.getContent();
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(file.getType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
                    .body(fileContent);
        } catch (IllegalAccessException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * receives the request and passes it to Service
     * @param fileID to be erased
     * @param user checks access rights in Service
     * @return message
     */
    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteFile(@RequestParam String fileID, @AuthenticationPrincipal User user) {
        try {
            return ResponseEntity.ok(fileService.deleteFile(fileID, user));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * receives the request and passes it to Service
     * @param user for access rights in Service
     * @param fileID to be renamed
     * @param newName self-explanatory
     * @return FileDTO
     */
    @PutMapping("/change-name")
    public ResponseEntity<?> changeFileName(@AuthenticationPrincipal User user, @RequestParam String fileID, @RequestParam String newName) {
        try {
            return ResponseEntity.ok(fileService.changeFileName(fileID, newName, user));
        } catch (Exception e) {
            return ResponseEntity.unprocessableEntity().body(e.getMessage());
        }
    }
}
