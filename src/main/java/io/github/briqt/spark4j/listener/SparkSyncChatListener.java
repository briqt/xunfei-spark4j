package io.github.briqt.spark4j.listener;

import io.github.briqt.spark4j.model.SparkSyncChatResponse;
import io.github.briqt.spark4j.model.request.SparkRequest;
import io.github.briqt.spark4j.model.response.SparkResponse;
import io.github.briqt.spark4j.model.response.SparkResponseFunctionCall;
import io.github.briqt.spark4j.model.response.SparkResponseUsage;
import okhttp3.WebSocket;

/**
 * SparkSyncChatListener
 *
 * @author briqt
 */
public class SparkSyncChatListener extends SparkBaseListener {

    private final StringBuilder stringBuilder = new StringBuilder();

    private final SparkSyncChatResponse sparkSyncChatResponse;

    public SparkSyncChatListener(SparkSyncChatResponse sparkSyncChatResponse) {
        this.sparkSyncChatResponse = sparkSyncChatResponse;
    }

    @Override
    public void onMessage(String content, SparkResponseUsage usage, Integer status, SparkRequest sparkRequest, SparkResponse sparkResponse, WebSocket webSocket) {
        stringBuilder.append(content);
        if (2 == status) {
            sparkSyncChatResponse.setContent(stringBuilder.toString());
            sparkSyncChatResponse.setTextUsage(usage.getText());
            sparkSyncChatResponse.setOk(true);
        }
    }

    /**
     * 收到functionCall调用此方法
     *
     * @param functionCall  functionCall
     * @param sparkRequest  本次会话的请求参数
     * @param sparkResponse 本次回调的响应数据
     * @param webSocket     本次会话的webSocket连接
     */
    @Override
    public void onFunctionCall(SparkResponseFunctionCall functionCall, SparkResponseUsage usage, Integer status, SparkRequest sparkRequest, SparkResponse sparkResponse, WebSocket webSocket) {
        if (2 == status) {
            sparkSyncChatResponse.setContent(stringBuilder.toString());
            sparkSyncChatResponse.setTextUsage(usage.getText());
            sparkSyncChatResponse.setFunctionCall(functionCall);
            sparkSyncChatResponse.setOk(true);
        }
    }
}
