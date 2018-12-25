# Java后端服务测试代码覆盖率检测

## ant build.xml

example:

```
<?xml version="1.0" ?>
<project name="fans-service" xmlns:jacoco="antlib:org.jacoco.ant" default="jacoco">
    <taskdef uri="antlib:org.jacoco.ant" resource="org/jacoco/ant/antlib.xml">
        <classpath path="./jacoco/lib/jacocoant.jar" />
    </taskdef>

    <target name="dump">
        <jacoco:dump address="t1-hongbao-002" port="7766" reset="false" destfile="fans-service_mock.exec" append="true"/>
        <jacoco:dump address="t1-hongbao-002" port="7765" reset="false" destfile="fans-service_test.exec" append="true"/>

    </target>
    <target name="merge">
        <jacoco:merge destfile="./jacoco-all.exec">
        <fileset dir="./" includes="*.exec"/>
        </jacoco:merge>
    </target>
</project>
```

## 测试服务部署

### 构建前，将jacoco打包到Dockerfile中

example:


```
cp -r /app/jacoco/jacoco-0 ./
sed -i '/^COPY/a\ADD jacoco-0 /jacoco' Dockerfile
```

### 启动参数增加javaagent

example:

```
cat > docker-compose.test.yml <<EOF
version: "2"
services:
  fans-service-test:
    environment:
      - JAVA_OPTIONS=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005 -Dspring.profiles.active=test -javaagent:/jacoco/lib/jacocoagent.jar=includes=*,output=tcpserver,port=7765,address=0.0.0.0
    ports:
      - "11085:8080"
      - "11185:5005"
      - "7765:7765"
EOF
```

### 在deploy之前需要备份当前状态下的代码覆盖率数据

```
sh /app/jacoco/backup.sh upay-activity t1-hongbao-001 7766

```

接收三个参数：

- 项目名称： 请务必与gitlab上的目录名称一致，例如：`upay-activity` 而不是 `upay-activity-mock` 或者 `upay-activity_test`
- host: 保持于测试服务器地址一致即可
- port: 配置的agent端口

添加该备份后，可不断的构建项目，代码覆盖率数据不会丢失。
例如：第一天测试完成50%的用例，第二天更新的service完成剩余的50%用例，此时代码覆盖率包含了这两天的执行结果

## 覆盖率报告获取

### 添加构建项目
![](http://pic.test7.hemayun.net/jacoco-jenkins-0.png)

如上图，换行填写你在gitlab上的目录文件地址，例如: `market/upay-activity`

### 点击 "Build with Parameters"
![](http://pic.test7.hemayun.net/jacoco-jenkins-1.png)

- 选择需要构建的项目
- 选择是否需要重新设置覆盖率数据

### 覆盖率报告
<!--![](http://pic.test7.hemayun.net/jacoco-jenkins-2.png)
-->
![](http://pic.test7.hemayun.net/jacoco-jenkins-3.png)

在这里你会看到更加详细的数据 [refer to jenkins](https://jenkins-test.wosai-inc.com/job/qa-jacoco-report/)

