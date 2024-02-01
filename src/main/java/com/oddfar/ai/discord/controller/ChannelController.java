package com.oddfar.ai.discord.controller;

import com.oddfar.ai.discord.config.properties.DiscordProperties;
import com.oddfar.ai.discord.domain.R;
import com.oddfar.ai.discord.domain.req.ChannelReq;
import com.oddfar.ai.discord.domain.resp.ChannelResp;
import com.oddfar.ai.discord.service.ChannelService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 创建频道
 *
 * @author oddfar
 * @since 2024/2/1
 */
@Slf4j
@RestController
@RequestMapping("/api/channel")
public class ChannelController {
    @Autowired
    private DiscordProperties discordProperties;
    @Autowired
    private ChannelService channelService;

    /**
     * 创建频道
     *
     * @param channelReq
     * @return R<ChannelResp>
     * @throws InterruptedException
     */
    @PostMapping("/createChannel")
    public R<ChannelResp> createChannel(@RequestBody ChannelReq channelReq) throws InterruptedException {

        TextChannel channel = channelService.createTextChannel(discordProperties.getGuildId(),
                channelReq.getName(), channelReq.getParentId());

        ChannelResp channelResp = ChannelResp.builder()
                .name(channel.getName())
                .id(channel.getId()).build();

        return R.ok(channelResp);
    }

    /**
     * 创建子区
     *
     * @param channelReq
     * @return R<ChannelResp>
     * @throws InterruptedException
     */
    @PostMapping("/createThreadChannel")
    public R<ChannelResp> createThreadChannel(@RequestBody ChannelReq channelReq) {
        ThreadChannel channel = channelService.createThreadChannel(channelReq.getName(), channelReq.getParentId());

        ChannelResp channelResp = ChannelResp.builder()
                .name(channel.getName())
                .id(channel.getId()).build();

        return R.ok(channelResp);
    }


}
