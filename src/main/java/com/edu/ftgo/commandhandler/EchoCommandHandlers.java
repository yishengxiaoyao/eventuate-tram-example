package com.edu.ftgo.commandhandler;

import com.edu.ftgo.command.EchoCommand;
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
public class EchoCommandHandlers {

    public CommandHandlers commandHandlers() {
        return CommandHandlersBuilder.fromChannel("echo")
                .resource("/user/{username}")
                .onMessage(EchoCommand.class, this::echo)
                .build();
    }

    private Message echo(CommandMessage<EchoCommand> cm,
                         PathVariables pathVariables) {
        log.info("cm:" + cm + "!!!!");
        log.info("path:" + pathVariables.getString("username") + "!!!!");
        return withSuccess("echo -> " + pathVariables.getString("username"));
    }
}
