package io.github.briqt.spark4j.constant;

/**
 * SparkApiVersion
 *
 * @author briqt
 * @date 2023/8/31
 */
public enum SparkApiVersion {

    /**
     * 1.5版本
     */
    V1_5("v1.1", "https://spark-api.xf-yun.com/v1.1/chat", "lite"),

    /**
     * 2.0版本
     */
    V2_0("v2.1", "https://spark-api.xf-yun.com/v2.1/chat", "generalv2"),

    /**
     * 3.0版本
     */
    V3_0("v3.1", "https://spark-api.xf-yun.com/v3.1/chat", "generalv3"),

    /**
     * 3.5版本
     */
    V3_5("v3.5", "https://spark-api.xf-yun.com/v3.5/chat", "generalv3.5"),

    /**
     * 4.0版本
     */
    V4_0("v4.0", "https://spark-api.xf-yun.com/v4.0/chat", "4.0Ultra"),
    ;

    SparkApiVersion(String version, String url, String domain) {
        this.version = version;
        this.url = url;
        this.domain = domain;
    }

    private final String version;

    private final String url;

    private final String domain;

    public String getVersion() {
        return version;
    }

    public String getUrl() {
        return url;
    }

    public String getDomain() {
        return domain;
    }
}
