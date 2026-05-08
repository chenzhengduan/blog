# syntax=docker/dockerfile:1

# xzagent 运行时镜像
# 基于运维提供的 Playwright 基础镜像（含 JDK17 + Chromium + 中文字体）
FROM xiaogj-registry-vpc.cn-hangzhou.cr.aliyuncs.com/base/jdk17:v17playwright02

# 清理邮件目录（避免 k8s 权限告警），统一时区
RUN rm -rf /var/mail /var/spool/mail && \
    ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo 'Asia/Shanghai' > /etc/timezone

WORKDIR /app
VOLUME /tmp
VOLUME /logs

# CI Runner 编译产物以 app.jar 传入（对应 CI 的 --build-arg JAR_FILE=app.jar）
ARG JAR_FILE=app.jar
COPY ${JAR_FILE} app.jar

# 声明端口，与 deployment.yaml SERVER_PORT 保持一致
ARG SERVER_PORT=9510
EXPOSE ${SERVER_PORT}

# JVM 参数：容器感知内存、OOM 堆转储、时区、编码
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 \
    -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/logs/ \
    -Duser.timezone=Asia/Shanghai -Dfile.encoding=UTF-8"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar app.jar"]
