package faceAnalyser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.ConfigFactory;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.indexer.Indexer;
import org.bytedeco.javacpp.indexer.IntBufferIndexer;
import org.bytedeco.javacpp.indexer.IntIndexer;
import org.bytedeco.javacpp.indexer.UByteBufferIndexer;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_face;
import org.bytedeco.javacv.OpenCVFrameConverter;
import play.Logger;
import play.api.libs.json.Json;

import javax.inject.Singleton;
import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_face.createFisherFaceRecognizer;
import static org.bytedeco.javacpp.opencv_imgcodecs.cvDecodeImage;
import static org.bytedeco.javacpp.opencv_imgcodecs.imread;
import static org.bytedeco.javacpp.opencv_imgcodecs.CV_LOAD_IMAGE_GRAYSCALE;
import static org.bytedeco.javacpp.opencv_imgproc.CV_BGR2GRAY;
import static org.bytedeco.javacpp.opencv_imgproc.cvCvtColor;
import static org.bytedeco.javacpp.opencv_imgproc.cvResize;


/**
 * Created by G on 26/03/2016.
 * Class to carry out facial recognition
 */
@Singleton
public class FaceRecogniser {

    private opencv_face.FaceRecognizer faceRecognizer = createFisherFaceRecognizer();
    private Map<Integer,String> faceLabelToNameMap = new HashMap<>();

    /**
     * Trains the face recogniser given a repository of images
     * @param rootImageRepository PATH to images with the format ../../<face name>/images
     */
    public void train(String rootImageRepository) {
        File rootDir = new File(rootImageRepository);

        // Filter to list directories
        FileFilter isDir = File::isDirectory;

        // Filter to list images
        FilenameFilter isImg = (dir, name) -> {
            name = name.toLowerCase();
            return name.endsWith(".jpg") || name.endsWith(".png");
        };

        // Now let's do the training
        File[] faceDirs = rootDir.listFiles(isDir);

        // Count the total number of faces we have on disk
        int totalImagesOnDisk = 0;
        for(File dir: faceDirs) {
            totalImagesOnDisk += dir.listFiles(isImg).length;
        }

        // Need an integer label for each face

        // initialise buffers
        opencv_core.MatVector imagesToAnalyse = new opencv_core.MatVector(totalImagesOnDisk);
        opencv_core.Mat faceLabels = new opencv_core.Mat(totalImagesOnDisk,1, CV_32SC1);
        IntBufferIndexer faceLabelIndex = faceLabels.createIndexer();


        int faceCounter = 0;
        int faceLabel = 0;
        for(File dir:faceDirs) {
            Logger.info("Loading " + dir.getName() + ", label = " + faceLabel);
            // get all the images in that directory and create a recogniser for them
            File[] faceImages = dir.listFiles(isImg);

//            if(faceImages.length < 250) {
//                throw new IllegalStateException("Not enough images for " + dir.getName());
//            }

            for(File face : faceImages) {
                opencv_core.Mat image = imread(face.getAbsolutePath(), CV_LOAD_IMAGE_GRAYSCALE);
                // Add them to the images to analyse
                imagesToAnalyse.put(faceCounter, image);
                image.release();
                // Add the associated label
                faceLabelIndex.put(faceCounter,0,faceLabel);
                faceCounter++;
            }
            // Need to associate the label to something humanly readable
            faceLabelToNameMap.put(faceLabel, dir.getName());
            faceLabel ++;
        }

        // Finally carry out the analysis
        Logger.debug("Started training");
        faceRecognizer.train(imagesToAnalyse,faceLabels);
        Logger.debug("Successfully trained");

        // Save the data for later use
//        ObjectMapper mapper = new ObjectMapper();
//        try {
//            mapper.writerWithDefaultPrettyPrinter().writeValue(new File(ConfigFactory.load().getString("faces.recogniser.labels")), faceLabelToNameMap);
//            faceRecognizer.save(ConfigFactory.load().getString("faces.recogniser"));
//        } catch (IOException e) {
//            Logger.error("Unable to save down trained data");
//        }

    }

    /**
     * From the available image repo, see if we can identify a person
     * @param imageData The image to check agains
     * @return Name if we find it
     */
    public String predict(byte[] imageData) {
        FaceDetector f = new FaceDetector();
        try {
            List<byte[]> faces = f.exportFaces(imageData);
            if(faces.size() > 0) {

                IplImage originalImage = cvDecodeImage(cvMat(1, faces.get(0).length, CV_8UC1, new BytePointer(faces.get(0))));
                IplImage resized = cvCreateImage(cvSize(200,200),originalImage.depth(), originalImage.nChannels());
                cvResize(originalImage, resized);
                OpenCVFrameConverter.ToMat conv = new OpenCVFrameConverter.ToMat();
                IplImage gray = IplImage.create(resized.width(),resized.height(), IPL_DEPTH_8U, 1);
                cvCvtColor(resized,gray, CV_BGR2GRAY);
                OpenCVFrameConverter.ToIplImage ci = new OpenCVFrameConverter.ToIplImage();
                Mat image = conv.convert(ci.convert(gray));

                return faceLabelToNameMap.get(faceRecognizer.predict(image));
            }


        } catch (IOException e) {

        }
        return "Not Found";
    }
}
