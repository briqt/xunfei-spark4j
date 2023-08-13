package com.github.briqt.spark4j;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.briqt.spark4j.listener.SparkConsoleListener;
import com.github.briqt.spark4j.model.SparkMessage;
import com.github.briqt.spark4j.model.SparkSyncChatResponse;
import com.github.briqt.spark4j.model.request.SparkRequest;
import com.github.briqt.spark4j.model.response.SparkTextUsage;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * SparkClientTest
 *
 * @author briqt
 */
public class SparkClientTest {

    /**
     * 客户端实例，线程安全
     */
    SparkClient sparkClient = new SparkClient();

    // 设置认证信息
    {
        sparkClient.appid = "";
        sparkClient.apiKey = "";
        sparkClient.apiSecret = "";
    }

    @Test
    void chatStreamTest() throws InterruptedException {
        // 消息列表，可以在此列表添加历史对话记录
        List<SparkMessage> messages = new ArrayList<>();
        messages.add(SparkMessage.userContent("请你扮演我的语文老师李老师，问我讲解问题问题，希望你可以保证知识准确，逻辑严谨。"));
        messages.add(SparkMessage.assistantContent("好的，这位同学，有什么问题需要李老师为你解答吗？"));
        messages.add(SparkMessage.userContent("鲁迅和周树人小时候打过架吗？"));

        // 构造请求
        SparkRequest sparkRequest = SparkRequest.builder()
                // 消息列表
                .messages(messages)
                // 模型回答的tokens的最大长度,非必传,取值为[1,4096],默认为2048
                .maxTokens(2048)
                // 核采样阈值。用于决定结果随机性,取值越高随机性越强即相同的问题得到的不同答案的可能性越高 非必传,取值为[0,1],默认为0.5
                .temperature(0.2)
                .build();

        // 使用默认的控制台监听器，流式调用；
        // 实际使用时请继承SparkBaseListener自定义监听器实现
        sparkClient.chatStream(sparkRequest, new SparkConsoleListener());

        Thread.sleep(60000);
    }

    @Test
    void chatSyncTest() throws JsonProcessingException {
        // 消息列表，可以在此列表添加历史对话记录
        List<SparkMessage> messages = new ArrayList<>();
        messages.add(SparkMessage.userContent("请你扮演我的语文老师李老师，问我讲解问题问题，希望你可以保证知识准确，逻辑严谨。"));
        messages.add(SparkMessage.assistantContent("好的，这位同学，有什么问题需要李老师为你解答吗？"));
        messages.add(SparkMessage.userContent("鲁迅和周树人小时候打过架吗？"));

        // 构造请求
        SparkRequest sparkRequest = SparkRequest.builder()
                // 消息列表
                .messages(messages)
                // 模型回答的tokens的最大长度,非必传,取值为[1,4096],默认为2048
                .maxTokens(2048)
                // 核采样阈值。用于决定结果随机性,取值越高随机性越强即相同的问题得到的不同答案的可能性越高 非必传,取值为[0,1],默认为0.5
                .temperature(0.2)
                .build();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        System.out.println("提问：" + objectMapper.writeValueAsString(messages));

        // 同步调用
        SparkSyncChatResponse chatResponse = sparkClient.chatSync(sparkRequest);
        SparkTextUsage textUsage = chatResponse.getTextUsage();

        System.out.println("\n回答：" + chatResponse.getContent());
        System.out.println("\n提问tokens：" + textUsage.getPromptTokens()
                + "，回答tokens：" + textUsage.getCompletionTokens()
                + "，总消耗tokens：" + textUsage.getTotalTokens());
    }
}
