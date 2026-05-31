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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;

/**
 *  sanitize picture by overwriting (OWASP recommandation) using Java built-in API
 *  adapted from the OWASP example, Dominique Righetod's https://github.com/righettod/document-upload-protection
 */
@Service
public class ImageSanitizerService implements DocumentSanitizer{
    @Override
    public MultipartFile sanitize(File f) {
        boolean fallbackOnApacheCommonsImaging;
        try {
            if (f == null || !f.exists() || !f.canRead() || !f.canWrite()) {
                return null;
            }

            //Get the image format
            ImageFormatInformation info = getImageFormat(f);
            String formatName = info.formatName();
            fallbackOnApacheCommonsImaging = info.fallbackOnApacheCommonsImaging();

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
            int resizedWidth = Math.max(1, originalWidth - 1);
            int resizedHeight = Math.max(1, originalHeight - 1);

            // Resize the image by removing 1px on Width and Height
            Image resizedImage = originalImage.getScaledInstance(resizedWidth, resizedHeight, Image.SCALE_SMOOTH);

            // Resize the resized image by adding 1px on Width and Height - In fact set image to is initial size
            Image initialSizedImage = resizedImage.getScaledInstance(originalWidth, originalHeight, Image.SCALE_SMOOTH);

            // Save image by overwriting the provided source file content
            saveByOverWritingFile(initialSizedImage, fallbackOnApacheCommonsImaging, formatName, f);

            return convertFileToMultipartFile(f);

        } catch (Exception e) {
            throw new FileValidationException("Error during Image file processing: " + e);
        }
    }

    private ImageFormatInformation getImageFormat(File f) throws IOException {
        String formatName;
        boolean fallbackOnApacheCommonsImaging;

        try (ImageInputStream imageInputStream = ImageIO.createImageInputStream(f)) {
            Iterator<ImageReader> imageReaderIterator = ImageIO.getImageReaders(imageInputStream);

            //If there not ImageReader instance found so it's means that the current format
            // is not supported by the Java built-in API
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

        return new ImageFormatInformation(formatName, fallbackOnApacheCommonsImaging);
    }

    private void saveByOverWritingFile(
            Image initialSizedImage,
            boolean fallbackOnApacheCommonsImaging,
            String formatName,
            File f
    ) throws IOException {
        BufferedImage sanitizedImage = new BufferedImage(
                initialSizedImage.getWidth(null),
                initialSizedImage.getHeight(null),
                formatName.equalsIgnoreCase("PNG") || formatName.equalsIgnoreCase("GIF")
                        ? BufferedImage.TYPE_INT_ARGB
                        : BufferedImage.TYPE_INT_RGB
        );
        Graphics2D bg = sanitizedImage.createGraphics();
        bg.drawImage(initialSizedImage, 0, 0, null);
        bg.dispose();

        try (OutputStream fos = Files.newOutputStream(
                f.toPath(), StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING
        )) {
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
    }

    @SuppressWarnings("unchecked")
    private <T extends ImagingParameters<T>> void writeImage(
            AbstractImageParser<T> parser,
            BufferedImage image,
            OutputStream out,
            ImagingParameters<?> params) throws IOException {
        parser.writeImage(image, out, (T) params);
    }

    private MultipartFile convertFileToMultipartFile(File f) throws IOException {
        String mimeType = Files.probeContentType(f.toPath());

        if (mimeType == null) {
            mimeType = getMimeTypeFromFormat(f);
        }

        try (FileInputStream input = new FileInputStream(f)) {

            return new MockMultipartFile(
                    f.getName(),
                    f.getName(),
                    mimeType,
                    input
            );
        }
    }

    private String getMimeTypeFromFormat(File f) throws IOException {
        ImageInfo imageInfo = Imaging.getImageInfo(f);

        if (imageInfo == null || imageInfo.getFormat() == null) {
            return "application/octet-stream";
        }

        String format = imageInfo.getFormat().getName();

        return switch (format.toUpperCase()) {
            case "PNG" -> "image/png";
            case "JPEG", "JPG" -> "image/jpeg";
            case "GIF" -> "image/gif";
            case "BMP" -> "image/bmp";
            case "TIFF" -> "image/tiff";
            case "WBMP" -> "image/vnd.wap.wbmp";
            default -> "application/octet-stream";
        };
    }

    private record ImageFormatInformation(
            String formatName,
            boolean fallbackOnApacheCommonsImaging
    ) {/*record only*/}
}
