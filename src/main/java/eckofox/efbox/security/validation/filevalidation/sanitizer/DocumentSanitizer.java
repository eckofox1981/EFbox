package eckofox.efbox.security.validation.filevalidation.sanitizer;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;

public interface DocumentSanitizer {

    /**
     * Method to try to (sanitize) disable any code contained into the specified file by using re-writing approach.
     *
     * @param f File to made safe
     *
     * @return TRUE only if the specified file has been successfully made safe.
     */
    MultipartFile sanitize(File f);
}
