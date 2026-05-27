package eckofox.efbox.security.validation.filevalidation.detector;

import com.aspose.slides.IOleObjectFrame;
import com.aspose.slides.IShape;
import com.aspose.slides.ISlide;
import com.aspose.slides.Presentation;
import eckofox.efbox.exception.FileValidationException;

import java.io.File;

public class PowerpointDocumentDetectorImpl implements DocumentDetector {
    @SuppressWarnings("rawtypes")
    @Override
    public boolean isSafe(File f) {
        boolean safeState = false;
        //TODO: remove nested if and if and if
        try {
            if ((f != null) && f.exists() && f.canRead()) {
                // Load the file into the Powerpoint document parser
                Presentation presentation = new Presentation(f.getAbsolutePath());
                // First check on Powerpoint format skipped because:
                // FileFormatInfo class is not provided for Aspose Slides API
                // PresentationFactory.getInstance().getPresentationInfo() can be used but the LoadFormat class miss format like POT or PPT XML
                //Aspose API do not support PPT XML format
                // Get safe state from presence of a VBA project in the presentation
                safeState = (presentation.getVbaProject() == null);
                // If presentation is safe then we pass to OLE objects analysis
                if (safeState) {
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
                    if (totalOLEObjectCount != 0) {
                        safeState = false;
                    }
                }

            }
        } catch (Exception e) {
            safeState = false;
            throw new FileValidationException("Error during Powerpoint file analysis: " + e);
        }
        return safeState;
    }
}
