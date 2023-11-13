package io.github.briqt.spark4j;

import io.github.briqt.spark4j.constant.SparkApiVersion;
import io.github.briqt.spark4j.exception.SparkException;
import io.github.briqt.spark4j.listener.SparkBaseListener;
import io.github.briqt.spark4j.listener.SparkSyncChatListener;
import io.github.briqt.spark4j.model.SparkSyncChatResponse;
import io.github.briqt.spark4j.model.request.SparkRequest;
import io.github.briqt.spark4j.utils.AuthorizationUtils;
import lombok.Data;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

import static io.github.briqt.spark4j.utils.AuthorizationUtils.getAuthUrl;

/**
 * XfSparkClient
 *
 * @author briqt
 */
@Configuration
@ConfigurationProperties("xunfei.client")
@Data
@ComponentScan
public class SparkClient {

    public String appid;

    public String apiKey;

    public String apiSecret;

    public OkHttpClient client = new OkHttpClient.Builder().build();

    public void chatStream(SparkRequest sparkRequest, SparkBaseListener listener) {
        sparkRequest.getHeader().setAppId(appid);
        listener.setSparkRequest(sparkRequest);

        SparkApiVersion apiVersion = sparkRequest.getApiVersion();
        String apiUrl = apiVersion.getUrl();

        // 构建鉴权url
        String authWsUrl = null;
        try {
            authWsUrl = AuthorizationUtils.getAuthUrl(apiUrl, apiSecret, apiKey).replace("http://", "ws://").replace("https://", "wss://");
        } catch (Exception e) {
            throw new SparkException(500, "构建鉴权url失败", e);
        }
        // 创建请求
        Request request = new Request.Builder().url(authWsUrl).build();
        // 发送请求
        client.newWebSocket(request, listener);
    }

    public SparkSyncChatResponse chatSync(SparkRequest sparkRequest) {
        SparkSyncChatResponse chatResponse = new SparkSyncChatResponse();
        SparkSyncChatListener syncChatListener = new SparkSyncChatListener(chatResponse);
        this.chatStream(sparkRequest, syncChatListener);
        while (!chatResponse.isOk()) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException ignored) {
            }
        }
        return chatResponse;
    }

}
