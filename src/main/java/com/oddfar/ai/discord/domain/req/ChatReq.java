package com.oddfar.ai.discord.domain.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * Chat请求参数
 *
 * @author oddfar
 * @since 2024/1/31
 */
@Data
public class ChatReq implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 频道ID
     */
//    @NotBlank(message = "频道ID不能为空")
    private String channelId;

    /**
     * 消息内容
     */
    @NotBlank(message = "消息内容不能为空")
    private String content;

//    /**
//     * 是否流式返回
//     */
//    private Boolean stream;
}
