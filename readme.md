# 科大讯飞星火认知大模型 Java SDK

JDK：1.8

项目依赖：`okhttp3`、`jackson` (最新稳定版)

主要功能：

- 流式调用问答接口
- 同步调用问答接口
- 完整的请求和响应数据对象封装，包括状态、用户ID、tokens统计等
- 封装简化了webSocket对接，提供简单便捷的SparkClient
- 完整的业务异常信息



## 使用方式



### 引入项目


```xml
<dependency>
    <groupId>io.github.briqt</groupId>
    <artifactId>xunfei-spark4j</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 创建客户端

```java
SparkClient sparkClient=new SparkClient();

// 设置认证信息
sparkClient.appid="";
sparkClient.apiKey="";
sparkClient.apiSecret="";

```

### 同步调用

```java
// 消息列表，可以在此列表添加历史对话记录
List<SparkMessage> messages=new ArrayList<>();
messages.add(SparkMessage.userContent("请你扮演我的语文老师李老师，问我讲解问题问题，希望你可以保证知识准确，逻辑严谨。"));
messages.add(SparkMessage.assistantContent("好的，这位同学，有什么问题需要李老师为你解答吗？"));
messages.add(SparkMessage.userContent("鲁迅和周树人小时候打过架吗？"));

// 构造请求
SparkRequest sparkRequest=SparkRequest.builder()
// 消息列表
.messages(messages)
// 模型回答的tokens的最大长度,非必传,取值为[1,4096],默认为2048
.maxTokens(2048)
// 核采样阈值。用于决定结果随机性,取值越高随机性越强即相同的问题得到的不同答案的可能性越高 非必传,取值为[0,1],默认为0.5
.temperature(0.2)
// 指定请求版本，默认使用最新2.0版本
.apiVersion(SparkApiVersion.V2_0)
.build();

// 同步调用
SparkSyncChatResponse chatResponse=sparkClient.chatSync(sparkRequest);
SparkTextUsage textUsage=chatResponse.getTextUsage();

System.out.println("\n回答："+chatResponse.getContent());
System.out.println("\n提问tokens："+textUsage.getPromptTokens()
+"，回答tokens："+textUsage.getCompletionTokens()
+"，总消耗tokens："+textUsage.getTotalTokens());
```

控制台输出：

```
提问：[{"role":"user","content":"请你扮演我的语文老师李老师，问我讲解问题问题，希望你可以保证知识准确，逻辑严谨。"},{"role":"assistant","content":"好的，这位同学，有什么问题需要李老师为你解答吗？"},{"role":"user","content":"鲁迅和周树人小时候打过架吗？"}]

回答：是的，鲁迅和周树人小时候确实打过架。据周作人的回忆录《鲁迅的故事》记载，他和鲁迅小时候在私塾里学习时，因为争夺一张桌子而打了起来，结果两人都受了伤。不过，这次打架并没有影响他们之间的友谊和合作关系。

提问tokens：66，回答tokens：68，总消耗tokens：134
```

### 流式调用

- 继承`SparkBaseListener`，重写6个参数的`onMessage`方法，参考`SparkConsoleListener`

```java
public class SparkConsoleListener extends SparkBaseListener {

    private final StringBuilder stringBuilder = new StringBuilder();

    public ObjectMapper objectMapper = new ObjectMapper();

