package com.yunxi.common.rpc.result;

import java.io.Serializable;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * 对外发布服务返回值基类，约束返回值类型<p>
 * 可以通过继承本类增加其他返回信息<p>
 * 
 * @author <a href="mailto:leukony@yeah.net">leukony</a>
 * @version $Id: Result.java, v 0.1 2016年12月26日 下午4:52:45 leukony Exp $
 */
public class Result<T> implements Serializable {

	private static final long serialVersionUID = 5404019836732161349L;

	/** 
	 * 本次服务是否正常处理，默认值为失败 <br>

	 * <ol>
     * <li>如果是，说明可以获得结果对象(结果可能也可能是null，比如没有查询到匹配值)</li>
     * <li>如果否，说明发生了异常，可以通过错误上下文了解错误原因</li>
     * <ol>
     * </p>
     */
    private boolean           success          = false;
    
    /** 错误信息，调用失败，服务方返回的错误信息 */
    private ResultError		  error;
    
    /** 服务提供者向服务消费者反馈的数据对象 */
    private T                 data;
    
    /**
     * 获取返回的错误编码
     * @return <code>error</code>如果为null，则返回null
     */
    public String getErrCode() {
    	return error == null ? null : error.getErrCode();
    }
    
    /**
     * 获取返回的错误信息
     * @return <code>error</code>如果为null，则返回null
     */
    public String getErrMsg() {
    	return error == null ? null : error.getErrMsg();
    }
    
    /**
     * Getter method for property <tt>success</tt>.
     * 
     * @return property value of success
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Setter method for property <tt>success</tt>.
     * 
     * @param success value to be assigned to property success
     */
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    /**
     * Getter method for property <tt>error</tt>.
     * 
     * @return property value of error
     */
    public ResultError getError() {
        return error;
    }

    /**
     * Setter method for property <tt>error</tt>.
     * 
     * @param error value to be assigned to property error
     */
    public void setError(ResultError error) {
        this.error = error;
    }
    
    /**
     * Getter method for property <tt>dataObject</tt>.
     * 
     * @return property value of dataObject
     */
    public T getData() {
        return data;
    }

    /**
     * Setter method for property <tt>data</tt>.
     * 
     * @param data value to be assigned to property data
     */
    public void setData(T data) {
        this.data = data;
    }

    /**
     * @see java.lang.Object#toString()
     * @see ToStringBuilder#reflectionToString(Object, ToStringStyle)
     * @see ToStringStyle#SHORT_PREFIX_STYLE
     */
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}