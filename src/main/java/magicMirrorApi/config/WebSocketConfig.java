package magicMirrorApi.config;

import magicMirrorApi.handlers.CountFacesWebocketHandler;
import magicMirrorApi.handlers.FindFacesWebSocketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

/**
 * Created by gsagoo on 30/12/2015.
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private static Logger _log = LoggerFactory.getLogger(WebSocketConfig.class);

    @Autowired
    private FindFacesWebSocketHandler findFacesWebSocketHandler;

    @Autowired
    private CountFacesWebocketHandler countFacesWebocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(findFacesWebSocketHandler, "/findFaces").setAllowedOrigins("*");
        registry.addHandler(countFacesWebocketHandler, "/countFaces").setAllowedOrigins("*");
    }

    @Bean
    public ServletServerContainerFactoryBean createWebSocketContainer() {
        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
        _log.debug("Setting websocket message buffer size");
        container.setMaxBinaryMessageBufferSize(625000);
        container.setMaxTextMessageBufferSize(625000);
        return container;
    }

}
