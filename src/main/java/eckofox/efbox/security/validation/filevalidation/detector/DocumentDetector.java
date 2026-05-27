package eckofox.efbox.security.validation.filevalidation.detector;

import java.io.File;

public interface DocumentDetector {

    boolean isSafe(File f);
}
