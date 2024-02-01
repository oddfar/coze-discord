package com.oddfar.ai.discord.controller;

import com.oddfar.ai.discord.common.exception.ServiceException;
import com.oddfar.ai.discord.config.properties.DiscordProperties;
import com.oddfar.ai.discord.domain.R;
import com.oddfar.ai.discord.domain.req.ChatReq;
import com.oddfar.ai.discord.domain.resp.ChatResp;
import com.oddfar.ai.discord.manage.MessageFuturesManager;
import com.oddfar.ai.discord.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.concurrent.CompletableFuture;

/**
 * 发送消息
 *
 * @author oddfar
 * @since 2024/1/31
 */
@Slf4j
@RestController
@RequestMapping("/api/chat")
public class ChatController {
    @Autowired
    private JDA jda;

    @Autowired
    private DiscordProperties discordProperties;

    /**
     * 发送消息 返回结果
     *
     * @param chatReq
     * @return
     */
    @PostMapping
    public R<ChatResp> sendChat(@Validated @RequestBody ChatReq chatReq) {
        // step1：获取一个频道
        if (StringUtils.isBlank(chatReq.getChannelId())) {
            chatReq.setChannelId(discordProperties.getChannelId());
        }
        TextChannel textChannel = jda.getTextChannelById(chatReq.getChannelId());
        // 是否可以在此通道中发送消息
        if (textChannel.canTalk()) {
            // step2：发送消息并获取结果
            //拼接信息
            String msg = String.format("<@%s> %s", discordProperties.getCozeBotId(), chatReq.getContent());
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
            return R.ok(resp);
        }
        return R.fail("处理消息时出错");
    }

    /**
     * 发送消息 流式返回
     *
     * @param chatReq
     * @return
     */
    @PostMapping("/stream")
    public ResponseEntity<StreamingResponseBody> sendChatStream(@Validated @RequestBody ChatReq chatReq) {
        // step1：获取一个频道
        if (StringUtils.isBlank(chatReq.getChannelId())) {
            chatReq.setChannelId(discordProperties.getChannelId());
        }
        TextChannel textChannel = jda.getTextChannelById(chatReq.getChannelId());
        // 是否可以在此通道中发送消息
        if (textChannel.canTalk()) {
            // step2：发送消息并获取结果
            //拼接信息
            String msg = String.format("<@%s> %s", discordProperties.getCozeBotId(), chatReq.getContent());
            Message sendMessage = textChannel.sendMessage(msg).complete();
            long messageId = sendMessage.getIdLong();
            log.info("发送成功，消息Id：{}", messageId);
            // step3：等待响应的信息
            CompletableFuture<ChatResp> future = new CompletableFuture<>();
            //3.1：添加流式的信息
            MessageFuturesManager.putFutures(messageId, future);
            MessageFuturesManager.streamMessageList.add(messageId);
            //流式返回
            StreamingResponseBody stream = out -> {
                while (true) {
                    //3.2：等待响应结果
                    ChatResp resp = MessageFuturesManager.awaitStream(messageId);
                    R<ChatResp> ok = R.ok(resp);
                    out.write(JsonUtils.toJsonString(ok).getBytes());
                    out.flush();
                    //3.3 最终结果出来
                    if (resp.getDone()) {
                        // step4：移除缓存信息
                        MessageFuturesManager.streamMessageList.remove(messageId);
                        MessageFuturesManager.removeFutures(messageId);
                        break;
                    }
                }
            };

            return ResponseEntity.ok().contentType(MediaType.TEXT_PLAIN).body(stream);
        }

        throw new ServiceException("处理消息时出错");
    }

}
