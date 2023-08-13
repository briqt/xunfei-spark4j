package com.github.briqt.spark4j.listener;

import com.github.briqt.spark4j.model.SparkSyncChatResponse;
import com.github.briqt.spark4j.model.request.SparkRequest;
import com.github.briqt.spark4j.model.response.SparkResponse;
import com.github.briqt.spark4j.model.response.SparkResponseUsage;
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
}
