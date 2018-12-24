# 代码覆盖率统计工具与实践应用

## 1. 代码覆盖率是什么？

阅读下面两篇文章就清楚了，本文的重点在于实践应用。

[官方文档](https://www.jacoco.org/jacoco/trunk/doc/index.html)

[原理及介绍篇](https://blog.csdn.net/TMQ1225/article/details/52221187)

## 2. 代码覆盖率检测有什么作用？

从QA角度来看，我认为至少可以解决以下几个痛点：

- 开发的技术方案细节描述不清，不了解开发实现，不知道该如何测试，会出现遗漏怎么办？
- 产品体验太差了，测试一半感觉很多问题，该如何了解产品体验情况？
- 接口自动化用例覆盖度统计
- 开发随意修改的代码未经过测试带上线
- 产品上线出现问题，发现有一个点没有测到
- 开发质量评估

代码覆盖率的检测，让我们有目的性的接触到开发的代码，然而又无需我们会java代码。
一个常用的使用场景：项目测试完成后，可以看出哪些类、方法, 逻辑分支，或其他代码未测试到，规避更多的问题，也可以反推开发代码开发质量。


## 3. 一个简单的spring boot项目演示

写了一个简单的示例初步了解该工具

### 3.1 项目说明

#### 3.1.1 项目结构

```
$ tree
.
├── build.xml
├── demo.iml
├── mvnw
├── mvnw.cmd
├── pom.xml
└── src
    ├── main
    │   ├── java
    │   │   └── com
    │   │       └── example
    │   │           └── demo
    │   │               ├── DemoApplication.java
    │   │               ├── controller
    │   │               │   └── CalculatorController.java
    │   │               └── service
    │   │                   └── Calculator.java
    │   └── resources
    │       ├── application.properties
    │       ├── static
    │       └── templates
    └── test
        └── java
            └── com
                └── example
                    └── demo
                        └── service
                            └── CalculatorTest.java

```

#### 3.1.2 conntroller.CalculatorController.java

定义了两个handler，加法、除法

```
package com.example.demo.controller;

import com.example.demo.service.Calculator;
import jdk.nashorn.internal.objects.annotations.Constructor;
import org.springframework.web.bind.annotation.*;

import javax.jws.WebResult;

@RestController
@RequestMapping("/")
public class CalculatorController {

    @RequestMapping(value = "/add/{v1}/{v2}", method = RequestMethod.GET)
    public int add(@PathVariable("v1") Integer v1, @PathVariable("v2") Integer v2){
        Calculator calculator = new Calculator();
        return calculator.add(v1, v2);
    }

    @RequestMapping(value = "/divide/{v1}/{v2}", method = RequestMethod.GET)
    public int divide(@PathVariable("v1") Integer v1, @PathVariable("v2") Integer v2){
        Calculator calculator = new Calculator();
        return calculator.devise(v1, v2);
    }
}

```

#### 3.1.3 service.Caculator

加法、除法实际逻辑

```
package com.example.demo.service;

public class Calculator {

    public int add(int a, int b) {
        return a + b;
    }

    public int divide(int a, int b) {
        if (b == 0) {
            // 很明显这是一个bug
            return 0;
        }
        return a - b;
    }
}
```

### 3.2 项目运行

mvn编译后启动，需要注意两点：

- 因为写了单元测试，这里要忽略单元测试运行
- 启动参数增加javaagent

```
java -jar target/demo-0.0.1-SNAPSHOT.jar -javaagent:/jacoco/lib/jacocoagent.jar=includes=*,output=tcpserver,port=7765,address=0.0.0.0 -DskipTest=true

```

### 3.3 执行用例

假设我们的测试用例只有两条：

```
curl http://localhost:8080/add/1/2
curl http://localhost:8080/divide/1/2

```

很明显这里**遗漏**了一个除数=0的逻辑分支

### 3.4 查看代码覆盖率报告

#### 3.4.1 生成测试报告

- ant dump

	ant dump 从远程javaagent获取exec文件，这个文件用来生成测试报告

- ant report

	ant report 用于生成一个测试报告，这些都是在pom.xml文件中定义的。通过阅读后面的内容可知，后面测试项目集成使用这个工具，无需使用pom.xml文件，所以这里一笔带过。

ant report 后会在指定目录下生成一个index.html文件，使用浏览器打开

#### 3.4.2 查看测试报告

- layer-1: 展示了 package 级别(文件)覆盖率统计
	![](http://pic.test7.hemayun.net/jacoco-demo-1.jpg)

- layer-2: 展示了 class 级别覆盖率统计
	![](http://pic.test7.hemayun.net/jacoco-demo-4.jpg)

- layer-3: 展示了 function 级别覆盖率统计
   ![](http://pic.test7.hemayun.net/jacoco-demo-2.jpg)

- layer-4: 展示了具体的代码覆盖率统计内容，其中红色表示未覆盖到，绿色表示完全覆盖，黄色表示条件覆盖(图中编码问题可忽略，实际不会有这个问题)
	![](http://pic.test7.hemayun.net/jacoco-demo-3.jpg)

大家可以重点关注下Branchers, Lines, Methods, Classes，分别表示逻辑分支，行数，方法，类。
字节码指令instructions和圈复杂度cxty( Cyclomatic Complexity)除了看出代码覆盖率，还未想到其他作用。

### 3.5 分析结论

结果一目了然，从这个报告我们得出两个结论：

1. divide接口测试中我们遗漏了除数=0的用例
2. 这个开发水平有点low，这里有bug。

## 4. 实际应用

### 4.1 单元测试

适用于开发，集成到maven, maven test 会生成报告，不做重点说明，也可以在gitlab CI下构建 [可参考fans-service](https://git.wosai-inc.com/marketing/fans-service/blob/master/.gitlab-ci.yml)。

### 4.2 功能测试及接口测试

[详细查看qa/jacoco项目说明](https://git.wosai-inc.com/qa/jacoco), 如何在自己的java项目中集成这个工具？

#### 4.2.1 重新构建服务

git中已经存在两个项目 market/fans-service, market/upay-activity，大家可以参考下，

主要思路：

- 将jacoco文件加在Dockerfile中，容器运行javaagent时使用

```
cp -r /app/jacoco/jacoco-0 ./
sed -i '/^COPY/a\ADD jacoco-0 /jacoco' Dockerfile
```

- JVM启动参数增加javaagent

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

服务起来后，telnet host 7765 如果连接成功，说明配置成功。

#### 4.2.2 配置build.xml

ant build.xml文件用于从远程服务(即测试服务器的javaagent)获取执行的记录exec文件

如下所示，fans-service项目我部署了两个实例，分别为fans-service_test 和 fans-service-mock_test， 前者作为功能测试，后者作为接口用例测试。最最终jacoco会将两个测试内容覆盖整合成一个exex文件并生成测试报告。

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

只需要改动两点：

1. name                ---- 项目名称，其实无任何作用，注明是哪个项目的build.xml
2. target name="dump"  ---- dump配置，可配置多个服务


## 问题讨论

现在回过头来看看之前提出的几个问题


> 开发的技术方案细节描述不清，不了解开发实现，不知道该如何测试，会出现遗漏怎么办？

项目测试完成后(功能测试 & 接口测试)，查看下代码覆盖率，如果开发的实现未测试到，不仅可以拿到这个信息，也可以看下开发针对这部分的实现逻辑。

> 产品体验太差了，测试一半感觉很多问题，该如何了解产品体验情况？

产品提测后，我们可以查看下当前代码覆盖率，如果覆盖率很低，基本上可以断定产品未有效体验

> 接口自动化用例覆盖度统计

目前的统计方式应该都是大致估算，这个工具下可以更客观的展示接口覆盖率情况

> 开发随意修改的代码未经过测试带上线

如果认真分析了覆盖率报告，这个问题应该不会存在

> 产品上线出现问题，发现有一个点没有测到

同理，如果认真分析了覆盖率报告，这个问题应该不会存在

> 开发质量评估

如果进行了充分的测试后，还发现覆盖率较低。通过分析未覆盖部分的代码，可以了解开发的实现逻辑，从而反推开发质量

## 总结

代码覆盖率高与低都不能说明质量的高低，代码覆盖率检测报告目前只能作为一个参考，需要我们在实践中不断探索总结出适用于我们公司的使用方式。
