package eckofox.efbox.security.validation.filevalidation.detector;

import eckofox.efbox.exception.FileValidationException;

import com.aspose.words.Document;
import com.aspose.words.FileFormatInfo;
import com.aspose.words.FileFormatUtil;
import com.aspose.words.NodeCollection;
import com.aspose.words.NodeType;
import com.aspose.words.Shape;

import java.io.File;
import java.util.List;
import java.util.Locale;

/**
 *  from the https://github.com/righettod/document-upload-protection
 *  checks the document using the aspose.com API for 'cells'
 */
public class WordDocumentDetector implements DocumentDetector {
    /**
     * List of allowed Word format (WML = Word ML (Word 2003 XML)).<br>
     * Allow also DOCM because it can exist without macro inside.<br>
     * Allow also DOT/DOTM because both can exist without macro inside.<br>
     * We reject MHTML file because:<br>
     * <ul>
     * <li>API cannot detect macro into this format</li>
     * <li>Is not normal to use this format to represent a Word file (there plenty of others supported format)</li>
     * </ul>
     */
    private static final List<String> ALLOWED_FORMAT = List.of("doc", "docx", "docm", "wml", "dot", "dotm");

    @SuppressWarnings("rawtypes")
    @Override
    public boolean isSafe(File f) {
        boolean safeState = false;
        try {
            if ((f == null) && !f.exists() && !f.canRead()) {
                return false;
            }

            // Perform a first check on Word document format
            FileFormatInfo formatInfo = FileFormatUtil.detectFileFormat(f.getAbsolutePath());
            String formatExtension = FileFormatUtil.loadFormatToExtension(formatInfo.getLoadFormat());

            if ((formatExtension == null || formatExtension.isBlank())
                    && !ALLOWED_FORMAT
                    .contains(formatExtension.toLowerCase(Locale.US).replaceAll("\\.", ""))) {
                return false;
            }
            // Load the file into the Word document parser
            Document document = new Document(f.getAbsolutePath());
            // Get safe state from Macro presence
            safeState = !document.hasMacros();

            // If document is safe then we pass to OLE objects analysis
            if (safeState) {
                safeState = oleCheck(document);
            }

        } catch (Exception e) {
            throw new FileValidationException("\"Error during Word file analysis: " + e.getMessage());
        }
        return safeState;
    }

    /**
     * check the word-doc's shapes for OLE-objects using the aspose API.
     * if none are found returns true else false
     * @param document
     * @return
     */
    private boolean oleCheck(Document document) {
        // Get all shapes of the document
        NodeCollection shapes = document.getChildNodes(NodeType.SHAPE, true);
        Shape shape = null;

        // Search OLE objects in all shapes
        int totalOLEObjectCount = 0;
        for (int i = 0; i < shapes.getCount(); i++) {
            shape = (Shape) shapes.get(i);
            // Check if the current shape has OLE object
            if (shape.getOleFormat() != null) {
                totalOLEObjectCount++;
            }
        }

        // Update safe status flag according to number of OLE object found
        return totalOLEObjectCount == 0;
    }
}
