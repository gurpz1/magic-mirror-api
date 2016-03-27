package faceAnalyser;

import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class Utils {

    /**
     * Convert IplImage to byte[]
     * @param image
     * @return
     * @throws IOException
     */
    public static byte[] iplImageToBytes(opencv_core.IplImage image) throws IOException{
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        Java2DFrameConverter javaConverter = new Java2DFrameConverter();
        OpenCVFrameConverter.ToIplImage iplImageConverter = new OpenCVFrameConverter.ToIplImage();

        BufferedImage imgb = javaConverter.convert(iplImageConverter.convert(image));
        ImageIO.write(imgb, "png", bout);

        return bout.toByteArray();
    }
}
