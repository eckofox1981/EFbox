package eckofox.efbox.security.validation.filevalidation.web;

import eckofox.efbox.security.validation.filevalidation.detector.*;
import eckofox.efbox.security.validation.filevalidation.sanitizer.DocumentSanitizer;
import eckofox.efbox.security.validation.filevalidation.sanitizer.ImageDocumentSanitizer;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.util.HexFormat;

@SuppressWarnings({"serial", "boxing"})
@WebServlet("/upload")
@MultipartConfig(fileSizeThreshold = 1024 * 1024 * 2, maxFileSize = 1024 * 1024 * 10, maxRequestSize = 1024 * 1024 * 50)
public class DocumentUpload extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        File tmpFile = null;
        Path tmpPath = null;
        try {

            /* Step 1: Retrieve upload information (file type + file content) */
            // File type: Word / Excel / Pdf
            String fileType = req.getParameter("fileType") == null ? "" : req.getParameter("fileType");
            if ((fileType == null) || (fileType.trim().length() == 0)) {
                throw new IllegalArgumentException("Unknown file type specified !");
            }

            // File content
            Part filePart = req.getPart("fileContent");
            if ((filePart == null) || (filePart.getInputStream() == null)) {
                throw new IllegalArgumentException("Unknown file content specified !");
            }

            // Write a temporary file with uploaded file
            tmpFile = File.createTempFile("uploaded-", null);
            tmpPath = tmpFile.toPath();
            long copiedBytesCount = Files.copy(filePart.getInputStream(), tmpPath, StandardCopyOption.REPLACE_EXISTING);
            if (copiedBytesCount != filePart.getSize()) {
                throw new IOException(String.format("Error during stream copy to temporary disk (copied: %s / expected: %s !", copiedBytesCount, filePart.getSize()));
            }

            /* Step 2: Initialize a detector/sanitizer for the target file type and perform validation */
            boolean isSafe;

            // Instantiate the dedicated detector/sanitizer implementation and apply detection/sanitizing
            DocumentDetector documentDetector;
            DocumentSanitizer documentSanitizer;
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
                case "IMAGE":
                    documentSanitizer = new ImageDocumentSanitizer();
                    isSafe = documentSanitizer.madeSafe(tmpFile);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown file type specified.");
            }

            /* Step 3 : Take decision based on sfa status detected */
            // Take action is the file is not safe
            if (!isSafe) {
                // Remove temporary file
                safelyRemoveFile(tmpPath);
                // Return error
            } else {
                // Here print file infos...
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.setContentType("text/plain");
                // For submitted file name, if you are on a container using Servlet API 3.1 then you can use method
                // filePart.getSubmittedFileName() instead of my workaround method....
                resp.getWriter().printf("Submitted file name       : %s\n", extractSubmittedFileName(filePart));
                resp.getWriter().printf("Submitted file size       : %s\n", filePart.getSize());
                resp.getWriter().printf("Received temp file name   : %s\n", tmpFile.getName());
                resp.getWriter().printf("Received temp file path   : %s\n", tmpFile.getAbsolutePath());
                // Create a HASH of the file to check the integrity of the uploaded content
                byte[] content = Files.readAllBytes(tmpPath);
                MessageDigest digester = MessageDigest.getInstance("sha-256");
                byte[] hash = digester.digest(content);
                String hashHex = HexFormat.of().formatHex(hash);
                resp.getWriter().printf("Received temp file SHA256 : %s\n", hashHex);
            }

        } catch (Exception e) {
            // Remove temporary file
            safelyRemoveFile(tmpPath);

            throw new FileNotFoundException("Error during detection of file upload safe status: " + e);
        }

    }

    /**
     * Utility methods to safely remove a file
     *
     * @param p file to remove
     */
    private static void safelyRemoveFile(Path p) throws IOException {
        try {
            if (p != null) {
                // Remove temporary file
                if (!Files.deleteIfExists(p)) {
                    // If remove fail then overwrite content to sanitize it
                    Files.write(p, "-".getBytes("utf8"), StandardOpenOption.CREATE);
                }
            }
        } catch (Exception e) {
            throw new IOException("Could not safely overwrite file content for " + p.toString());
        }
    }

    /**
     * Utility method (taken from Oracle site) to retrieve the original name of the submitted file.<br>
     * Only useful when your container run version of Servlet API inferior to 3.1 <br>
     * It's the case here because I use the Tomcat 7 Maven plugin and it use Servlet API version 3.0 <br>
     * Unfortunately I haven't found any Maven plugin for Tomcat 8...
     *
     * @param part Multipart file part
     * @return The file name or null if not found
     * @see "https://tomcat.apache.org/whichversion.html"
     * @see "https://docs.oracle.com/javaee/6/tutorial/doc/glraq.html"
     */
    private static String extractSubmittedFileName(final Part part) {
        for (String content : part.getHeader("content-disposition").split(";")) {
            if (content.trim().startsWith("filename")) {
                return content.substring(content.indexOf('=') + 1).trim().replace("\"", "");
            }
        }
        return null;
    }
}
