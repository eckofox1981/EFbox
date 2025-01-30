package eckofox.EFbox.fileobjects.effile;

import eckofox.EFbox.user.User;
import lombok.AllArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/file")
@AllArgsConstructor
public class EFFileController {
    private EFFileService fileService;

    @PostMapping(path = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadFile(@RequestPart("file") MultipartFile file, @AuthenticationPrincipal User user,
            @RequestParam String parentID) {
        try {
            return ResponseEntity.ok(fileService.uploadFile(file, user, parentID));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/download")
    public ResponseEntity<?> downloadFile(@RequestParam String fileID, @AuthenticationPrincipal User user){
        try {
            EFFileDTO file = fileService.getFile(fileID, user);
            byte[] fileContent = file.getContent();
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(file.getType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
                    .body(fileContent);
        } catch (IllegalAccessException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteFile(@RequestParam String fileID, @AuthenticationPrincipal User user) {
        try {
            return ResponseEntity.ok(fileService.deleteFile(fileID, user));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/change-name")
    public ResponseEntity<?> changeFileName(@AuthenticationPrincipal User user, @RequestParam String fileID, @RequestParam String newName) {
        try {
            return ResponseEntity.ok(fileService.changeFileName(fileID, newName, user));
        } catch (Exception e) {
            return ResponseEntity.unprocessableEntity().body(e.getMessage());
        }
    }
}
