package com.br.igorsily.discordbotchatgpt.config;

import com.theokanning.openai.service.OpenAiService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfiguration {

    @Value("${openai.api-key}")
    private String apiKey;

    @Bean
    public OpenAiService openAI() {
        return new OpenAiService(apiKey);
    }
}
