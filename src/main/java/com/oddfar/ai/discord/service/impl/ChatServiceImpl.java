package com.oddfar.ai.discord.service.impl;

import com.oddfar.ai.discord.common.exception.ServiceException;
import com.oddfar.ai.discord.config.properties.DiscordProperties;
import com.oddfar.ai.discord.domain.resp.ChatResp;
import com.oddfar.ai.discord.manage.MessageFuturesManager;
import com.oddfar.ai.discord.service.ChatService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * chat 实现类
 *
 * @author oddfar
 * @since 2024/2/2
 */
@Service
@Slf4j
public class ChatServiceImpl implements ChatService {
    @Autowired
    private JDA jda;

    @Autowired
    private DiscordProperties discordProperties;

    @Override
    public ChatResp sendChat(String channelId, String msg) {
        // step1：获取一个频道
        TextChannel textChannel = jda.getTextChannelById(channelId);

        // 是否可以在此通道中发送消息
        if (textChannel.canTalk()) {
            // step2：发送消息并获取结果
            //拼接信息
            msg = String.format("<@%s> %s", discordProperties.getCozeBotId(), msg);
            Message sendMessage = textChannel.sendMessage(msg).complete();
            long messageId = sendMessage.getIdLong();
            log.info("发送成功，消息Id：{}", messageId);
            // step3：等待响应的信息
            CompletableFuture<ChatResp> future = new CompletableFuture<>();
            //添加
            MessageFuturesManager.putFutures(messageId, future);
            //等待响应并获取结果
            ChatResp resp = MessageFuturesManager.awaitResponse(messageId);
            // step4：移除缓存信息
            MessageFuturesManager.messageFutures.remove(messageId);
            return resp;
        } else {
            throw new ServiceException(String.format("bot无权在%s通道中发消息", textChannel.getId()));
        }
    }

}