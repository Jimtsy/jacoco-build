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

## 覆盖率报告获取

### configuration

```
cd $BUILD_PROJECT
SERVICE=${BUILD_PROJECT##*/}

# 更新源码文件 includes. *.java, *.class
cp -r /app/jenkins/data/workspace/${SERVICE}_test $SERVICE
cp -r /app/jacoco/jacoco-0 jacoco

# 从远程服务获取exec文件
ant dump
ant merge

# 更新tmp目录，存放最新的结果
cp -r ./$SERVICE tmp
cp jacoco-all.exec tmp/
rm *.exec

ls -alt $SERVICE
ls -alrt tmp
```

[refer to jenkins configuration](https://jenkins-test.wosai-inc.com/job/qa-jacoco/)

