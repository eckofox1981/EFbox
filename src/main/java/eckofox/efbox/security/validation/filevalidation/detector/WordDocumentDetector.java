package eckofox.efbox.security.validation.filevalidation.detector;

import eckofox.efbox.exception.FileValidationException;

import com.aspose.words.Document;
import com.aspose.words.FileFormatInfo;
import com.aspose.words.FileFormatUtil;
import com.aspose.words.NodeCollection;
import com.aspose.words.NodeType;
import com.aspose.words.Shape;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

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
        //TODO: remove nested if and if and if
        try {
            if ((f != null) && f.exists() && f.canRead()) {
                // Perform a first check on Word document format
                FileFormatInfo formatInfo = FileFormatUtil.detectFileFormat(f.getAbsolutePath());
                String formatExtension = FileFormatUtil.loadFormatToExtension(formatInfo.getLoadFormat());

                if ((formatExtension != null)
                        && ALLOWED_FORMAT
                        .contains(formatExtension.toLowerCase(Locale.US).replaceAll("\\.", ""))) {
                    // Load the file into the Word document parser
                    Document document = new Document(f.getAbsolutePath());
                    // Get safe state from Macro presence
                    safeState = !document.hasMacros();

                    // If document is safe then we pass to OLE objects analysis
                    if (safeState) {
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
                        if (totalOLEObjectCount != 0) {
                            safeState = false;
                        }

                    }
                }
            }
        }
        catch (Exception e) {
            safeState = false;
            throw new FileValidationException("\"Error during Word file analysis: " + e.getMessage());
        }
        return safeState;
    }
}
