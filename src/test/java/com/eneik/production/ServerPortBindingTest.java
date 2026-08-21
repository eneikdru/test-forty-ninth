package com.eneik.production;

import com.eneik.production.config.ServerPortCustomizer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(properties = {"SERVER_PORT=18080"})
public class ServerPortBindingTest {

    @Autowired
    private ServerPortCustomizer serverPortCustomizer;

    @Test
    public void testServerPortCustomizerBindsContainerPortTo8080() {
        assertEquals(8080, serverPortCustomizer.getPort());

        TomcatServletWebServerFactory factory = new TomcatServletWebServerFactory();
        serverPortCustomizer.customize(factory);

        assertEquals(8080, factory.getPort());
    }
}
