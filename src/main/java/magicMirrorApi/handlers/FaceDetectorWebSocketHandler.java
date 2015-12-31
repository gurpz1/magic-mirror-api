package magicMirrorApi.handlers;

import magicMirrorApi.face.FaceDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

/**
 * Created by gsagoo on 30/12/2015.
 */
public class FaceDetectorWebSocketHandler extends BinaryWebSocketHandler {

    private static Logger _log = LoggerFactory.getLogger(FaceDetectorWebSocketHandler.class);

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception{
        _log.info("Connection established with " + session.getRemoteAddress());
    }

    @Override
    public void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception{
        _log.debug("Message Received");

       try {
           FaceDetector f = new FaceDetector();
           BinaryMessage m = new BinaryMessage(f.convert(message.getPayload().array()));
           session.sendMessage(m);

       } catch (Exception e) {
           _log.error("Unable to convert face: " + e.getMessage());
       }
    }
}
