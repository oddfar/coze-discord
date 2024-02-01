package com.oddfar.ai.discord.service;

import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;

/**
 * 频道服务接口
 *
 * @author oddfar
 * @since 2024/2/1
 */
public interface ChannelService {

    /**
     * 创建文本频道
     *
     * @param guildId    公会id
     * @param name       频道名称
     * @param categoryId 父类别 ID，为空时默认为创建父频道
     * @return TextChannel
     */
    TextChannel createTextChannel(String guildId, String name, String categoryId) throws InterruptedException;


    /**
     * 创建文本频道子区
     *
     * @param name      频道名称
     * @param channelId 父ID
     * @return TextChannel
     */
    ThreadChannel createThreadChannel(String name, String channelId);

}
