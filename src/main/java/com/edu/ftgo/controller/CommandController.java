package com.edu.ftgo.controller;

import com.edu.ftgo.command.QueryWeatherCommand;
import com.edu.ftgo.domain.QueryWeatherResult;
import io.eventuate.common.json.mapper.JSonMapper;
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
public class CommandController {

    @Autowired
    CommandProducer commandProducer;

    @Autowired
    MessageConsumer messageConsumer;

    @GetMapping("/weather")
    public void weatherCommand() {
        messageConsumer
                .subscribe("weather-subscriber", Collections.singleton("weather-reply"),
                        message -> {
                            QueryWeatherResult result = JSonMapper
                                    .fromJson(message.getPayload(), QueryWeatherResult.class);
                            log.info("consumer result:" + result.getResult() + "!!!!");
                        });

        commandProducer
                .send("weather", new QueryWeatherCommand("Beijing"), "weather-reply",
                        new HashMap<>());
    }

}