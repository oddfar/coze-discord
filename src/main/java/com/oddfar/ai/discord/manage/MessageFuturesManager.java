package com.oddfar.ai.discord.manage;

import com.oddfar.ai.discord.common.exception.ServiceException;
import com.oddfar.ai.discord.domain.resp.ChatResp;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.*;

/**
 * 消息Future管理
 *
 * @author oddfar
 * @since 2024/2/1
 */
@Slf4j
public class MessageFuturesManager {
    public final static Map<Long, CompletableFuture<ChatResp>> messageFutures = new ConcurrentHashMap<>();
    //存需流式返回的messageId
    public final static ConcurrentLinkedDeque<Long> streamMessageList = new ConcurrentLinkedDeque<>();


    public static CompletableFuture<ChatResp> putFutures(Long messageId, CompletableFuture<ChatResp> future) {
        return messageFutures.put(messageId, future);
    }

    public static boolean containsFutures(Long messageId) {
        return messageFutures.containsKey(messageId);
    }

    public static CompletableFuture<ChatResp> getFutures(Long messageId) {
        return messageFutures.get(messageId);
    }

    public static CompletableFuture<ChatResp> removeFutures(Long messageId) {
        return messageFutures.remove(messageId);
    }

    public static ChatResp awaitResponse(Long messageId) {
        try {
            CompletableFuture<ChatResp> future = messageFutures.get(messageId);
            ChatResp chatResp = future.get(60, TimeUnit.SECONDS);  // 设置超时时间为60秒
            return chatResp;
        } catch (TimeoutException e) {
            //移除缓存信息
            removeFutures(messageId);
            throw new ServiceException("响应超时");
        } catch (InterruptedException | ExecutionException e) {
            //移除缓存信息
            removeFutures(messageId);
            throw new ServiceException("处理消息时出错");
        }
    }

    public static ChatResp awaitStream(Long messageId) {
        if (!messageFutures.containsKey(messageId)) {
            return null;
        }
        CompletableFuture<ChatResp> future = messageFutures.get(messageId);
        try {
            ChatResp chatResp = future.get(60, TimeUnit.SECONDS);  // 设置超时时间为60秒
            //消费完了若还没执行完，则创建一个新的
            messageFutures.remove(messageId);
            if (!chatResp.getDone()) {
                //添加
                if (!containsFutures(messageId)) {
                    CompletableFuture<ChatResp> newFuture = new CompletableFuture<>();
                    putFutures(messageId, newFuture);
                }
            }
            return chatResp;
        } catch (TimeoutException e) {
            //移除缓存信息
            streamMessageList.remove(messageId);
            removeFutures(messageId);
            throw new ServiceException("响应超时");
        } catch (InterruptedException | ExecutionException e) {
            //移除缓存信息
            streamMessageList.remove(messageId);
            removeFutures(messageId);
            throw new ServiceException("处理消息时出错");
        }
    }
}
