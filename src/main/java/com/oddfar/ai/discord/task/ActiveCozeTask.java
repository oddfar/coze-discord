package com.oddfar.ai.discord.task;

import com.oddfar.ai.discord.config.properties.DiscordProperties;
import com.oddfar.ai.discord.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * 活跃 coze 机器人的定时任务
 *
 * @author oddfar
 * @since 2024/2/8
 */
@Configuration
@EnableScheduling
public class ActiveCozeTask {

    @Autowired
    private ChatService chatService;

    @Autowired
    private DiscordProperties discordProperties;

    /**
     * 0：10 执行
     */
    @Scheduled(cron = "0 10 0 ? * * ")
    public void ActiveCozeTask() {
        chatService.sendChat(discordProperties.getChannelId(), "你好");
    }
}
