package eckofox.efbox.security.validation.filevalidation.detector;

import java.io.File;

import com.itextpdf.text.pdf.PdfArray;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfReader;
import eckofox.efbox.exception.FileValidationException;

/**
 *  from the https://github.com/righettod/document-upload-protection
 *  checks the document using the itextpdf-library API for 'PDF'
 */
public class PdfDocumentDetector implements DocumentDetector {
    @Override
    public boolean isSafe(File f) {
        boolean safeState = false;
        try {
            if ((f == null) && !f.exists()) {
                return false;
            }

            // Load stream in PDF parser
            // If the stream is not a PDF then exception will be thrown
            // here and safe state will be set to FALSE
            PdfReader reader = new PdfReader(f.getAbsolutePath());
            // Check 1:
            // Detect if the document contains any JavaScript code
            String jsCode = reader.getJavaScript();

            if (jsCode != null) {
                return false;
            }
            // OK no JS code then when pass to check 2:
            // Detect if the document has any embedded files
            PdfDictionary root = reader.getCatalog();
            PdfDictionary names = root.getAsDict(PdfName.NAMES);
            PdfArray namesArray = null;
            if (names != null) {
                PdfDictionary embeddedFiles = names.getAsDict(PdfName.EMBEDDEDFILES);
                namesArray = embeddedFiles.getAsArray(PdfName.NAMES);
            }
            // Get safe state from number of embedded files
            safeState = ((namesArray == null) || namesArray.isEmpty());
        } catch (Exception e) {
            throw new FileValidationException("Error during Pdf file analysis:" + e);
        }
        return safeState;
    }
}
