# Base, ubuntu 이미지
FROM ubuntu:18.04

# jdk 설치
RUN apt-get update && \
    apt-get install -y openjdk-17-jdk && \
    apt-get clean;

# work directory 지정
WORKDIR /app

COPY build/libs/OrderService.jar /app/app.jar

# 수행될 Command
CMD ["java", "-jar", "app.jar"]