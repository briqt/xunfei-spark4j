package io.github.briqt.spark4j.listener;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.briqt.spark4j.constant.SparkApiVersion;
import io.github.briqt.spark4j.model.SparkMessage;
import io.github.briqt.spark4j.model.request.SparkRequest;
import io.github.briqt.spark4j.model.response.SparkResponse;
import io.github.briqt.spark4j.model.response.SparkResponseFunctionCall;
import io.github.briqt.spark4j.model.response.SparkResponseUsage;
import io.github.briqt.spark4j.model.response.SparkTextUsage;
import okhttp3.WebSocket;

import java.util.List;
import java.util.Map;

/**
 * SparkConsoleListener
 *
 * @author briqt
 */
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
                SparkApiVersion apiVersion = sparkRequest.getApiVersion();
                System.out.println("请求地址：" + apiVersion.getUrl()+"  版本："+apiVersion.getVersion());
                System.out.println("\n提问：" + objectMapper.writeValueAsString(messages));
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
            System.out.println("\n完整回答：" + stringBuilder);
            SparkTextUsage textUsage = usage.getText();
            System.out.println("\n回答结束；提问tokens：" + textUsage.getPromptTokens()
                    + "，回答tokens：" + textUsage.getCompletionTokens()
                    + "，总消耗tokens：" + textUsage.getTotalTokens());
        }
    }

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
            System.out.println("\n收到functionCall：方法名称：" + functionCallName + "，参数：" + objectMapper.writeValueAsString(arguments));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
