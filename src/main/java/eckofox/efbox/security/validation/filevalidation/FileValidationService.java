package eckofox.efbox.security.validation.filevalidation;

import eckofox.efbox.exception.FileValidationException;
import eckofox.efbox.fileobjects.efboxfile.EFBoxFile;
import eckofox.efbox.security.validation.filevalidation.detector.*;
import eckofox.efbox.security.validation.filevalidation.sanitizer.ImageSanitizerService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

/**
 * adapted from the https://github.com/righettod/document-upload-protection,
 * transformed into a Service, the validateFile method checks the request file and fileType content
 * then detects what kind of file (WORD, EXCEL, PDF, POWERPOINT or IMAGE), the detectors check if the file is safe
 * and if safe the EFBoxFile is created and returned.
 */
@Service
public class FileValidationService {

    public EFBoxFile validateFile(HttpServletRequest req, MultipartFile file) throws IOException {
        Path secureTempDir = Files.createTempDirectory(System.getenv("TEMP_DIRECTORY_FOR_FILE_VALIDATION"));  //NOSONAR intentional and believed to be safe
        File tmpFile;
        Path tmpPath = null;
        boolean isSafe;
        EFBoxFile fileToSave = new EFBoxFile();

        try {
            /* Step 1: Retrieve upload information (file type + file content) */
            // File type: Word / Excel / PDF
            String fileType = req.getParameter("fileType") == null ? "" : req.getParameter("fileType");
            if (fileType.isEmpty()) {
                throw new FileValidationException("Unknown file type specified.");
            }

            // File content
            Part filePart = req.getPart("file");
            if ((filePart == null) || (filePart.getInputStream() == null)) {
                throw new FileValidationException("Unknown file content specified.");
            }

            // Write a temporary file with uploaded file, SonarQube insists on defined directories for security
            Files.createDirectories(secureTempDir);

            tmpFile = File.createTempFile(
                    "uploaded-",
                    ".tmp",
                    secureTempDir.toFile()
            );

            tmpPath = tmpFile.toPath();

            file.transferTo(tmpPath);
            tmpFile = tmpPath.toFile();

            controlBytesCount(filePart, tmpPath);

            /* Step 2: Initialize a detector/sanitizer for the target file type and perform validation */
            if (fileType.equals("IMAGE")) {
                ImageSanitizerService imageSanitizerService = new ImageSanitizerService();
                file = imageSanitizerService.sanitize(tmpFile);
                isSafe = file != null;
            } else {
                isSafe = detectSanitizedIsSafe(fileType, tmpFile);
            }

            /* Step 3 : Take decision based on sfa status detected */
            // Take action is the file is not safe
            if (!isSafe) {
                // Remove temporary file
                throw new FileValidationException("The file uploaded was not safe.");
            } else {
                fileToSave.setFileID(UUID.randomUUID());
                fileToSave.setFilename(file.getOriginalFilename());
                fileToSave.setParentFolder(null);
                fileToSave.setContent(file.getBytes());
                fileToSave.setType(file.getContentType());
            }

        } catch (Exception e) {
            // Remove temporary file
            safelyRemoveFile(tmpPath);

            throw new FileNotFoundException("Error during detection of file upload safe status: " + e);
        } finally {
            safelyRemoveFile(tmpPath);

            if (secureTempDir != null) {
                Files.deleteIfExists(secureTempDir);
            }
        }

        return fileToSave;
    }

    private void controlBytesCount(Part filePart, Path tmpPath) throws IOException {
        long copiedBytesCount = Files.copy(filePart.getInputStream(), tmpPath, StandardCopyOption.REPLACE_EXISTING);
        if (copiedBytesCount != filePart.getSize()) {
            throw new IOException(
                    String.format(
                            "Error during stream copy to temporary disk (copied: %s / expected: %s !",
                            copiedBytesCount,
                            filePart.getSize()
                    )
            );
        }
    }

    // Instantiate the dedicated detector/sanitizer implementation and apply detection/sanitizing
    private boolean detectSanitizedIsSafe(String fileType, File tmpFile) {
        boolean isSafe;
        DocumentDetector documentDetector;
        switch (fileType) {
            case "PDF":
                documentDetector = new PdfDocumentDetector();
                isSafe = documentDetector.isSafe(tmpFile);
                break;
            case "WORD":
                documentDetector = new WordDocumentDetector();
                isSafe = documentDetector.isSafe(tmpFile);
                break;
            case "EXCEL":
                documentDetector = new ExcelDocumentDetector();
                isSafe = documentDetector.isSafe(tmpFile);
                break;
            case "POWERPOINT":
                documentDetector = new PowerpointDocumentDetectorImpl();
                isSafe = documentDetector.isSafe(tmpFile);
                break;
            default:
                throw new IllegalArgumentException("Unknown file type specified.");
        }

        return isSafe;
    }

    /**
     * Utility methods to safely remove a file
     *
     * @param p file to remove
     */
    private static void safelyRemoveFile(Path p) throws IOException {
        try {
            if (p != null && !Files.deleteIfExists(p)) {
                // If remove fail then overwrite content to sanitize it
                Files.write(p, "-".getBytes("utf8"), StandardOpenOption.CREATE);
            }
        } catch (Exception e) {
            throw new IOException("Could not safely overwrite file content for " + p + "Message:" + e.getMessage());
        }
    }


}
