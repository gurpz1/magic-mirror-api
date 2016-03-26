package magicMirrorApi.face;

import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.opencv_core.CvMemStorage;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import static org.bytedeco.javacpp.helper.opencv_objdetect.cvHaarDetectObjects;
import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgcodecs.cvDecodeImage;
import static org.bytedeco.javacpp.opencv_imgcodecs.imread;
import static org.bytedeco.javacpp.opencv_imgproc.*;
import static org.bytedeco.javacpp.opencv_objdetect.*;

/**
 * Created by gsagoo on 30/12/2015.
 */
@Service
public class FaceDetector implements AutoCloseable{
    private final File CASCADE_FILE;
    private final int MIN_SIZE = CV_HAAR_DO_CANNY_PRUNING;
    private final int GROUP = 3;
    private final double SCALE = 1.1;

    private FaceRecogniser faceRecogniser;

    private static Logger _log = LoggerFactory.getLogger(FaceDetector.class);

    @Autowired
    public FaceDetector(FaceRecogniser faceRecogniser) throws IOException{
        CASCADE_FILE = Loader.extractResource(getClass(), "/opencv-cascades/haarcascade_frontalface_alt.xml", new File(System.getProperty("java.io.tmpdir")), "classifier", ".xml");
        _log.debug("Loading cascade file from " + CASCADE_FILE.getAbsolutePath());
        this.faceRecogniser = faceRecogniser;
    }

    public byte[] findFaces(byte[] imageData) throws IOException, RuntimeException {
        CvSeq faces = null;

        // create image from supplied bytearray
        IplImage originalImage = cvDecodeImage(cvMat(1, imageData.length, CV_8UC1, new BytePointer(imageData)));

        _log.info("Attempting recognition");
        _log.info(Integer.toString(faceRecogniser.predict(originalImage)));

        try {
            faces = getFaces(originalImage, CASCADE_FILE.getAbsolutePath(), SCALE, GROUP, MIN_SIZE);
        } catch (Exception e) {
            _log.error("Unable to get faces");
            _log.error(e.getMessage());
        }

        // We iterate over the discovered faces and draw yellow rectangles around them.
        for (int i = 0; i < faces.total(); i++) {
            CvRect r = new CvRect(cvGetSeqElem(faces, i));
            cvRectangle(originalImage, cvPoint(r.x(), r.y()),
                    cvPoint(r.x() + r.width(), r.y() + r.height()),
                    CvScalar.YELLOW, 1, CV_AA, 0);
        }

        // convert the resulting image back to an array
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        Java2DFrameConverter javaConverter = new Java2DFrameConverter();
        OpenCVFrameConverter.ToIplImage iplImageConverter = new OpenCVFrameConverter.ToIplImage();

        BufferedImage imgb = javaConverter.convert(iplImageConverter.convert(originalImage));
        ImageIO.write(imgb, "png", bout);
        return bout.toByteArray();
    }

    public int countFaces(byte[] imageData) throws RuntimeException {
        CvSeq faces = null;

        // create image from supplied bytearray
        IplImage originalImage = cvDecodeImage(cvMat(1, imageData.length, CV_8UC1, new BytePointer(imageData)));

        try {
            faces = getFaces(originalImage, CASCADE_FILE.getAbsolutePath(), SCALE, GROUP, MIN_SIZE);
        } catch (Exception e) {
            _log.error("Unable to get faces");
            _log.error(e.getMessage());
        }

        return faces.total();
    }


    /**
     * Extracts faces from an image given a cascade filter
     * @param image Image to analyse
     * @param cascadeFile Cascade file to use
     * @param scale
     * @param group
     * @param minsize
     * @return
     */
    private CvSeq getFaces(IplImage image, String cascadeFile, double scale, int group, int minsize) {

        // convert to grayscale
        IplImage grayImg = IplImage.create(image.width(),image.height(), IPL_DEPTH_8U, 1);
        cvCvtColor(image, grayImg, CV_BGR2GRAY);
        // storage is needed to store information during detection
        CvMemStorage storage = CvMemStorage.create();

        // Extract resource from jar
        CvHaarClassifierCascade cascade = new CvHaarClassifierCascade(cvLoad(cascadeFile));
        // We detect the faces.
        CvSeq ret =  cvHaarDetectObjects(image, cascade, storage, scale, group, minsize);

        //Clear once finished
        cvClearMemStorage(storage);
        return ret;
    }

    public void close() {
        if(CASCADE_FILE.exists()) {
            _log.debug("Cleaining up " + CASCADE_FILE.getAbsolutePath());
            CASCADE_FILE.delete();
        }
    }
}
