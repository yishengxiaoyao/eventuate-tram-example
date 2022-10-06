package com.edu.ftgo.commandhandler;

import com.edu.ftgo.command.QueryWeatherCommand;
import com.edu.ftgo.domain.QueryWeatherResult;
import io.eventuate.tram.commands.consumer.CommandHandlers;
import io.eventuate.tram.commands.consumer.CommandHandlersBuilder;
import io.eventuate.tram.commands.consumer.CommandMessage;
import io.eventuate.tram.commands.consumer.PathVariables;
import io.eventuate.tram.messaging.common.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static io.eventuate.tram.commands.consumer.CommandHandlerReplyBuilder.withSuccess;

@Component
@Slf4j
public class WeatherCommandHandlers {

    public CommandHandlers commandHandlers() {
        return CommandHandlersBuilder.fromChannel("weather")
                .onMessage(QueryWeatherCommand.class, this::queryWeather)
                .build();
    }

    private Message queryWeather(CommandMessage<QueryWeatherCommand> cm,
                                 PathVariables pathVariables) {
        log.info("cm result:" + cm.getCommand() + "!!!!!!!!!");
        return withSuccess(
                new QueryWeatherResult(cm.getCommand().getCity(), "Rain"));
    }
}
