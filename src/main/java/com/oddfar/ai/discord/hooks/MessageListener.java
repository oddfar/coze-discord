package com.oddfar.ai.discord.hooks;

import cn.hutool.extra.spring.SpringUtil;
import com.oddfar.ai.discord.config.properties.DiscordProperties;
import com.oddfar.ai.discord.domain.resp.ChatResp;
import com.oddfar.ai.discord.manage.MessageFuturesManager;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageType;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * 消息监听
 *
 * @author oddfar
 * @since 2024/1/31
 */
@Slf4j
@Service
public class MessageListener extends ListenerAdapter {

    /**
     * 收到消息的回调
     * 一个案例，发送“!ping”，回复“Pong!”
     */
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        // 如果事件为null
        if (event == null) return;
        DiscordProperties discordProperties = SpringUtil.getBean(DiscordProperties.class);
        //如果不是 coze 机器人发的消息
        if (!event.getAuthor().getId().equals(discordProperties.getCozeBotId())) return;
        //本消息
        Message message = event.getMessage();
        //如果不是引用的消息
        if (message.getType() != MessageType.INLINE_REPLY) return;
        //引用的消息
        Message referencedMessage = message.getReferencedMessage();
        if (referencedMessage == null) return;
        String content = message.getContentRaw();
        log.info("收到消息：{}", content);
        log.info("收到消息的引用消息Id：{}", referencedMessage.getId());

        if (content.equals("!ping")) {
            MessageChannel channel = event.getChannel();
            channel.sendMessage("Pong!").queue();
        }
    }

    /**
     * 消息更新时的回调
     *
     * @param event
     */
    @Override
    public void onMessageUpdate(MessageUpdateEvent event) {
        // 如果事件为null
        if (event == null) return;
        DiscordProperties discordProperties = SpringUtil.getBean(DiscordProperties.class);
        //如果不是 coze 机器人发的消息
        if (!event.getAuthor().getId().equals(discordProperties.getCozeBotId())) return;
        //本消息
        Message message = event.getMessage();
        //如果不是引用的消息
        if (message.getType() != MessageType.INLINE_REPLY) return;
        //引用的消息
        Message referencedMessage = message.getReferencedMessage();
        boolean stream = MessageFuturesManager.streamMessageList.contains(referencedMessage.getIdLong());
        if (stream) {
            //如果是流式返回，每次都返回
            log.info("收到update消息,流式返回：{}", message.getContentRaw());
            setFutures(referencedMessage, message, false);
        }
        //是否存在按钮 = 100%响应完毕，只返回一次
        boolean component = message.getComponents().isEmpty();
        //存在按钮 = 100%响应完毕（一般来说是这样的）
        if (!component && referencedMessage != null) {
            log.info("收到update结果消息：{}", message.getContentRaw());
            log.info("收到消息的引用消息Id：{}", referencedMessage.getId());
            setFutures(referencedMessage, message, true);
        }


    }

    private static void setFutures(Message referencedMessage, Message message, boolean done) {
        CompletableFuture<ChatResp> future = MessageFuturesManager.getFutures(referencedMessage.getIdLong());
        if (future != null && !future.isDone()) {
            //构建ChatResp信息
            ChatResp chatResp = new ChatResp(message, referencedMessage, done);
            future.complete(chatResp);
        }
    }


}
