package com.oddfar.ai.discord.config;

import com.oddfar.ai.discord.config.properties.DiscordProperties;
import com.oddfar.ai.discord.hooks.MessageListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import okhttp3.OkHttpClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.net.InetSocketAddress;
import java.net.Proxy;

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
        //设置代理
        if (StringUtils.isNotBlank(discordProperties.getProxyHostPort())) {
            String[] hostPort = discordProperties.getProxyHostPort().split(":");
            if (hostPort == null || hostPort.length != 2) {
                throw new IllegalArgumentException("Please check if the 'discord.proxyHostPort' configuration is correct");
            }
            Proxy proxy = new Proxy(Proxy.Type.SOCKS,
                    InetSocketAddress.createUnresolved(hostPort[0], Integer.parseInt(hostPort[1])));
            builder.setHttpClientBuilder(new OkHttpClient.Builder().proxy(proxy));
        }

        return builder.build();

    }
}
