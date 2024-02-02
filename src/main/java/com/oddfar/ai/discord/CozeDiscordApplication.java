package com.oddfar.ai.discord;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 使用两个Discord机器人代理Coze服务，免费使用GPT-4高级模型的API功能
 * GitHub: https://github.com/oddfar/coze-discord
 *
 * @author oddfar
 */
@SpringBootApplication
public class CozeDiscordApplication {

    public static void main(String[] args) {
        SpringApplication.run(CozeDiscordApplication.class, args);
    }

}
