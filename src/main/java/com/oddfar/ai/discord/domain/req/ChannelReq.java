package com.oddfar.ai.discord.domain.req;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 频道请求参数
 *
 * @author oddfar
 * @since 2024/2/1
 */
@Data
public class ChannelReq implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 父频道/父类别 ID，为空时默认为创建父频道
     */
    private String parentId;

    /**
     * 频道名称
     */
    private String name;
}
