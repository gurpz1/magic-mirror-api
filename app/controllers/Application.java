package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.ning.http.util.Base64;
import com.typesafe.config.ConfigFactory;
import faceAnalyser.FaceDetector;
import faceAnalyser.FaceRecogniser;
import play.Logger;
import play.mvc.*;

import views.html.*;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


public class Application extends Controller {

    private static FaceRecogniser f = new FaceRecogniser();

    public static Result index() {
        return ok(index.render("Your new app is ready."));
    }

    /**
     * Counts the numebr of faces from a dataurl encoded post
     * @return
     */
    public static Result countFaces() {
        response().setHeader("Access-Control-Allow-Origin", "*");

        Logger.debug("Incoming request from " + request().host());

        JsonNode json = request().body().asJson();
        if(json == null) {
            return badRequest("Expecting Json data");
        }

        String stringImage = json.findPath("image").asText();

        if(stringImage == null) {
            // Real bad I know; but oh well
            return badRequest("Missing image_type or image_data from request");
        }


        FaceDetector faceDetector = new FaceDetector();

        return ok(Integer.toString(faceDetector.countFaces(getBytesFromDataUrl(stringImage))));
    }

    /**
     * Saves the posted image to disk
     * @param id
     * @return
     */
    public static Result trainFace(String id) {
        response().setHeader("Access-Control-Allow-Origin", "*");

        Logger.debug("Collecting images for " + id);

        JsonNode json = request().body().asJson();
        if(json == null) {
            return badRequest("Expecting Json data");
        }

        String stringImage = json.findPath("image").asText();

        String path = ConfigFactory.load().getString("faces.repo")  + "/" + id;

        File dir = new File(path);
        if(!dir.exists())
            dir.mkdirs();

        int count = dir.listFiles().length;


        try {
            FaceDetector faceD = new FaceDetector();
            List<byte[]> faces = faceD.exportFaces(getBytesFromDataUrl(stringImage));
            if(faces.size() > 0) {
                for(int i=0; i< faces.size(); i++) {
                    File f = new File(path + "/c" + count + "." + i + ".png");
                    if(!f.exists())
                        f.createNewFile();
                    ByteArrayInputStream bais = new ByteArrayInputStream(faces.get(i));
                    BufferedImage bf = ImageIO.read(bais);
                    ImageIO.write(bf, "png", f);
                    Logger.debug("Written to " + f.getAbsolutePath());
                }
            }

        } catch (IOException e) {
            Logger.error("Unable to write down image for " + id);
            Logger.error(e.getMessage());
        }

        return ok();
    }

    public static Result trainAll() {
        response().setHeader("Access-Control-Allow-Origin", "*");

        Logger.info("Recieved training request " + request().host());

        JsonNode json = request().body().asJson();
        if(json == null) {
            return badRequest("Expecting Json data");
        }

        String keyword = json.findPath("keyword").asText();

        if(Objects.equals(keyword.toLowerCase(), "do it now")) {
            Logger.debug("Password matches");
            f.train(ConfigFactory.load().getString("faces.repo"));
        }

        return ok();
    }

    public static Result whoami() {
        response().setHeader("Access-Control-Allow-Origin", "*");

        Logger.debug("Incoming whoami from " + request().host());

        JsonNode json = request().body().asJson();
        if(json == null) {
            return badRequest("Expecting Json data");
        }

        String stringImage = json.findPath("image").asText();

        if(stringImage == null) {
            // Real bad I know; but oh well
            return badRequest("Missing image_type or image_data from request");
        }

        return ok(f.predict(getBytesFromDataUrl(stringImage)));
    }

    private static byte[] getBytesFromDataUrl(String dataUrl) {
        String encodingPrefix = "base64,";
        int contentStartIndex = dataUrl.indexOf(encodingPrefix) + encodingPrefix.length();
        return Base64.decode(dataUrl.substring(contentStartIndex));
    }

    public static Result preflight(String all) {
        response().setHeader("Access-Control-Allow-Origin", "*");
        response().setHeader("Allow", "*");
        response().setHeader("Access-Control-Allow-Methods", "POST, GET, PUT, DELETE, OPTIONS");
        response().setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept, Referer, User-Agent");
        return ok();
    }
}
