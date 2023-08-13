# 科大讯飞星火认知大模型 Java SDK

JDK：1.8

项目依赖：`okhttp3`、`jackson` (最新稳定版)

### 引入项目

> 注意：项目还没有发布到Maven中心仓库，需要将源码下载后自行编译安装；建议直接将项目内嵌到业务系统中

```xml

<dependency>
    <groupId>com.github.briqt</groupId>
    <artifactId>xunfei-spark4j</artifactId>
    <version>0.1.0</version>
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
        .build();

        // 同步调用
        SparkSyncChatResponse chatResponse=sparkClient.chatSync(sparkRequest);
        SparkTextUsage textUsage=chatResponse.getTextUsage();

        System.out.println("\n回答："+chatResponse.getContent());
        System.out.println("\n提问tokens："+textUsage.getPromptTokens()
        +"，回答tokens："+textUsage.getCompletionTokens()
        +"，总消耗tokens："+textUsage.getTotalTokens());
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