    {
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    @Override
    public void onMessage(String content, SparkResponseUsage usage, Integer status, SparkRequest sparkRequest, SparkResponse sparkResponse, WebSocket webSocket) {
        stringBuilder.append(content);

        if (0 == status) {
            List<SparkMessage> messages = sparkRequest.getPayload().getMessage().getText();
            try {
                System.out.println("提问：" + objectMapper.writeValueAsString(messages));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            System.out.println("\n收到回答：\n");
        }

        try {
            System.out.println("--content：" + content + " --完整响应：" + objectMapper.writeValueAsString(sparkResponse));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        if (2 == status) {
            System.out.println("\n完整回答：" + stringBuilder.toString());
            SparkTextUsage textUsage = usage.getText();
            System.out.println("\n回答结束；提问tokens：" + textUsage.getPromptTokens()
                    + "，回答tokens：" + textUsage.getCompletionTokens()
                    + "，总消耗tokens：" + textUsage.getTotalTokens());
        }
    }
}
```

- 调用代码

```java
// 消息列表，可以在此列表添加历史对话记录
List<SparkMessage> messages=new ArrayList<>();
messages.add(SparkMessage.userContent("请你扮演我的语文老师李老师，问我讲解问题问题，希望你可以保证知识准确，逻辑严谨。"));
messages.add(SparkMessage.assistantContent("好的，这位同学，有什么问题需要李老师为你解答吗？"));
messages.add(SparkMessage.userContent("鲁迅和周树人小时候打过架吗？"));

// 构造请求
SparkRequest sparkRequest=SparkRequest.builder()
// 消息列表
.messages(messages)
// 模型回答的tokens的最大长度,非必传,取值为[1,4096],默认为2048
.maxTokens(2048)
// 核采样阈值。用于决定结果随机性,取值越高随机性越强即相同的问题得到的不同答案的可能性越高 非必传,取值为[0,1],默认为0.5
.temperature(0.2)
.build();

// 使用默认的控制台监听器，流式调用；
// 实际使用时请继承SparkBaseListener自定义监听器实现
sparkClient.chatStream(sparkRequest,new SparkConsoleListener());
```

控制台输出：

```
提问：[{"role":"user","content":"请你扮演我的语文老师李老师，问我讲解问题问题，希望你可以保证知识准确，逻辑严谨。"},{"role":"assistant","content":"好的，这位同学，有什么问题需要李老师为你解答吗？"},{"role":"user","content":"鲁迅和周树人小时候打过架吗？"}]

收到回答：

--content：是--完整响应：{"header":{"code":0,"status":0,"message":"Success","sid":"xxx"},"payload":{"choices":{"status":0,"seq":0,"text":[{"role":"assistant","content":"是","index":"0"}]}}}
--content：的， --完整响应：{"header":{"code":0,"status":1,"message":"Success","sid":"xxx"},"payload":{"choices":{"status":1,"seq":1,"text":[{"role":"assistant","content":"的，","index":"0"}]}}}
--content：鲁迅和周--完整响应：{"header":{"code":0,"status":1,"message":"Success","sid":"xxx"},"payload":{"choices":{"status":1,"seq":2,"text":[{"role":"assistant","content":"鲁迅和周","index":"0"}]}}}
--content：树人小时候确实--完整响应：{"header":{"code":0,"status":1,"message":"Success","sid":"xxx"},"payload":{"choices":{"status":1,"seq":3,"text":[{"role":"assistant","content":"树人小时候确实","index":"0"}]}}}
--content：打过架。 --完整响应：{"header":{"code":0,"status":1,"message":"Success","sid":"xxx"},"payload":{"choices":{"status":1,"seq":4,"text":[{"role":"assistant","content":"打过架。","index":"0"}]}}}
--content：据周作人的回忆录《鲁迅的故事》记载，他和鲁迅小时候在浙江绍兴的一所私塾里读书，两人因为争夺一张桌子而打了起来，最终被老师分开。这件事情也成为了他们之间的一个小秘密。 --完整响应：{"header":{"code":0,"status":2,"message":"Success","sid":"xxx"},"payload":{"choices":{"status":2,"seq":5,"text":[{"role":"assistant","content":"据周作人的回忆录《鲁迅的故事》记载，他和鲁迅小时候在浙江绍兴的一所私塾里读书，两人因为争夺一张桌子而打了起来，最终被老师分开。这件事情也成为了他们之间的一个小秘密。","index":"0"}]},"usage":{"text":{"prompt_tokens":66,"completion_tokens":65,"total_tokens":131,"question_tokens":20}}}}

完整回答：是的，鲁迅和周树人小时候确实打过架。据周作人的回忆录《鲁迅的故事》记载，他和鲁迅小时候在浙江绍兴的一所私塾里读书，两人因为争夺一张桌子而打了起来，最终被老师分开。这件事情也成为了他们之间的一个小秘密。

回答结束；提问tokens：66，回答tokens：65，总消耗tokens：131
```
