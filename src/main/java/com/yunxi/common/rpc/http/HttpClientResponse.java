package com.yunxi.common.rpc.http;

/**
 * HttpClient响应结果
 * 
 * @author <a href="mailto:leukony@yeah.net">leukony</a>
 * @version $Id: HttpClientResponse.java, v 0.1 2017年1月12日 下午7:06:12 leukony Exp $
 */
public class HttpClientResponse<T> {

    /** 响应结果码 */
    private int code;

    /** 响应结果体 */
    private T   body;

    /**
     * @param code
     * @param body
     */
    public HttpClientResponse(int code, T body) {
        this.code = code;
        this.body = body;
    }

    /**
      * Getter method for property <tt>code</tt>.
      * 
      * @return property value of code
      */
    public int getCode() {
        return code;
    }

    /**
      * Setter method for property <tt>code</tt>.
      * 
      * @param code value to be assigned to property code
      */
    public void setCode(int code) {
        this.code = code;
    }

    /**
      * Getter method for property <tt>body</tt>.
      * 
      * @return property value of body
      */
    public T getBody() {
        return body;
    }

    /**
      * Setter method for property <tt>body</tt>.
      * 
      * @param body value to be assigned to property body
      */
    public void setBody(T body) {
        this.body = body;
    }
}