package eckofox.efbox.security.validation.filevalidation.detector;

import com.aspose.cells.Workbook;
import com.aspose.slides.IOleObjectFrame;
import com.aspose.slides.IShape;
import com.aspose.slides.ISlide;
import com.aspose.slides.Presentation;
import eckofox.efbox.exception.FileValidationException;

import java.io.File;

/**
 * checks the document using the aspose.com API for 'powerpoints'
 * based the OWASP example, Dominique Righetod's https://github.com/righettod/document-upload-protection
 */
public class PowerpointDocumentDetectorImpl implements DocumentDetector {
    @SuppressWarnings("rawtypes")
    @Override
    public boolean isSafe(File f) {
        boolean safeState = false;
        try {
            if ((f == null) && !f.exists() && !f.canRead()) {
                return false;
            }
            // Load the file into the PowerPoint document parser
            Presentation presentation = new Presentation(f.getAbsolutePath());
            // First check on PowerPoint format skipped because:
            // FileFormatInfo class is not provided for Aspose Slides API
            // PresentationFactory.getInstance().getPresentationInfo() can be used but the LoadFormat class
            // miss format like POT or PPT XML
            //Aspose API does not support PPT XML format
            // Get safe state from presence of a VBA project in the presentation
            safeState = presentation.getVbaProject() == null;

            // If presentation is safe then we pass to OLE objects analysis
            if (safeState) {
                safeState = oleCheck(presentation);
            }

        } catch (Exception e) {
            throw new FileValidationException("Error during Powerpoint file analysis: " + e);
        }

        return safeState;
    }

    /**
     * check the presentation's slides for OLE-objects using the aspose API.
     * if none are found returns true else false
     * @param presentation
     * @return
     */
    private boolean oleCheck(Presentation presentation) {
        //Parse all slides of the presentation
        int totalOLEObjectCount = 0;

        for (ISlide slide : presentation.getSlides()) {
            for (IShape shape : slide.getShapes()) {
                //Check if the current shape is an OLE object
                if (shape instanceof IOleObjectFrame) {
                    totalOLEObjectCount++;
                }
            }
        }

        // Update safe status flag according to number of OLE object found
        return totalOLEObjectCount == 0;
    }
}
