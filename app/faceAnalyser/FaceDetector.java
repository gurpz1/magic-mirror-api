package faceAnalyser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.typesafe.config.ConfigFactory;
import org.bytedeco.javacv.OpenCVFrameConverter;
import play.Logger;

import org.bytedeco.javacpp.BytePointer;

import static org.bytedeco.javacpp.helper.opencv_objdetect.cvHaarDetectObjects;
import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_core.cvCreateImage;
import static org.bytedeco.javacpp.opencv_imgcodecs.*;
import static org.bytedeco.javacpp.opencv_imgproc.*;
import static org.bytedeco.javacpp.opencv_objdetect.*;

/**
 * Created by G on 26/03/2016.
 * Class used to detect faces from an image
 */
public class FaceDetector {

    private File CASCADE_FILE;
    private final int MIN_SIZE = 20;
    private final int GROUP = 0;
    private final double SCALE = 1.1;

    /**
     * Constructor for face detection
     * @param haarCascade XML haar cascade
     */
    public FaceDetector(String haarCascade) {

        CASCADE_FILE = new File(haarCascade);

        if(!CASCADE_FILE.exists()) {
            Logger.error("Unable to find " + CASCADE_FILE.getAbsolutePath());
            defaultInit();
        }
        Logger.debug("Loading filter from " + CASCADE_FILE.getAbsolutePath());
    }

    /**
     * Default constructor
     */
    public FaceDetector() {
        defaultInit();
    }

    private void defaultInit() {
        Logger.debug("Using default filter for face detection");
        CASCADE_FILE = new  File(ConfigFactory.load().getString("cascades.haar.face"));
    }

    /**
     * Extracts faces from an image given a cascade filter
     * @param image Image to analyse
     * @param scale
     * @param group
     * @param minsize
     * @return
     */
    public CvSeq getFaces(IplImage image, double scale, int group, int minsize) {

        // convert to grayscale
        IplImage grayImg = IplImage.create(image.width(),image.height(), IPL_DEPTH_8U, 1);
        cvCvtColor(image, grayImg, CV_BGR2GRAY);
        // storage is needed to store information during detection
        CvMemStorage storage = CvMemStorage.create();

        // Extract resource from jar
        CvHaarClassifierCascade cascade = new CvHaarClassifierCascade(cvLoad(CASCADE_FILE.getAbsolutePath()));
        // We detect the faces.
        CvSeq ret =  cvHaarDetectObjects(image, cascade, storage, scale, group, minsize);

        //Clear once finished
        cvClearMemStorage(storage);
        return ret;
    }

    /**
     * Extracts all the faces from an image
     * @param imageData
     * @return
     */
    public List<byte[]> exportFaces(byte[] imageData) throws IOException {

        List<byte[]> ret = new ArrayList<>();

        IplImage originalImage = cvDecodeImage(cvMat(1, imageData.length, CV_8UC1, new BytePointer(imageData)));

        CvSeq faces = getFaces(originalImage, SCALE, GROUP, MIN_SIZE);

        for(int i=0; i<faces.total(); i++) {
            CvRect r = new CvRect(cvGetSeqElem(faces, i));
            // Set the region of interest in the image
            cvSetImageROI(originalImage, r);

            IplImage cropped = cvCreateImage(cvGetSize(originalImage), originalImage.depth(), originalImage.nChannels());
            // Copy original image (only ROI) to the cropped image
            cvCopy(originalImage, cropped);

            // convert the resulting image back to an array
            // Ensure that the exported faces are all the same size
            IplImage resized = cvCreateImage(cvSize(200,200),originalImage.depth(), originalImage.nChannels());
            cvResize(cropped, resized);
            ret.add(Utils.iplImageToBytes(resized));

            resized.release();
            cropped.release();
        }
        return ret;
    }

    /**
     * Draws a yellow rectangle around the faces
     * @param imageData
     * @return
     * @throws RuntimeException when unable to read cascade file
     * @throws IOException when unable to write to output buffer
     */
    public byte[] highlightFaces(byte[] imageData) throws RuntimeException, IOException {
        IplImage originalImage = cvDecodeImage(cvMat(1, imageData.length, CV_8UC1, new BytePointer(imageData)));
        // We detect the faces.
        CvSeq faces = getFaces(originalImage, SCALE, GROUP, MIN_SIZE);

        // We iterate over the discovered faces and draw yellow rectangles around them.
        for (int i = 0; i < faces.total(); i++) {
            CvRect r = new CvRect(cvGetSeqElem(faces, i));
            cvRectangle(originalImage, cvPoint(r.x(), r.y()),
                    cvPoint(r.x() + r.width(), r.y() + r.height()),
                    CvScalar.YELLOW, 1, CV_AA, 0);
        }

        // convert the resulting image back to an array
        return Utils.iplImageToBytes(originalImage);
    }

    /**
     * Counts the number of faces in the image
     * @param imageData
     * @return
     */
    public int countFaces(byte[] imageData) {
        IplImage originalImage = cvDecodeImage(cvMat(1, imageData.length, CV_8UC1, new BytePointer(imageData)));
        return getFaces(originalImage, SCALE, GROUP, MIN_SIZE).total();
    }

}
