package eckofox.efbox.fileobjects.efboxfile;

import eckofox.efbox.exception.NoTokenFoundException;
import eckofox.efbox.security.CookieMaker;
import eckofox.efbox.security.JWTService;
import eckofox.efbox.user.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.rmi.AccessException;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/file")
@AllArgsConstructor
public class EFBoxFileController {
    private EFBoxFileService fileService;
    private final CookieMaker cookieMaker;
    private final JWTService jwtService;


    /**
     * receives the request and passes it to Service
     *
     * @param file     spring's multipartfile format for practical handling
     * @param user     used later to check access to folder in Service
     * @param parentID where the file will be saved
     * @return ResponseEntity
     */
    @PostMapping(path = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadFile(
            @RequestPart("file") MultipartFile file,
            @AuthenticationPrincipal User user,
            @RequestParam String parentID,
            HttpServletResponse response,
            HttpServletRequest request
    ) throws AccessException, NoSuchElementException, IOException, NoTokenFoundException {
        EFBoxFile efBoxfile = fileService.uploadFile(file, user, parentID);

        response.addCookie(cookieMaker
                .cookieBaker(jwtService.tokenRefreshIfThreeMinutesLeft(request, user.getUserID())));
        return ResponseEntity.ok(EFBoxFileDTO.fromEFBoxFile(efBoxfile));
    }

    /**
     * receives the request and passes it to Service
     *
     * @param fileID to find the file in the database
     * @param user   to check for Illegal access in Service
     * @return ResponseEntity of the file requested or an error message
     */
    @GetMapping("/download")
    public ResponseEntity<?> downloadFile(
            @RequestParam String fileID,
            @AuthenticationPrincipal User user,
            HttpServletResponse response,
            HttpServletRequest request
    ) throws AccessException, NoSuchElementException, NoTokenFoundException {
        EFBoxFile efBoxFile = fileService.getFile(fileID, user);
        byte[] fileContent = efBoxFile.getContent();

        response.addCookie(cookieMaker
                .cookieBaker(jwtService.tokenRefreshIfThreeMinutesLeft(request, user.getUserID())));
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(efBoxFile.getType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + efBoxFile.getFileName() + "\"")
                .body(fileContent);
    }

    /**
     * receives the request and passes it to Service
     *
     * @param fileID to be erased
     * @param user   checks access rights in Service
     * @return message
     */
    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteFile(
            @RequestParam String fileID,
            @AuthenticationPrincipal User user,
            HttpServletResponse response,
            HttpServletRequest request
    ) throws NoSuchElementException, AccessException, NoTokenFoundException {

        response.addCookie(cookieMaker
                .cookieBaker(jwtService.tokenRefreshIfThreeMinutesLeft(request, user.getUserID())));
        return ResponseEntity.status(202).body(fileService.deleteFile(fileID, user).getFileName() + " deleted.");
    }

    /**
     * receives the request and passes it to Service
     *
     * @param user    for access rights in Service
     * @param fileID  to be renamed
     * @param newName self-explanatory
     * @return ResponseEntity with either FileDTO or a error response
     */
    @PutMapping("/change-name")
    public ResponseEntity<?> changeFileName(
            @AuthenticationPrincipal User user,
            @RequestParam String fileID,
            @RequestParam String newName,
            HttpServletResponse response,
            HttpServletRequest request
    ) throws AccessException, NoSuchElementException, NoTokenFoundException {

        response.addCookie(cookieMaker
                .cookieBaker(jwtService.tokenRefreshIfThreeMinutesLeft(request, user.getUserID())));
        return ResponseEntity.ok(EFBoxFileDTO.fromEFBoxFile(fileService.changeFileName(fileID, newName, user)));
    }
}
