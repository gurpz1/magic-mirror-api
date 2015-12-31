package magicMirrorApi.face;

import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_core.CvMemStorage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.bytedeco.javacpp.helper.opencv_objdetect.cvHaarDetectObjects;
import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgcodecs.cvDecodeImage;
import static org.bytedeco.javacpp.opencv_imgproc.*;
import static org.bytedeco.javacpp.opencv_objdetect.*;

/**
 * Created by gsagoo on 30/12/2015.
 */
public class FaceDetector{
    private static final String CASCADE_FILE = "opencv-cascades/haarcascade_frontalface_alt2.xml";

    private int minsize = 20;
    private int group = 0;
    private double scale = 1.1;


    public byte[] convert(byte[] imageData) throws IOException {

        // create image from supplied bytearray
        IplImage originalImage = cvDecodeImage(cvMat(1, imageData.length, CV_8UC1, new BytePointer(imageData)));
//        IplImage grayImage = IplImage.create(originalImage.width(), originalImage.height(), IPL_DEPTH_8U, 1);
//        cvCvtColor(originalImage, grayImage, CV_BGR2GRAY);
        // storage is needed to store information during detection
        CvMemStorage storage = CvMemStorage.create();

        // Configuration to use in analysis
        String cascadeFile = this.getClass().getClassLoader().getResource(CASCADE_FILE).getPath();
        CvHaarClassifierCascade cascade = new CvHaarClassifierCascade(cvLoad(cascadeFile));


        // We detect the faces.
        CvSeq faces = cvHaarDetectObjects(originalImage, cascade, storage, scale, group, minsize);

        // We iterate over the discovered faces and draw yellow rectangles around them.
        for (int i = 0; i < faces.total(); i++) {
            CvRect r = new CvRect(cvGetSeqElem(faces, i));
            cvRectangle(originalImage, cvPoint(r.x(), r.y()),
                    cvPoint(r.x() + r.width(), r.y() + r.height()),
                    CvScalar.YELLOW, 1, CV_AA, 0);
        }

        // convert the resulting image back to an array
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
//        BufferedImage imgb = originalImage.getBufferedImage();
//        ImageIO.write(imgb, "png", bout);
        bout.write(originalImage.asByteBuffer().array());
        return bout.toByteArray();
    }

}
