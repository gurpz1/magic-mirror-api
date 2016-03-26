package magicMirrorApi.face;

import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_face;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.nio.IntBuffer;
import java.util.*;

import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_face.createFisherFaceRecognizer;
import static org.bytedeco.javacpp.opencv_imgcodecs.CV_LOAD_IMAGE_GRAYSCALE;
import static org.bytedeco.javacpp.opencv_imgcodecs.imread;
import static org.bytedeco.javacpp.opencv_imgproc.CV_BGR2GRAY;
import static org.bytedeco.javacpp.opencv_imgproc.cvCvtColor;

/**
 * Created by gsagoo on 31/01/2016.
 */
@Component
public class FaceRecogniser {

    private static Logger _log = LoggerFactory.getLogger(FaceRecogniser.class);
    private final String REPO_DIR = "/Users/gsagoo/Documents/workspace/repo";

    private opencv_face.FaceRecognizer faceRecognizer;
    private Map<Integer,String> labelToNameMap = new HashMap<Integer, String>();

    public FaceRecogniser() {
        faceRecognizer = createFisherFaceRecognizer();
        initRecogniser();
    }

    private void initRecogniser() {
        // DIR name in the repo is a face label
        File rootDir = new File(REPO_DIR);

        // Filter to list directories
        FileFilter isDir = new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isDirectory();
            }
        };

        // Filter to list images
        FilenameFilter isImg = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                name = name.toLowerCase();
                return name.endsWith(".jpg") || name.endsWith(".png");
            }
        };

        // Now let's do the training
        File[] dirs = rootDir.listFiles(isDir);

        // Count how many images we have
        int totalFiles = 0;
        for(File dir: dirs) {
            totalFiles += dir.listFiles(isImg).length;
        }

        // Label for each dir in the repo
        int label=0;
        // ImgData
        opencv_core.MatVector images = new opencv_core.MatVector(totalFiles);
        // LabelData
        opencv_core.Mat labels = new opencv_core.Mat(totalFiles,1, CV_32SC1);
        IntBuffer labelsBuf = labels.getIntBuffer();

        // Iterate the repo
        int imgCounter = 0;
        for(File dir: dirs) {
            // Get all the images in the dir
            File[] imgs = dir.listFiles(isImg);
            for(File img:imgs) {
                opencv_core.Mat image = imread(img.getAbsolutePath(), CV_LOAD_IMAGE_GRAYSCALE);
                images.put(imgCounter, image);
                labelsBuf.put(imgCounter, label);
                imgCounter ++;
            }

            // Map the label to a name
            labelToNameMap.put(label, dir.getName());
            label ++;
        }

        // Now we have scanned it all, let's setup the recogniser
        faceRecognizer.train(images, labels);
    }

    public int predict(IplImage img) {
        OpenCVFrameConverter.ToMat conv = new OpenCVFrameConverter.ToMat();
        IplImage gray = IplImage.create(img.width(),img.height(), IPL_DEPTH_8U, 1);
        cvCvtColor(img,gray, CV_BGR2GRAY);
        OpenCVFrameConverter.ToIplImage ci = new OpenCVFrameConverter.ToIplImage();
        Mat image = conv.convert(ci.convert(gray));
        return faceRecognizer.predict(image);
    }
}
