package magicMirrorApi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

/**
 * Created by gsagoo on 31/01/2016.
 */
@Component
public class ApplicationStartup implements ApplicationListener<ContextRefreshedEvent> {

    private static Logger _log = LoggerFactory.getLogger(ApplicationStartup.class);


    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {


    }
}
