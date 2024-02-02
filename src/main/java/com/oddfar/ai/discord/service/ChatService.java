package com.oddfar.ai.discord.service;

import com.oddfar.ai.discord.domain.resp.ChatResp;
import jakarta.validation.constraints.NotBlank;

/**
 * chat接口
 *
 * @author oddfar
 * @since 2024/2/2
 */
public interface ChatService {

    /**
     * 发送一个chat
     *
     * @param channelId 频道
     * @param msg       发送的消息
     * @return discord的coze机器人响应完后返回结果ChatResp
     */
    ChatResp sendChat(@NotBlank String channelId, @NotBlank String msg);
}
