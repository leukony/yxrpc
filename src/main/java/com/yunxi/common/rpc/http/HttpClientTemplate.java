package com.yunxi.common.rpc.http;

import static com.yunxi.common.tracer.constants.TracerConstants.RPC_ID;
import static com.yunxi.common.tracer.constants.TracerConstants.TRACE_ID;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.EntityEnclosingMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;

import com.yunxi.common.tracer.TracerFactory;
import com.yunxi.common.tracer.context.HttpContext;
import com.yunxi.common.tracer.tracer.HttpTracer;

/**
 * HttpClient模板
 * 
 * <p>
 * 基于Apache Common HttpClient:
 * <ul>
 * <li>设置默认超时参数</li>
 * <li>执行Http请求</li>
 * <li>断开连接</li>
 * </ul>
 * <p>
 * 
 * @author <a href="mailto:leukony@yeah.net">leukony</a>
 * @version $Id: HttpClientTemplate.java, v 0.1 2017年2月22日 上午10:38:08 leukony Exp $
 */
public class HttpClientTemplate {

    /** HttpClient */
    private HttpClient httpClient;

    /**
     * 以Get方式执行http请求
     * @param url
     * @return
     * @throws IOException
     * @throws HttpException
     */
    public HttpClientResponse<String> executeGet(String url) throws IOException, HttpException {
        return executeGet(url, new HttpClientCallback<String>() {

            @Override
            protected String doConvert(HttpMethod httpMethod) throws IOException {
                return getResponseBody(httpMethod);
            }

        });
    }

    /**
     * 以Get方式执行http请求
     * @param url
     * @param callback
     * @return
     * @throws IOException
     * @throws HttpException
     */
    public <T> HttpClientResponse<T> executeGet(String url, HttpClientCallback<T> callback)
                                                                                           throws IOException,
                                                                                           HttpException {
        return execute(new GetMethod(url), callback);
    }

    /**
     * 以Post方式执行http请求
     * @param url
     * @param headParams
     * @param reqParams
     * @return
     * @throws IOException
     * @throws HttpException
     */
    public HttpClientResponse<String> executePost(String url, Map<String, String> headParams,
                                                  Map<String, String> reqParams)
                                                                                throws IOException,
                                                                                HttpException {
        return executePost(url, headParams, reqParams, new HttpClientCallback<String>() {

            @Override
            protected String doConvert(HttpMethod httpMethod) throws IOException {
                return getResponseBody(httpMethod);
            }

        });
    }

    /**
     * 以Post方式执行http请求
     * @param url
     * @param headParams
     * @param reqParams
     * @param callback
     * @return
     * @throws IOException
     * @throws HttpException
     */
    public <T> HttpClientResponse<T> executePost(String url, Map<String, String> headParams,
                                                 Map<String, String> reqParams,
                                                 HttpClientCallback<T> callback)
                                                                                throws IOException,
                                                                                HttpException {
        PostMethod postMethod = new PostMethod(url);

        if (headParams != null && headParams.size() > 0) {
            for (Entry<String, String> header : headParams.entrySet()) {
                postMethod.addRequestHeader(header.getKey(), header.getValue());
            }
        }

        if (reqParams != null && reqParams.size() > 0) {
            for (Entry<String, String> req : reqParams.entrySet()) {
                postMethod.addParameter(req.getKey(), req.getValue());
            }
        }

        return execute(postMethod, callback);
    }

    /**
     * 使用HttpClientCallback处理HttpClient的响应
     * @param httpMethod
     * @param callback
     * @return
     * @throws IOException
     * @throws HttpException
     */
    public <T> HttpClientResponse<T> execute(HttpMethod httpMethod, HttpClientCallback<T> callback)
                                                                                                   throws IOException,
                                                                                                   HttpException {
        String resultCode = "";
        HttpTracer httpTracer = null;

        try {
            // 1、从工厂获取HttpTracer
            httpTracer = TracerFactory.getHttpClientTracer();

            // 2、开始Http请求,调用startInvoke
            HttpContext httpContext = httpTracer.startInvoke();

            // 3、将上下文中Tracer参数设置到请求头
            if (httpContext != null) {
                httpMethod.setRequestHeader(TRACE_ID, httpContext.getTraceId());
                httpMethod.setRequestHeader(RPC_ID, httpContext.getRpcId());

                httpContext.setUrl(httpMethod.getURI().getURI());
                httpContext.setMethod(httpMethod.getName());
                httpContext.setCurrentApp(appName);

                if (httpMethod instanceof EntityEnclosingMethod) {
                    RequestEntity requestEntity = ((EntityEnclosingMethod) httpMethod)
                        .getRequestEntity();
                    if (requestEntity != null) {
                        httpContext.setRequestSize(requestEntity.getContentLength());
                    }
                }
            }

            // 4、开始Http请求,调用executeMethod
            int httpCode = httpClient.executeMethod(httpMethod);

            // 5、将Http请求结果设置到上下文中
            if (httpContext != null) {
                resultCode = String.valueOf(httpCode);
                if (httpMethod instanceof HttpMethodBase) {
                    HttpMethodBase httpMethodBase = (HttpMethodBase) httpMethod;
                    httpContext.setResponseSize(httpMethodBase.getResponseContentLength());
                }
            }

            // 6、调用CallBack处理Http请求返回结果
            return callback.process(httpMethod);
        } finally {
            // 7、结束Http请求调用，打印Trace日志
            if (httpTracer != null) {
                httpTracer.finishInvoke(resultCode, HttpContext.class);
            }

            // 8、释放Http请求链接
            httpMethod.releaseConnection();
        }
    }
    
