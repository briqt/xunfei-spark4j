package io.github.briqt.spark4j.model;

import io.github.briqt.spark4j.model.response.SparkResponse;
import io.github.briqt.spark4j.model.response.SparkTextUsage;

import java.io.Serializable;

/**
 * SparkTextChatResponse
 *
 * @author briqt
 */
public class SparkSyncChatResponse implements Serializable {
    private static final long serialVersionUID = -6785055441385392782L;

    /**
     * 回答内容
     */
    private String content;

    /**
     * 最后一次响应
     */
    private SparkResponse lastResponse;

    /**
     * tokens统计
     */
    private SparkTextUsage textUsage;

    /**
     * 内部自用字段
     */
    private boolean ok = false;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public SparkResponse getLastResponse() {
        return lastResponse;
    }

    public void setLastResponse(SparkResponse lastResponse) {
        this.lastResponse = lastResponse;
    }

    public SparkTextUsage getTextUsage() {
        return textUsage;
    }

    public void setTextUsage(SparkTextUsage textUsage) {
        this.textUsage = textUsage;
    }

    public boolean isOk() {
        return ok;
    }

    public void setOk(boolean ok) {
        this.ok = ok;
    }
}
