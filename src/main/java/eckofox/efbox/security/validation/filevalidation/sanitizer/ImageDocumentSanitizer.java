package eckofox.efbox.security.validation.filevalidation.sanitizer;

import eckofox.efbox.exception.FileValidationException;
import org.apache.commons.imaging.AbstractImageParser;
import org.apache.commons.imaging.ImageInfo;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.ImagingParameters;
import org.apache.commons.imaging.formats.bmp.BmpImageParser;
import org.apache.commons.imaging.formats.bmp.BmpImagingParameters;
import org.apache.commons.imaging.formats.dcx.DcxImageParser;
import org.apache.commons.imaging.formats.gif.GifImageParser;
import org.apache.commons.imaging.formats.gif.GifImagingParameters;
import org.apache.commons.imaging.formats.pcx.PcxImageParser;
import org.apache.commons.imaging.formats.pcx.PcxImagingParameters;
import org.apache.commons.imaging.formats.png.PngImageParser;
import org.apache.commons.imaging.formats.png.PngImagingParameters;
import org.apache.commons.imaging.formats.tiff.TiffImageParser;
import org.apache.commons.imaging.formats.tiff.TiffImagingParameters;
import org.apache.commons.imaging.formats.wbmp.WbmpImageParser;
import org.apache.commons.imaging.formats.wbmp.WbmpImagingParameters;
import org.apache.commons.imaging.formats.xbm.XbmImageParser;
import org.apache.commons.imaging.formats.xbm.XbmImagingParameters;
import org.apache.commons.imaging.formats.xpm.XpmImageParser;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;

public class ImageDocumentSanitizer implements DocumentSanitizer{
    @Override
    public boolean madeSafe(File f) {
        boolean safeState = false;
        boolean fallbackOnApacheCommonsImaging;
        //TODO: remove nested if and if and if
        try {
            if ((f != null) && f.exists() && f.canRead() && f.canWrite()) {
                //Get the image format
                String formatName;
                try (ImageInputStream iis = ImageIO.createImageInputStream(f)) {
                    Iterator<ImageReader> imageReaderIterator = ImageIO.getImageReaders(iis);
                    //If there not ImageReader instance found so it's means that the current format is not supported by the Java built-in API
                    if (!imageReaderIterator.hasNext()) {
                        ImageInfo imageInfo = Imaging.getImageInfo(f);
                        if (imageInfo != null && imageInfo.getFormat() != null && imageInfo.getFormat().getName() != null) {
                            formatName = imageInfo.getFormat().getName();
                            fallbackOnApacheCommonsImaging = true;
                        } else {
                            throw new IOException("Format of the original image is not supported for read operation.");
                        }
                    } else {
                        ImageReader reader = imageReaderIterator.next();
                        formatName = reader.getFormatName();
                        fallbackOnApacheCommonsImaging = false;
                    }
                }

                // Load the image
                BufferedImage originalImage;
                if (!fallbackOnApacheCommonsImaging) {
                    originalImage = ImageIO.read(f);
                } else {
                    originalImage = Imaging.getBufferedImage(f);
                }

                // Check that image has been successfully loaded
                if (originalImage == null) {
                    throw new IOException("Cannot load the original image.");
                }

                // Get current Width and Height of the image
                int originalWidth = originalImage.getWidth(null);
                int originalHeight = originalImage.getHeight(null);


                // Resize the image by removing 1px on Width and Height
                Image resizedImage = originalImage.getScaledInstance(originalWidth - 1, originalHeight - 1, Image.SCALE_SMOOTH);

                // Resize the resized image by adding 1px on Width and Height - In fact set image to is initial size
                Image initialSizedImage = resizedImage.getScaledInstance(originalWidth, originalHeight, Image.SCALE_SMOOTH);

                // Save image by overwriting the provided source file content
                BufferedImage sanitizedImage = new BufferedImage(initialSizedImage.getWidth(null), initialSizedImage.getHeight(null), BufferedImage.TYPE_INT_RGB);
                Graphics bg = sanitizedImage.getGraphics();
                bg.drawImage(initialSizedImage, 0, 0, null);
                bg.dispose();
                try (OutputStream fos = Files.newOutputStream(f.toPath(), StandardOpenOption.WRITE)) {
                    if (!fallbackOnApacheCommonsImaging) {
                        ImageIO.write(sanitizedImage, formatName, fos);
                    } else {
                        AbstractImageParser<?> imageParser;
                        ImagingParameters<?> params;
                        //Handle only formats for which Apache Commons Imaging can successfully write (YES in Write column of the reference link) the image format
                        //See reference link in the class header
                        switch (formatName) {
                            case "TIFF": {
                                imageParser = new TiffImageParser();
                                params = new TiffImagingParameters();
                                break;
                            }
                            case "PCX": {
                                imageParser = new PcxImageParser();
                                params = new PcxImagingParameters();
                                break;
                            }
                            case "DCX": {
                                imageParser = new DcxImageParser();
                                params = new ImagingParameters<>();
                                break;
                            }
                            case "BMP": {
                                imageParser = new BmpImageParser();
                                params = new BmpImagingParameters();
                                break;
                            }
                            case "GIF": {
                                imageParser = new GifImageParser();
                                params = new GifImagingParameters();
                                break;
                            }
                            case "PNG": {
                                imageParser = new PngImageParser();
                                params = new PngImagingParameters();
                                break;
                            }
                            case "WBMP": {
                                imageParser = new WbmpImageParser();
                                params = new WbmpImagingParameters();
                                break;
                            }
                            case "XBM": {
                                imageParser = new XbmImageParser();
                                params = new WbmpImagingParameters();
                                break;
                            }
                            case "XPM": {
                                imageParser = new XpmImageParser();
                                params = new XbmImagingParameters();
                                break;
                            }
                            default: {
                                throw new IOException(
                                        "Format of the original image is not supported for write operation."
                                );
                            }

                        }

                        writeImage(imageParser, sanitizedImage, fos, params);
                    }

                }

                // Set state flag
                safeState = true;
            }

        } catch (Exception e) {
            throw new FileValidationException("Error during Image file processing: " + e);
        }

        return safeState;
    }

    @SuppressWarnings("unchecked")
    private <T extends ImagingParameters<T>> void writeImage(
            AbstractImageParser<T> parser,
            BufferedImage image,
            OutputStream out,
            ImagingParameters<?> params) throws IOException {
        parser.writeImage(image, out, (T) params);
    }
}
