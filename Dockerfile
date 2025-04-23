# 使用 Maven 3.9.9 和 Java 8 作为构建环境
FROM maven:3.9.9-amazoncorretto-8 AS build

# 设置工作目录
WORKDIR /app

# 复制 pom.xml 和源代码到容器中
COPY pom.xml .
COPY src ./src

# 设置淘宝镜像以加速 Maven 依赖下载
RUN mkdir -p /root/.m2 && \
    echo "<settings><mirrors><mirror><id>aliyunmaven</id><mirrorOf>central</mirrorOf><url>https://maven.aliyun.com/repository/public</url></mirror></mirrors></settings>" > /root/.m2/settings.xml

# 构建项目
RUN mvn clean package -DskipTests

# 使用 Java 8 作为运行环境
FROM openjdk:8-jdk-alpine

# 设置工作目录
WORKDIR /app

# 从构建阶段复制生成的 jar 文件
COPY --from=build /app/target/delyoj-0.0.1-SNAPSHOT.jar app.jar

# 暴露端口
EXPOSE 8101

# 启动应用
ENTRYPOINT ["java", "-Xmx512m", "-jar", "app.jar"]