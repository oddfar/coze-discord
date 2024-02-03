FROM openjdk:17

MAINTAINER oddfar

# 创建目录
RUN mkdir -p /home/campus/conf

WORKDIR /campus/server

ENV SERVER_PORT=8080 LANG=C.UTF-8 LC_ALL=C.UTF-8 JAVA_OPTS=""

EXPOSE ${SERVER_PORT}

ADD ./target/*.jar ./app.jar


ENTRYPOINT java -Djava.security.egd=file:/dev/./urandom -Dserver.port=${SERVER_PORT} \
           -Ddiscord.botToken=${DISCORD_BOT_TOKEN}\
           -Ddiscord.guildId=${DISCORD_GUILD_ID}\
           -Ddiscord.cozeBotId=${DISCORD_COZE_BOT_ID}\
           -Ddiscord.channelId=${DISCORD_CHANNEL_ID}\
           -Ddiscord.proxyHostPort=${DISCORD_PROXY_HOST_PORT}\
           -jar app.jar \
           -XX:+HeapDumpOnOutOfMemoryError -Xlog:gc*,:time,tags,level -XX:+UseZGC ${JAVA_OPTS}

