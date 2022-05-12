package com.github.mxsm.rain.uid.client;


import com.github.mxsm.rain.uid.client.exception.ClientHttpRequestException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.Message;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.impl.bootstrap.HttpAsyncRequester;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.apache.hc.core5.http.nio.AsyncClientEndpoint;
import org.apache.hc.core5.http.nio.entity.StringAsyncEntityConsumer;
import org.apache.hc.core5.http.nio.support.AsyncRequestBuilder;
import org.apache.hc.core5.http.nio.support.BasicResponseConsumer;
import org.apache.hc.core5.http2.HttpVersionPolicy;
import org.apache.hc.core5.http2.config.H2Config;
import org.apache.hc.core5.http2.impl.nio.bootstrap.H2RequesterBootstrap;
import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.reactor.IOReactorConfig;
import org.apache.hc.core5.util.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mxsm
 * @date 2022/5/5 8:14
 * @Since 1.0.0
 */
public class Http2Requester {

    private static final Logger LOGGER = LoggerFactory.getLogger(Http2Requester.class);

    private final static Http2Requester H2REQUESTER = new Http2Requester();

    private final IOReactorConfig ioReactorConfig;

    private final H2Config h2Config;

    private final HttpAsyncRequester requester;

    private Http2Requester() {
        this.ioReactorConfig = IOReactorConfig.custom()
            .setSoTimeout(5, TimeUnit.SECONDS)
            .setSoKeepAlive(true)
            .setRcvBufSize(4 * 1024)
            .setSndBufSize(4 * 1024)
            .build();

        this.h2Config = H2Config.custom()
            .setPushEnabled(false)
            .setMaxConcurrentStreams(100)
            .build();

        this.requester = H2RequesterBootstrap.bootstrap()
            .setIOReactorConfig(ioReactorConfig)
            .setVersionPolicy(HttpVersionPolicy.FORCE_HTTP_2)
            .setH2Config(h2Config).create();
        initAndStart();
    }

    private void initAndStart() {

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                LOGGER.info("HTTP requester shutting down");
                requester.close(CloseMode.GRACEFUL);
            }
        });
        requester.start();
    }

    public static String executeGET(String host, int port, String path)
        throws ExecutionException, InterruptedException {
        return executeGET(host, port, path, null);
    }

    public static String executeGET(String host, int port, String path, Map<String, String> params)
        throws ExecutionException, InterruptedException {

        NameValuePair[] pairs = null;
        if (params != null && !params.isEmpty()) {
            int size = params.size();
            pairs = new NameValuePair[size];
            int index = 0;
            Set<Entry<String, String>> entries = params.entrySet();
            for (Entry<String, String> entry : entries) {
                pairs[index++] = new BasicNameValuePair(entry.getKey(), entry.getValue());
            }
        }
        return H2REQUESTER.http2Get(host, port, path, pairs);
    }

    private String http2Get(String host, int port, String path, NameValuePair... params)
        throws ExecutionException, InterruptedException {
        Future<Message<HttpResponse, String>> execute = getMessageFuture(host, port, path, params);
        Message<HttpResponse, String> httpResponseStringMessage = execute.get();
        int code = httpResponseStringMessage.getHead().getCode();
        if (code != 200) {
            throw new ClientHttpRequestException(
                "HTTP Code:" + code + ", Message:" + httpResponseStringMessage.getBody());
        }
        return httpResponseStringMessage.getBody();
    }

    private Future<Message<HttpResponse, String>> getMessageFuture(String host, int port, String path,
        NameValuePair[] params) throws InterruptedException, ExecutionException {
        final HttpHost target = new HttpHost(host, port);
        final Future<AsyncClientEndpoint> future = requester.connect(target, Timeout.ofSeconds(5));
        final AsyncClientEndpoint clientEndpoint = future.get();
        AsyncRequestBuilder asyncRequestBuilder = AsyncRequestBuilder.get()
            .setHttpHost(target)
            .setPath(path);
        if (params != null && params.length != 0) {
            asyncRequestBuilder.addParameters(params);
        }
        Future<Message<HttpResponse, String>> execute = clientEndpoint.execute(
            asyncRequestBuilder.build(),
            new BasicResponseConsumer<>(new StringAsyncEntityConsumer()),
            new FutureCallback<>() {
                @Override
                public void completed(final Message<HttpResponse, String> message) {
                    clientEndpoint.releaseAndReuse();
                }

                @Override
                public void failed(final Exception ex) {
                    clientEndpoint.releaseAndDiscard();
                }

                @Override
                public void cancelled() {
                    clientEndpoint.releaseAndDiscard();
                }
            });
        return execute;
    }
}
