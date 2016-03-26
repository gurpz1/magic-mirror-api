package magicMirrorApi.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

/**
 * Created by gsagoo on 09/01/2016.
 */
@RestController
public class FaceTrainingController {

    private int count = 0;
    private static Logger _log = LoggerFactory.getLogger(FaceTrainingController.class);

    @CrossOrigin(origins = "*")
    @RequestMapping(value = "/trainface/{faceId}", method = RequestMethod.PUT)
    public ResponseEntity<?> trainFace(@PathVariable String faceId, @RequestBody byte[] image) {
        try {
            File f = new File("/var/tmp/" + faceId + "/c" + count + ".png");
            if(!f.exists()) {
                // Make parent directories
                f.getParentFile().mkdirs();
                f.createNewFile();
            }
            ByteArrayInputStream bais = new ByteArrayInputStream(image);
            BufferedImage bf = ImageIO.read(bais);
            ImageIO.write(bf, "png", f);
            count++;
        } catch (IOException e) {
            _log.error("Error wring down image");
            _log.error(e.getMessage());
        }
        return new ResponseEntity<Object>(null, HttpStatus.ACCEPTED);
    }

}