    /**
     * 转化返回结果
     * @param httpMethod
     * @return
     * @throws IOException 
     */
    private String getResponseBody(HttpMethod httpMethod) throws IOException {
        InputStream is = httpMethod.getResponseBodyAsStream();
        Reader r = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(r);
        String result = "";
        StringBuffer sb = new StringBuffer();  
        while ((result = br.readLine())!= null) {  
            sb.append(result); 
        }
        return sb.toString();
    }

    /** 应用名 */
    private String appName;

    /** 默认连接数 */
    private int    maxConnPerHost           = 6;

    /** 最大连接数 */
    private int    maxTotalConn             = 10;

    /** 默认等待数据返回超时，单位:毫秒*/
    private int    soTimeout                = 10000;

    /** 默认等待连接建立超时，单位:毫秒*/
    private int    connectionTimeout        = 1000;

    /** 默认请求连接池连接超时,单位:毫秒*/
    private int    connectionManagerTimeout = 1000;
    
    /** 默认请求的编码 */
    private String contentCharset           = "UTF-8";

    /** 代理Host */
    private String proxyHost;

    /** 代理端口 */
    private int    proxyPort;

    /** 代理用户名 */
    private String proxyUserName;

    /** 代理密码 */
    private String proxyUserPassword;

    /**
     * 初始化HttpClient
     * @param params
     */
    public void initialize() {
        HttpConnectionManagerParams httpConnectionManagerParams = new HttpConnectionManagerParams();
        HttpConnectionManager httpConnectionManager = new MultiThreadedHttpConnectionManager();
        httpConnectionManagerParams.setDefaultMaxConnectionsPerHost(maxConnPerHost);
        httpConnectionManagerParams.setConnectionTimeout(connectionTimeout);
        httpConnectionManagerParams.setMaxTotalConnections(maxTotalConn);
        httpConnectionManagerParams.setSoTimeout(soTimeout);
        httpConnectionManager.setParams(httpConnectionManagerParams);
        httpClient = new HttpClient(httpConnectionManager);
        httpClient.getParams().setConnectionManagerTimeout(connectionManagerTimeout);
        httpClient.getParams().setContentCharset(contentCharset);
        if (proxyHost != null) {
            httpClient.getHostConfiguration().setProxy(proxyHost, proxyPort);
            if (proxyUserName != null) {
                httpClient.getState().setProxyCredentials(AuthScope.ANY,
                    new UsernamePasswordCredentials(proxyUserName, proxyUserPassword));
            }
        }
    }

    /**
      * Setter method for property <tt>httpClient</tt>.
      * 
      * @param httpClient value to be assigned to property httpClient
      */
    public void setHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    /**
      * Setter method for property <tt>appName</tt>.
      * 
      * @param appName value to be assigned to property appName
      */
    public void setAppName(String appName) {
        this.appName = appName;
    }

    /**
      * Setter method for property <tt>maxConnPerHost</tt>.
      * 
      * @param maxConnPerHost value to be assigned to property maxConnPerHost
      */
    public void setMaxConnPerHost(int maxConnPerHost) {
        this.maxConnPerHost = maxConnPerHost;
    }

    /**
      * Setter method for property <tt>maxTotalConn</tt>.
      * 
      * @param maxTotalConn value to be assigned to property maxTotalConn
      */
    public void setMaxTotalConn(int maxTotalConn) {
        this.maxTotalConn = maxTotalConn;
    }

    /**
      * Setter method for property <tt>soTimeout</tt>.
      * 
      * @param soTimeout value to be assigned to property soTimeout
      */
    public void setSoTimeout(int soTimeout) {
        this.soTimeout = soTimeout;
    }

    /**
      * Setter method for property <tt>connectionTimeout</tt>.
      * 
      * @param connectionTimeout value to be assigned to property connectionTimeout
      */
    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    /**
      * Setter method for property <tt>connectionManagerTimeout</tt>.
      * 
      * @param connectionManagerTimeout value to be assigned to property connectionManagerTimeout
      */
    public void setConnectionManagerTimeout(int connectionManagerTimeout) {
        this.connectionManagerTimeout = connectionManagerTimeout;
    }

    /**
      * Setter method for property <tt>contentCharset</tt>.
      * 
      * @param contentCharset value to be assigned to property contentCharset
      */
    public void setContentCharset(String contentCharset) {
        this.contentCharset = contentCharset;
    }

    /**
      * Setter method for property <tt>proxyHost</tt>.
      * 
      * @param proxyHost value to be assigned to property proxyHost
      */
    public void setProxyHost(String proxyHost) {
        this.proxyHost = proxyHost;
    }

    /**
      * Setter method for property <tt>proxyPort</tt>.
      * 
      * @param proxyPort value to be assigned to property proxyPort
      */
    public void setProxyPort(int proxyPort) {
        this.proxyPort = proxyPort;
    }

    /**
      * Setter method for property <tt>proxyUserName</tt>.
      * 
      * @param proxyUserName value to be assigned to property proxyUserName
      */
    public void setProxyUserName(String proxyUserName) {
        this.proxyUserName = proxyUserName;
    }

    /**
      * Setter method for property <tt>proxyUserPassword</tt>.
      * 
      * @param proxyUserPassword value to be assigned to property proxyUserPassword
      */
    public void setProxyUserPassword(String proxyUserPassword) {
        this.proxyUserPassword = proxyUserPassword;
    }
}