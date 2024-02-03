package com.oddfar.ai.discord.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * discord 配置属性
 *
 * @author oddfar
 * @since 2024/1/31
 */
@Data
@ConfigurationProperties(prefix = "discord")
public class DiscordProperties {
    // 发送消息的Bot-Token
    private String botToken;
    // 两个机器人所在的服务器ID
    private String guildId;
    // 由coze托管的机器人ID
    private String cozeBotId;
    // 默认频道
    private String channelId;

    private String proxyHostPort;

}
