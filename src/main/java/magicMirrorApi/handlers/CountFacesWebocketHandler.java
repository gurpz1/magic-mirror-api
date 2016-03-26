package magicMirrorApi.handlers;

import magicMirrorApi.face.FaceDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

/**
 * Created by gsagoo on 09/01/2016.
 */
@Component
public class CountFacesWebocketHandler extends BinaryWebSocketHandler {
    private static Logger _log = LoggerFactory.getLogger(CountFacesWebocketHandler.class);
    private FaceDetector f;

    @Autowired
    public CountFacesWebocketHandler(FaceDetector f) {
        this.f = f;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception{
        _log.info("Connection established with " + session.getRemoteAddress());
    }

    @Override
    public void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception{
        _log.debug("Message Received");

        TextMessage m = new TextMessage(Integer.toString(f.countFaces(message.getPayload().array())));
        f.close();
        session.sendMessage(m);
    }
}
