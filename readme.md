# 科大讯飞星火认知大模型 Java SDK

JDK：1.8

项目依赖：`okhttp3`、`jackson` 、`slf4j-api`

主要功能：

- 流式调用问答接口
- 同步调用问答接口
- 完整的请求和响应数据对象封装，包括状态、用户ID、tokens统计等
- 封装简化了webSocket对接，提供简单便捷的SparkClient
- 完整的业务异常信息
- function call



## 使用方式



### 引入项目


```xml
<!--修改version为最新稳定版-->

<dependency>
    <groupId>io.github.briqt</groupId>
    <artifactId>xunfei-spark4j</artifactId>
    <version>1.1.2</version>
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
messages.add(SparkMessage.systemContent("请你扮演我的语文老师李老师，问我讲解问题问题，希望你可以保证知识准确，逻辑严谨。"));
messages.add(SparkMessage.userContent("鲁迅和周树人小时候打过架吗？"));
// 构造请求
SparkRequest sparkRequest=SparkRequest.builder()
// 消息列表
.messages(messages)
// 模型回答的tokens的最大长度,非必传，默认为2048。
// V1.5取值为[1,4096]
// V2.0取值为[1,8192]
// V3.0取值为[1,8192]
.maxTokens(2048)
// 核采样阈值。用于决定结果随机性,取值越高随机性越强即相同的问题得到的不同答案的可能性越高 非必传,取值为[0,1],默认为0.5
.temperature(0.2)
// 指定请求版本，默认使用最新2.0版本
.apiVersion(SparkApiVersion.V2_0)
.build();

try {
    // 同步调用
    SparkSyncChatResponse chatResponse = sparkClient.chatSync(sparkRequest);
    SparkTextUsage textUsage = chatResponse.getTextUsage();
    
    System.out.println("\n回答：" + chatResponse.getContent());
    System.out.println("\n提问tokens：" + textUsage.getPromptTokens()
            + "，回答tokens：" + textUsage.getCompletionTokens()
            + "，总消耗tokens：" + textUsage.getTotalTokens());
} catch (SparkException e) {
    System.out.println("发生异常了：" + e.getMessage());
}
```

控制台输出：

```
提问：[{"role":"system","content":"请你扮演我的语文老师李老师，问我讲解问题问题，希望你可以保证知识准确，逻辑严谨。"},{"role":"user","content":"鲁迅和周树人小时候打过架吗？"}]

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
// 模型回答的tokens的最大长度,非必传，默认为2048。
// V1.5取值为[1,4096]
// V2.0取值为[1,8192]
// V3.0取值为[1,8192]
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

### functionCall

- 在请求时注册方法

```java
// 构造请求
SparkRequest sparkRequest = SparkRequest.builder()
        // 消息列表
        .messages(messages)
        // 使用functionCall功能版本需要大于等于3.0
        .apiVersion(SparkApiVersion.V3_0)
        // 添加方法，可多次调用添加多个方法
        .addFunction(
                // 回调时回传的方法名
                SparkFunctionBuilder.functionName("stockPrice")
                        // 让大模型理解方法意图 方法描述
                        .description("根据公司名称查询最新股票价格")
                        // 方法需要的参数。可多次调用添加多个参数
                        .addParameterProperty("companyName", "string", "公司名称")
                        // 指定以上的参数哪些是必传的
                        .addParameterRequired("companyName").build()
        ).build();
```

- 流式调用：需要重写`SparkBaseListener`的`onFunctionCall`方法，当会话触发functionCall时调用`onFunctionCall`方法，不再调用`onMessage`，其中第一个参数`SparkResponseFunctionCall`包含命中的方法名和具体参数。参考`SparkConsoleListener`写法。

```java
/**
 * 收到functionCall调用此方法
 *
 * @param functionCall  functionCall
 * @param usage         tokens消耗统计
 * @param status        会话状态，取值为[0,1,2]；0代表首次结果；1代表中间结果；2代表最后一个结果，当status为2时，webSocket连接会自动关闭
 * @param sparkRequest  本次会话的请求参数
 * @param sparkResponse 本次回调的响应数据
 * @param webSocket     本次会话的webSocket连接
 */
@Override
public void onFunctionCall(SparkResponseFunctionCall functionCall, SparkResponseUsage usage, Integer status, SparkRequest sparkRequest, SparkResponse sparkResponse, WebSocket webSocket) {
    String functionCallName = functionCall.getName();
    Map<String, Object> arguments = functionCall.getMapArguments();

    // 在这里根据方法名和参数自行调用方法实现

    try {
        System.out.println("\n收到functionCall：方法名称：" + functionCallName + 
                           "，参数：" + objectMapper.writeValueAsString(arguments));
    } catch (JsonProcessingException e) {
        throw new RuntimeException(e);
    }
}
```

- 同步调用：参考`SparkClientTest.functionCallTest()`

```java
// 同步调用
SparkSyncChatResponse chatResponse = sparkClient.chatSync(sparkRequest);
SparkTextUsage textUsage = chatResponse.getTextUsage();
SparkResponseFunctionCall functionCall = chatResponse.getFunctionCall();

if (null != functionCall) {
    String functionName = functionCall.getName();
    Map<String, Object> arguments = functionCall.getMapArguments();

    System.out.println("\n收到functionCall：方法名称：" + functionName + "，参数：" + objectMapper.writeValueAsString(arguments));

    // 在这里根据方法名和参数自行调用方法实现

} else {
    System.out.println("\n回答：" + chatResponse.getContent());
}
```
