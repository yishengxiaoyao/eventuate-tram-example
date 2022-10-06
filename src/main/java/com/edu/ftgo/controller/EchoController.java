package com.edu.ftgo.controller;

import com.edu.ftgo.command.EchoCommand;
import io.eventuate.tram.commands.producer.CommandProducer;
import io.eventuate.tram.messaging.consumer.MessageConsumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.HashMap;

@RestController
@Slf4j
public class EchoController {
    @Autowired
    CommandProducer commandProducer;

    @Autowired
    MessageConsumer messageConsumer;

    @GetMapping("/echo")
    public void echoCommand() {
        messageConsumer
                .subscribe("echo-subscriber", Collections.singleton("echo-reply"),
                        message -> {
                            log.info(message.getPayload() + "!!!!!!!!");
                        });

        commandProducer
                .send("echo", "/user/alex", new EchoCommand(), "echo-reply",
                        new HashMap<>());
    }
}
