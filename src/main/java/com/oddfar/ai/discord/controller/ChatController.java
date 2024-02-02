package com.oddfar.ai.discord.controller;

import com.oddfar.ai.discord.common.exception.ServiceException;
import com.oddfar.ai.discord.config.properties.DiscordProperties;
import com.oddfar.ai.discord.domain.R;
import com.oddfar.ai.discord.domain.req.ChatReq;
import com.oddfar.ai.discord.domain.resp.ChatResp;
import com.oddfar.ai.discord.manage.MessageFuturesManager;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

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
    @CrossOrigin
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
    @CrossOrigin
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<R<ChatResp>> sendChatStream(@Validated @RequestBody ChatReq chatReq) {
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

            // 使用AtomicBoolean来跟踪是否应当停止发射更多的元素
            AtomicBoolean shouldEmit = new AtomicBoolean(true);

            //3.2 使用Flux生成反应式流
            return Flux.interval(Duration.ofMillis(300)) //间隔的持续时间
                    .flatMap(tick -> {
                        // 只有当shouldEmit为true时才继续发射元素
                        if (shouldEmit.get()) {
                            ChatResp chatResp = MessageFuturesManager.awaitStream(messageId);
                            return Mono.justOrEmpty(chatResp);
                        } else {
                            return Mono.empty(); // 停止发射元素
                        }
                    })
                    .doOnNext(resp -> {
                        // 如果响应已经完成，则设置shouldEmit为false，这样不会有更多的元素被发射
                        if (resp.getDone()) {
                            shouldEmit.set(false);
                        }
                    })
                    .takeUntil(resp -> !shouldEmit.get()) // 当shouldEmit为false时，将包含当前这个元素并停止
                    .map(R::ok)
                    .doFinally(signalType -> {
                        // step4：无论流是完成还是取消，都移除缓存信息
                        MessageFuturesManager.streamMessageList.remove(messageId);
                        MessageFuturesManager.removeFutures(messageId);
                    })
                    .onBackpressureBuffer(); // 用以处理背压问题
        }

        throw new ServiceException("处理消息时出错");
    }

}
