package com.oddfar.ai.discord.domain.resp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.dv8tion.jda.api.entities.Message;

import java.io.Serial;
import java.io.Serializable;

/**
 * Chat请求响应
 *
 * @author oddfar
 * @since 2024/2/1
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatResp implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    // 本消息的id
    private String id;
    // 引用消息id
    private String referencedMessageId;
    //返回内容
    private String content;
    //响应完成
    private Boolean done;

    private User author;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class User implements Serializable {
        private String id;
        private String name;
    }

    public ChatResp(Message message, Message referencedMessage, boolean done) {
        this.id = message.getId();
        this.done = done;
        this.content = message.getContentRaw();
        this.referencedMessageId = referencedMessage.getId();
        net.dv8tion.jda.api.entities.User user = message.getAuthor();
        this.author = new User(user.getId(), user.getName());
    }

}
