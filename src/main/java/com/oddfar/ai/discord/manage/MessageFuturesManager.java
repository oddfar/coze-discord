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
            throw new ServiceException("响应超时");
        } catch (InterruptedException | ExecutionException e) {
            throw new ServiceException("处理消息时出错");
        }
    }

    public static ChatResp awaitStream(Long messageId) {
        CompletableFuture<ChatResp> future = messageFutures.get(messageId);
        try {
//            while ((future = messageFutures.get(messageId)) == null) {
//                // 如果 future 为 null，稍等片刻再重试
//                TimeUnit.MILLISECONDS.sleep(200); // 200毫秒
//            }
            ChatResp chatResp = future.get(10, TimeUnit.SECONDS);  // 设置超时时间为10秒
            //消费完了创建一个新的
            messageFutures.remove(messageId);
            //添加
            if (!containsFutures(messageId)) {
                CompletableFuture<ChatResp> newFuture = new CompletableFuture<>();
                MessageFuturesManager.putFutures(messageId, newFuture);
            }
            return chatResp;
        } catch (TimeoutException e) {
            throw new ServiceException("响应超时");
        } catch (InterruptedException | ExecutionException e) {
            throw new ServiceException("处理消息时出错");
        }
    }
}
