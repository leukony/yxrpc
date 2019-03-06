package com.yunxi.common.rpc.result;

/**
 * 服务调用结果组装
 * 
 * @author <a href="mailto:leukony@yeah.net">leukony</a>
 * @version $Id: ResultWrapper.java, v 0.1 2016年12月26日 下午4:52:45 leukony Exp $
 */
public class ResultWrapper {
	
	/**
     * 构造成功结果
     * 操作如下方法:<br>
     * <ol>
     * <li>{@link Result#setSuccess(boolean)}设置为<code>true</code></li>
     * <li>{@link Result#setData(Object)}设置为<code>model</code></li>
     * </ol>
     * @param model 如果为null，{@link Result#getData()}获取到的也是null
     */
    public static <T extends Object> Result<T> success(T model) {
    	Result<T> result = new Result<T>();
        result.setSuccess(true);
        result.setData(model);
        return result;
    }
    
    /**
     * 构造失败结果
     * <P>
     * 操作如下方法:<br>
     * <ol>
     * <li>{@link Result#setSuccess(boolean)}设置为<code>false</code></li>
     * <li>{@link Result#setError(ResultError)}设置为<code>errCode</code></li>
     * </ol>
     * @param errCode 错误编码信息，不能为null
     * @param errMsg 错误描述信息,比如校验异常提示具体是那些校验未通过，可以为空
     * @throws IllegalArgumentException 如果参数<code>errCode</code>为<code>null</code>抛出该异常
     */
    public static <T extends Object> Result<T> failure(String errCode, String errMsg) {
    	if (errCode == null || errCode.trim().length() == 0) {
    		throw new IllegalArgumentException("errCode is null");
    	}
        Result<T> result = new Result<T>();
        result.setSuccess(false);
        result.setError(new ResultError(errCode, errMsg));
        return result;
    }
}