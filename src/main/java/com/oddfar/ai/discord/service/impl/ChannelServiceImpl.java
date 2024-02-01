package com.oddfar.ai.discord.service.impl;

import com.oddfar.ai.discord.service.ChannelService;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 频道服务实现类
 *
 * @author oddfar
 * @since 2024/2/1
 */
@Service
public class ChannelServiceImpl implements ChannelService {
    @Autowired
    private JDA jda;

    /**
     * 创建文本频道
     *
     * @param guildId    公会id
     * @param name       频道名称
     * @param categoryId 父类别 ID，为空时默认为创建父频道
     * @return TextChannel
     */
    @Override
    public TextChannel createTextChannel(String guildId, String name, String categoryId) throws InterruptedException {
        JDA readyJDA = jda.awaitReady();
        //获取工会(xxx服务器)
        Guild guild = readyJDA.getGuildById(guildId);
        if (StringUtils.hasText(categoryId)) {
            Category category = readyJDA.getCategoryById(categoryId);
            return guild.createTextChannel(name, category).complete();
        } else {
            return guild.createTextChannel(name).complete();
        }
    }

    /**
     * 创建文本频道子区
     *
     * @param name      频道名称
     * @param channelId 父ID
     * @return TextChannel
     */
    @Override
    public ThreadChannel createThreadChannel(String name, String channelId) {
        TextChannel textChannel = jda.getTextChannelById(channelId);
        //创建子区
        return textChannel.createThreadChannel(name).complete();
    }
}
