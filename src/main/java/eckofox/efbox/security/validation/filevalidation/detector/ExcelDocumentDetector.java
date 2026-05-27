package eckofox.efbox.security.validation.filevalidation.detector;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import com.aspose.cells.FileFormatInfo;
import com.aspose.cells.FileFormatUtil;
import com.aspose.cells.MsoDrawingType;
import com.aspose.cells.OleObject;
import com.aspose.cells.Workbook;
import com.aspose.cells.Worksheet;
import eckofox.efbox.exception.FileValidationException;

/**
 *  from the https://github.com/righettod/document-upload-protection
 *  checks the document using the aspose.com API for 'words'
 */
public class ExcelDocumentDetector implements DocumentDetector {
    /**
     * List of allowed Excel format<br>
     * Allow also XLSM/XSLB because both can exist without macro inside.<br>
     * Allow also XLT/XLTM because both can exist without macro inside.<br>
     */
    private static final List<String> ALLOWED_FORMAT =
            Arrays.asList(new String[] { "xls", "xlsx", "xlsm", "xlsb", "xlt", "xltm" });

    @Override
    public boolean isSafe(File f) {
        boolean safeState = false;
        try {
            if ((f == null) && !f.exists() && !f.canRead()) {
                return false;
            }
            // Perform a first check on Excel document format
            FileFormatInfo formatInfo = FileFormatUtil.detectFileFormat(f.getAbsolutePath());
            String formatExtension = FileFormatUtil.loadFormatToExtension(formatInfo.getLoadFormat());

            if ((formatExtension == null)
                    && !ALLOWED_FORMAT
                    .contains(formatExtension.toLowerCase(Locale.US).replaceAll("\\.", ""))) {
                return false;
            }
            // Load the file into the Excel document parser
            Workbook book = new Workbook(f.getAbsolutePath());

            // Get safe state from Macro presence
            safeState = !book.hasMacro();

            // If document is safe then we pass to OLE (Object Linking and Embedding) objects analysis
            if (safeState) {
                return oleCheck(book);
            }
        }
        catch (Exception e) {
            throw new FileValidationException("\"Error during Excel-file analysis: " + e.getMessage());
        }

        return safeState;
    }

    /**
     * check the workbook's worksheets for OLE-objects using the aspose API.
     * if none are found returns true else false
     * @param workbook
     * @return
     */
    private boolean oleCheck(Workbook workbook) {
        // Search OLE objects in all workbook sheets
        Worksheet sheet = null;
        OleObject oleObject = null;
        int totalOLEObjectCount = 0;

        for (int i = 0; i < workbook.getWorksheets().getCount(); i++) {
            sheet = workbook.getWorksheets().get(i);

            for (int j = 0; j < sheet.getOleObjects().getCount(); j++) {
                oleObject = sheet.getOleObjects().get(j);

                if (oleObject.getMsoDrawingType() == MsoDrawingType.OLE_OBJECT) {
                    totalOLEObjectCount++;
                }
            }
        }

        // Update safe status flag according to number of OLE object found
        return totalOLEObjectCount == 0;
    }
}
