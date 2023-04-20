package com.br.igorsily.discordbotchatgpt.Listener;

import com.theokanning.openai.completion.CompletionChoice;
import com.theokanning.openai.completion.CompletionRequest;
import com.theokanning.openai.image.CreateImageRequest;
import com.theokanning.openai.image.Image;
import com.theokanning.openai.service.OpenAiService;
import discord4j.core.object.entity.Message;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

public abstract class MessageListener {

    @Value("${openai.model}")
    private String model;

    private final OpenAiService openAiService;

    @Autowired

    public MessageListener(OpenAiService openAiService) {
        this.openAiService = openAiService;
    }

    public Mono<Void> processCommand(Message eventMessage) {
        return Mono.just(eventMessage)
                .filter(message -> message.getAuthor().map(user -> !user.isBot()).orElse(false))
                .filter(message -> !message.getContent().isEmpty())
                .flatMap(Message::getChannel)
                .flatMap(channel -> {
                    String content = eventMessage.getContent().trim();

                    if (content.toLowerCase().startsWith("gpt:")) {
                        content = content.replace("gpt:", "");
                    } else {
                        return Mono.empty();
                    }
                    Object response = processMessage(content.trim());
                    if (response instanceof String) {
                        return channel.createMessage((String) response);
                    } else if (response instanceof EmbedCreateSpec) {
                        return channel.createMessage((EmbedCreateSpec) response);
                    }
                    return channel.createMessage("Não entendi o que você quis dizer.");
                })
                .then();
    }

    private Object processMessage(String content) {

        if (content.startsWith("Image:")) {
            return processImage(content.replace("Image:", ""));
        }

        return processText(content);

    }


    private String processText(String content) {
        CompletionRequest request = this.createCompletionRequest(content);

        try {
            return openAiService.createCompletion(request)
                    .getChoices()
                    .stream()
                    .map(CompletionChoice::getText)
                    .collect(Collectors.joining("\n"));
        } catch (Exception e) {
            return openAiService.createCompletion(createCompletionRequest("Resuma o texto e traduza de Inglês para Português: " + content))
                    .getChoices()
                    .stream()
                    .map(CompletionChoice::getText)
                    .collect(Collectors.joining("\n"));
        }
    }

    private EmbedCreateSpec processImage(String content) {
        CreateImageRequest request = this.createImageRequest(content);
        List<Image> imageList = openAiService.createImage(request).getData();

        return EmbedCreateSpec.builder()
                .color(Color.BLUE)
                .title(content)
                .image(imageList.get(0).getUrl())
                .timestamp(Instant.now())
                .build();

    }

    private CompletionRequest createCompletionRequest(String content) {
        return CompletionRequest
                .builder()
                .prompt(content)
                .model(model)
                .maxTokens(2048)
                .temperature(0.7)
                .n(1)
                .echo(false)
                .build();
    }

    private CreateImageRequest createImageRequest(String content) {
        return CreateImageRequest
                .builder()
                .prompt(content)
                .size("1024x1024")
                .n(1)
                .build();
    }
}