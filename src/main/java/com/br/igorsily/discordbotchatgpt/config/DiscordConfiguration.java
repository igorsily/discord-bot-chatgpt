package com.br.igorsily.discordbotchatgpt.config;

import com.br.igorsily.discordbotchatgpt.Listener.EventListener;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.Event;
import discord4j.gateway.intent.Intent;
import discord4j.gateway.intent.IntentSet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Objects;

@Configuration
public class DiscordConfiguration {

    @Value("${discord.token}")
    private String token;

    @Bean
    public <T extends Event> GatewayDiscordClient gatewayDiscordClient(List<EventListener<T>> eventListeners) {
        GatewayDiscordClient client = DiscordClient.create(token)
                .gateway()
                .setEnabledIntents(IntentSet.of(Intent.GUILD_MESSAGES, Intent.GUILDS, Intent.MESSAGE_CONTENT))
                .login()
                .block();

        for (EventListener<T> listener : eventListeners) {
            Objects.requireNonNull(client).on(listener.getEventType())
                    .flatMap(listener::execute)
                    .onErrorResume(listener::handleError)
                    .subscribe();
        }
        return client;

    }
}
