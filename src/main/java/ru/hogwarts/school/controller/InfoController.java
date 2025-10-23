package ru.hogwarts.school.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class InfoController {

    private static final Logger logger = LoggerFactory.getLogger(InfoController.class);

    private int serverPort;
    private final String applicationName;

    public InfoController(@Value("${server.port}") int serverPort,
                          @Value("${spring.application.name}") String applicationName) {
        this.serverPort = serverPort;
        this.applicationName = applicationName;
        logger.info("InfoController initialized for application: {} on port: {}", applicationName, serverPort);
    }

    @GetMapping("/port")
    public String getPort(){
        logger.info("Was invoked method for get server port");
        logger.debug("Application: {}, Port: {}", applicationName, serverPort);

        String response = String.format("Application '%s' is running on port: %d", applicationName, serverPort);
        logger.debug("Response: {}", response);

        return response;
    }
}
