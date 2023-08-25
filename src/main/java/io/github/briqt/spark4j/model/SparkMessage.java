package io.github.briqt.spark4j.model;

import io.github.briqt.spark4j.constant.SparkMessageRole;

/**
 * 消息
 *
 * @author briqt
 */
public class SparkMessage {

    /**
     * 角色
     */
    private String role;

    /**
     * 内容
     */
    private String content;

    /**
     * 响应时独有，请求入参请忽略
     */
    private String index;

    /**
     * 创建用户消息
     *
     * @param content 内容
     */
    public static SparkMessage userContent(String content) {
        return new SparkMessage(SparkMessageRole.USER, content);
    }

    /**
     * 创建机器人消息
     *
     * @param content 内容
     */
    public static SparkMessage assistantContent(String content) {
        return new SparkMessage(SparkMessageRole.ASSISTANT, content);
    }

    public SparkMessage() {
    }

    public SparkMessage(String role, String content) {
        this.role = role;
        this.content = content;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getIndex() {
        return index;
    }
}
