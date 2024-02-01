package com.oddfar.ai.discord.config;

import com.oddfar.ai.discord.config.properties.DiscordProperties;
import com.oddfar.ai.discord.hooks.MessageListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * discord 配置
 *
 * @author oddfar
 * @since 2024/1/31
 */
@AutoConfiguration
@EnableConfigurationProperties(DiscordProperties.class)
public class DiscordConfiguration {
    @Autowired
    private DiscordProperties discordProperties;

    public DiscordConfiguration(DiscordProperties discordProperties) {
        this.discordProperties = discordProperties;
    }

    /**
     * 配置 JDA Bean
     */
    @Bean
    @ConditionalOnMissingBean
    public JDA jda() {

        JDABuilder builder = JDABuilder.createDefault(discordProperties.getBotToken());

        builder.addEventListeners(new MessageListener());
        builder.enableIntents(GatewayIntent.MESSAGE_CONTENT);

        return builder.build();

    }
}
