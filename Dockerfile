FROM openjdk:17

MAINTAINER oddfar

# 创建目录
RUN mkdir -p /home/campus/conf

WORKDIR /campus/server

ENV SERVER_PORT=8080 LANG=C.UTF-8 LC_ALL=C.UTF-8 JAVA_OPTS=""

EXPOSE ${SERVER_PORT}

ADD ./target/*.jar ./app.jar


ENTRYPOINT java -Djava.security.egd=file:/dev/./urandom -Dserver.port=${SERVER_PORT} \
           -Ddiscord.botToken=${DDISCORD_BOT_TOKEN}\
           -Ddiscord.guildId=${DDISCORD_GUILD_ID}\
           -Ddiscord.cozeBotId=${DDISCORD_COZE_BOT_ID}\
           -Ddiscord.channelId=${DDISCORD_CHANNEL_ID}\
           -jar app.jar \
           -XX:+HeapDumpOnOutOfMemoryError -Xlog:gc*,:time,tags,level -XX:+UseZGC ${JAVA_OPTS}

