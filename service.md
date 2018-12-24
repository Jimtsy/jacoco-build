
## upay-activity 测试服务构建说明

1. 构建前，将jacoco打包到Dockerfile中

```
cd ./upay-activity
cp -r /app/jacoco/jacoco-0 ./
sed -i '/^COPY/a\ADD jacoco-0 /jacoco' Dockerfile
```

2. 启动参数增加javaagent

```
cat > docker-compose.yml <<EOF
version: '2'
services:
  upay-activity-{{ env }}:
    build: .
    image: registry.wosai-inc.com/upay-activity:{{tag}}
    restart: always
    container_name: upay-activity-{{ env }}
    environment:
      - JAVA_OPTIONS=-Xms256m -Xmx2G -Dshouqianba.flavor={{ env }} -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005 -javaagent:/jacoco/lib/jacocoagent.jar=includes=*,output=tcpserver,port=7766,address=0.0.0.0
      - logDir=/var/lib/jetty/logs
      - rpcLogDir=/var/lib/jetty/logs
    ports:
      - "11138:8080"
      - "5005:5005"
      - "7766:7766"
    volumes:
      - /app/log/upay-activity-{{ env }}:/var/lib/jetty/logs
      - /opt/data:/opt/data
      - /opt/settings:/opt/settings
EOF
```