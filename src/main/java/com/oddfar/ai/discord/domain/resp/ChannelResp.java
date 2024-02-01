package com.oddfar.ai.discord.domain.resp;

import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 频道响应
 *
 * @author oddfar
 * @since 2024/2/1
 */
@Data
@Builder
public class ChannelResp implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * 频道ID
     */
    private String id;

    /**
     * 频道名称
     */
    private String name;
}
