//package com.sunlei.util;
//
///**
// * @author sunlei
// */
//
//import org.apache.commons.io.FileUtils;
//import org.apache.commons.lang3.StringUtils;
//import org.apache.http.*;
//import org.apache.http.client.config.CookieSpecs;
//import org.apache.http.client.config.RequestConfig;
//import org.apache.http.client.entity.UrlEncodedFormEntity;
//import org.apache.http.client.methods.HttpGet; v
//import org.apache.http.client.methods.HttpPost;
//import org.apache.http.client.methods.HttpUriRequest;
//import org.apache.http.config.Registry;
//import org.apache.http.config.RegistryBuilder;
//import org.apache.http.conn.socket.ConnectionSocketFactory;
//import org.apache.http.conn.socket.PlainConnectionSocketFactory;
//import org.apache.http.conn.ssl.NoopHostnameVerifier;
//import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
//import org.apache.http.cookie.Cookie;
//import org.apache.http.entity.StringEntity;
//import org.apache.http.impl.client.*;
//import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
//import org.apache.http.message.BasicNameValuePair;
//import org.apache.http.ssl.SSLContextBuilder;
//import org.apache.http.ssl.TrustStrategy;
//import org.apache.http.util.EntityUtils;
//import org.jsoup.Jsoup;
//import org.jsoup.nodes.Document;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import javax.net.ssl.SSLContext;
//import java.io.File;
//import java.io.IOException;
//import java.nio.charset.Charset;
//import java.security.cert.CertificateException;
//import java.security.cert.X509Certificate;
//import java.util.*;
//
///**
// * 封装httpCilent请求,基于httpClient4.5.2开发
// * HTTP请求工具类
// */
//public class EasyHttpClient {
//    private PoolingHttpClientConnectionManager connectionManager;
//    private RequestConfig requestConfig;
//    private HttpClientBuilder builder;
//    private static final int MAX_TIMEOUT = 5 * 1000;
//    private CloseableHttpClient httpClient;
//    private BasicCookieStore cookieStore = new BasicCookieStore();
//    private static final Logger logger = LoggerFactory.getLogger(EasyHttpClient.class);
//
//    /**
//     * ----------主要是设置HttpClient的一些属性----------
//     */
//    public EasyHttpClient() {
//        //注册http和https
//        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
//                .register("http", PlainConnectionSocketFactory.INSTANCE)
//                .register("https", createSSLConnectionSocketFactory()).build();
//        //设置连接池
//        connectionManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
//        //设置连接池大小
//        connectionManager.setMaxTotal(100);
//        connectionManager.setDefaultMaxPerRoute(100);   //(同一个路由允许最大连接数)
//        RequestConfig.Builder configBuilder = RequestConfig.custom();
//        //设置连接超时
//        configBuilder.setConnectTimeout(MAX_TIMEOUT);
//        //设置读取超时
//        configBuilder.setSocketTimeout(MAX_TIMEOUT);
//        //设置从连接池获取连接实例的超时
//        configBuilder.setConnectionRequestTimeout(MAX_TIMEOUT);
//        //此处使用默认的Cookie规范，可以让后续请求共享Cookie
//        configBuilder.setCookieSpec(CookieSpecs.STANDARD_STRICT);
//        //支持重定向
//        configBuilder.setRedirectsEnabled(true);
//        requestConfig = configBuilder.build();
//        builder = HttpClients.custom()
//                .setUserAgent("Mozilla/5.0(Windows;U;Windows NT 5.1;en-US;rv:0.9.4)")
//                .setConnectionManager(connectionManager)
//                .setDefaultRequestConfig(requestConfig)
//                .setRedirectStrategy(new LaxRedirectStrategy())
//                .setDefaultCookieStore(cookieStore)
//                .setSSLSocketFactory(createSSLConnectionSocketFactory());           //设置证书
//        httpClient = builder.build();
//    }
//
//    public void setProxy(String proxy, Integer port) {
//        builder.setProxy(new HttpHost(proxy, port));           //设置走代理 方便Fiddler查看抓取请求
//        httpClient = builder.build();
//    }
//
//    /**
//     * Get方式请求,
//     *
//     * @param url
//     * @param charset
//     */
//    public String get(String url, String charset) {
//        return get(url, null, null, charset);
//    }
//
//    /**
//     * Get方式请求
//     *
//     * @param url
//     */
//    public String get(String url) {
//        return get(url, null, null, null);
//    }
//
//    /**
//     * Get方式请求
//     *
//     * @param url
//     * @param map
//     */
//    public String get(String url, Map<String, String> map) {
//        return get(url, map, null, null);
//    }
//
//    /**
//     * Get方式请求
//     *
//     * @param url
//     * @param map
//     * @param headers
//     */
//    public String get(String url, Map<String, String> map, Map<String, String> headers) {
//        return get(url, map, headers, null);
//    }
//
//    /**
//     * Get方式请求
//     *
//     * @param url
//     * @param map     请求参数
//     * @param headers
//     */
//    public String get(String url, Map<String, String> map, Map<String, String> headers, String charset) {
//        String html = null;
//        StringBuffer buffer = new StringBuffer(url);
//        /**----------设置请求参数------------*/
//        if (map != null && map.size() != 0) {
//            for (String mapName : map.keySet()) {
//                if (buffer.toString().equals(url)) {
//                    buffer.append("?");
//                } else {
//                    buffer.append("&");
//                }
//                buffer.append(mapName).append("=").append(map.get(mapName).toString());
//            }
//        }
//        HttpGet get = new HttpGet(buffer.toString());
//        /**----------设置请求头信息-----------*/
//        setHeaders(get, headers);
//        try {
//            logger.info("GET URL--" + buffer.toString());
//            HttpResponse httpResponse = httpClient.execute(get);
//            HttpEntity entity = httpResponse.getEntity();
//            if (null != entity) {
//                if (StringUtils.isNotBlank(charset)) {
//                    html = EntityUtils.toString(entity, charset);
//                } else {
//                    html = EntityUtils.toString(entity, "utf-8");
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            logger.error("GET请求失败,{}", e);
//        }
//        return html;
//    }
//
//    /**
//     * Post方式请求
//     *
//     * @param url
//     * @param map
//     * @param charset
//     */
//    public String postMap(String url, Map<String, String> map, String charset) {
//        return postMap(url, map, null, charset);
//    }
//
//    /**
//     * Post方式请求
//     *
//     * @param url
//     * @param map
//     */
//    public String postMap(String url, Map<String, String> map) {
//        return postMap(url, map, null, null);
//    }
//
//    /**
//     * Post方式请求
//     *
//     * @param url
//     * @param map
//     * @param headers
//     */
//    public String postMap(String url, Map<String, String> map, Map<String, String> headers) {
//        return postMap(url, map, headers, null);
//    }
//
//    /**
//     * Post方式请求
//     *
//     * @param url
//     * @param map
//     * @param headers
//     * @param charset
//     */
//    public String postMap(String url, Map<String, String> map, Map<String, String> headers, String charset) {
//        String html = null;
//        String apiUrl = url;
//        try {
//            HttpPost post = new HttpPost(apiUrl);
//            if (map != null && map.size() != 0) {
//                List<NameValuePair> list = new ArrayList<NameValuePair>(map.size());
//                /**----------设置请求参数------------*/
//                for (Map.Entry<String, String> entry : map.entrySet()) {
//                    NameValuePair pair = new BasicNameValuePair(entry.getKey(), entry.getValue());
//                    list.add(pair);
//                }
//                post.setEntity(new UrlEncodedFormEntity(list, Charset.forName("utf-8")));
//            }
//            /**----------设置请求头信息----------*/
//            setHeaders(post, headers);
//            logger.info("POST--MAP URL--" + url);
//            HttpResponse httpResponse = httpClient.execute(post);
//            HttpEntity entity = httpResponse.getEntity();
//            if (null != entity) {
//                if (StringUtils.isNotBlank(charset)) {
//                    html = EntityUtils.toString(entity, charset);
//                } else {
//                    html = EntityUtils.toString(entity, "utf-8");
//                }
//            }
//        } catch (Exception e) {
//            logger.error("POST-MAP请求失败,{}", e);
//        }
//        return html;
//    }
//
//    /**
//     * Post方式请求
//     *
//     * @param url
//     * @param json
//     * @param json
//     * @param charset
//     */
//    public String postJson(String url, String json, String charset) {
//        return postJson(url, json, null, charset);
//    }
//
//    /**
//     * Post方式请求
//     *
//     * @param url
//     * @param json
//     * @param json
//     */
//    public String postJson(String url, String json) {
//        return postJson(url, json, null, null);
//    }
//
//    /**
//     * Post方式请求
//     *
//     * @param url
//     * @param json
//     * @param json
//     */
//    public String postJson(String url, String json, Map<String, String> headers, String charset) {
//        String html = null;
//        try {
//            HttpPost post = new HttpPost(url);
//            setHeaders(post, headers);
//            if (null != json) {
//                StringEntity stringEntity = new StringEntity(json.toString(), "utf-8");
//                stringEntity.setContentType("application/json");
//                stringEntity.setContentEncoding("utf-8");
//                post.setEntity(stringEntity);
//            }
//            logger.info("POST--JSON URL--" + url);
//            HttpResponse httpResponse = httpClient.execute(post);
//            HttpEntity entity = httpResponse.getEntity();
//            if (null != entity) {
//                if (StringUtils.isNotBlank(charset)) {
//                    html = EntityUtils.toString(entity, charset);
//                } else {
//                    html = EntityUtils.toString(entity, "utf-8");
//                }
//            }
//        } catch (Exception e) {
//            logger.error("POST-JSON请求失败,{}", e);
//        }
//        return html;
//    }
//
//    /**
//     * 通过传入参数设置header
//     *
//     * @param request 代表http请求
//     * @param headers 包含请求头的Map对象
//     */
//    private void setHeaders(HttpUriRequest request, Map<String, String> headers) {
//        if (null != headers && headers.size() != 0) {
//            Set<Map.Entry<String, String>> entries = headers.entrySet();
//            for (Map.Entry<String, String> entry : entries) {
//                request.setHeader(entry.getKey(), entry.getValue());
//            }
//        }
//    }
//
//    /**
//     * 请求图片
//     * return byte[]
//     *
//     * @param imgUrl
//     */
//    public byte[] getImg(String imgUrl) {
//        return getImg(imgUrl, null);
//    }
//
//    /**
//     * 请求图片
//     *
//     * @param imgUrl
//     * @param headers
//     */
//    public byte[] getImg(String imgUrl, Map<String, String> headers) {
//        byte[] data = null;
//        try {
//            HttpGet get = new HttpGet(imgUrl);
//            /**----------设置请求头信息-----------*/
//            if (headers != null && headers.size() != 0) {
//                setHeaders(get, headers);
//            }
//            logger.info("GET--IMAGE URL--" + imgUrl);
//            HttpResponse response = httpClient.execute(get);
//            HttpEntity entity = response.getEntity();
//            if (null != entity) {
//                data = EntityUtils.toByteArray(entity);
//            }
//        } catch (Exception e) {
//            logger.error("GET-IMAGE请求失败,{}", e);
//        }
//        return data;
//    }
//
//    /**
//     * 关闭client请求
//     */
//    public String getCookie() {
//        StringBuffer buffer = new StringBuffer();
//        try {
//            List<Cookie> cookies = cookieStore.getCookies();
//            for (Cookie cookie : cookies) {
//                buffer.append(cookie.getName() + "=" + cookie.getValue() + ";");
//            }
//        } catch (Exception e) {
//            logger.error("COOKIE请求失败,{}", e);
//        }
//        return buffer.toString();
//    }
//
//    /**
//     * 关闭client请求
//     */
//    public void close() {
//        try {
//            httpClient.close();
//        } catch (IOException e) {
//            logger.error("CLOSE请求失败,{}", e);
//        }
//    }
//
//    /**
//     * 创建SSL连接
//     * 解决https问题
//     */
//    public static SSLConnectionSocketFactory createSSLConnectionSocketFactory() {
//        SSLConnectionSocketFactory factory = null;
//        try {
//            SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
//                public boolean isTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
//                    return true;
//                }
//            }).build();
//            factory = new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return factory;
//    }
//
//    public static void main(String[] args) throws InterruptedException, IOException {
//        EasyHttpClient client = new EasyHttpClient();
//        String html = client.get("https://www.qunar.com/?tab=hotel&ex_track=auto_4e0d874a");
//        System.out.println(html);
//    }
//}
