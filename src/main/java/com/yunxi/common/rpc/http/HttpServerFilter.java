package com.yunxi.common.rpc.http;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import com.yunxi.common.lang.util.WebUtils;
import com.yunxi.common.tracer.TracerFactory;
import com.yunxi.common.tracer.constants.TracerConstants;
import com.yunxi.common.tracer.context.HttpContext;
import com.yunxi.common.tracer.tracer.HttpTracer;

/**
 * Http Server Tracer Filter
 * 
 * @author <a href="mailto:leukony@yeah.net">leukony</a>
 * @version $Id: HttpServerFilter.java, v 0.1 2017年1月12日 下午5:11:21 leukony Exp $
 */
public class HttpServerFilter implements Filter {

    /** 应用名 */
    private String    appName;

    /** 忽略的URL */
    private Pattern[] ignoreUri;

    /** 
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                         FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        // 1、判断是否为需要忽略的链接地址
        if (isIgnoreRequestUri(request.getRequestURI())) {
            chain.doFilter(request, response);
            return;
        }

        // 2、获取WEB请求中的Tracer参数
        String traceId = request.getHeader(TracerConstants.TRACE_ID);
        String rpcId = request.getHeader(TracerConstants.RPC_ID);

        Map<String, String> tracerContext = null;
        if (traceId != null && traceId.length() > 0 && rpcId != null && rpcId.length() > 0) {
            tracerContext = new HashMap<String, String>();
            tracerContext.put(TracerConstants.TRACE_ID, traceId);
            tracerContext.put(TracerConstants.RPC_ID, rpcId);
        }

        // 3、从工厂中获取HttpTracer
        HttpTracer httpTracer = TracerFactory.getHttpServerTracer();

        // 4、将请求中的Tracer参数设置到上下文中
        if (tracerContext != null) {
            httpTracer.setContext(tracerContext);
        }

        // 5、开始处理WEB请求,调用startProcess
        HttpContext httpContext = httpTracer.startProcess();

        // 6、获取WEB请求参数并设置到Tracer上下文中
        HttpServletRequest httpReq = request;
        httpContext.setUrl(WebUtils.getRequestURLWithParameters(httpReq));
        httpContext.setRequestIp(WebUtils.getRequestIP(httpReq));
        httpContext.setRequestSize(httpReq.getContentLength());
        httpContext.setMethod(httpReq.getMethod());
        httpContext.setCurrentApp(appName);

        EnhanceResponseWrapper wrapper = new EnhanceResponseWrapper(response);

        try {
            chain.doFilter(request, wrapper);
        } finally {
            httpContext.setResponseSize(wrapper.length);
            httpTracer.finishProcess(String.valueOf(wrapper.status));
        }
    }

    /**
     * 判断是否为需要忽略的链接地址
     * @param requestUri
     * @return
     */
    private boolean isIgnoreRequestUri(String requestUri) {
        if (ignoreUri == null || ignoreUri.length == 0) {
            return false;
        }

        for (Pattern pattern : ignoreUri) {
            Matcher matcher = pattern.matcher(requestUri);
            if (matcher != null && matcher.matches()) {
                return true;
            }
        }

        return false;
    }

    /** 
     * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
     */
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        this.appName = filterConfig.getInitParameter("appName");

        String excludeUrl = filterConfig.getInitParameter("excludeUrl");
        if (excludeUrl != null && excludeUrl.trim().length() > 0) {
            String[] excludeUrlArray = excludeUrl.split(";");
            this.ignoreUri = new Pattern[excludeUrlArray.length];
            for (int i = 0; i < excludeUrlArray.length; i++) {
                ignoreUri[i] = Pattern.compile(excludeUrlArray[i]);
            }
        }
    }

    /** 
     * @see javax.servlet.Filter#destroy()
     */
    @Override
    public void destroy() {
    }

    class EnhanceResponseWrapper extends HttpServletResponseWrapper {

        int length = 0;
        int status = 200;

        public EnhanceResponseWrapper(HttpServletResponse response) {
            super(response);
        }

        /**
         * @see javax.servlet.http.HttpServletResponseWrapper#setStatus(int)
         */
        @Override
        public void setStatus(int status) {
            this.status = status;
            super.setStatus(status);
        }

        /**
         * @see javax.servlet.ServletResponseWrapper#setContentLength(int)
         */
        @Override
        public void setContentLength(int contentLength) {
            this.length = contentLength;
            super.setContentLength(contentLength);
        }
    }
}