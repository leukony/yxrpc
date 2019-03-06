package com.yunxi.common.rpc.result;

import java.io.Serializable;

/**
 * 标准错误对象.
 * 
 * <p>
 * 标准错误对象包含:
 * <ul>
 * <li>标准错误码</li>
 * <li>错误默认文案</li>
 * </ul>
 * <p>
 * 
 * @author <a href="mailto:leukony@yeah.net">leukony</a>
 * @version $Id: ResultError.java, v 0.1 2016年12月26日 下午4:52:45 leukony Exp $
 */
public class ResultError implements Serializable {

	private static final long serialVersionUID = 7502635183841702455L;

	/** 错误编码 */
    private String            errCode;

    /** 错误描述 */
    private String            errMsg;

    /**
	 * 构造方法
	 */
    public ResultError() {
    }

	/**
	 * 构造方法
	 * 
	 * @param errCode
	 * @param errMsg
	 */
	public ResultError(String errCode, String errMsg) {
		this.errCode = errCode;
		this.errMsg = errMsg;
	}

	/**
	 * Getter method for property <tt>errCode</tt>.
	 * 
	 * @return property value of errCode
	 */
	public String getErrCode() {
		return errCode;
	}

	/**
	 * Setter method for property <tt>errCode</tt>.
	 * 
	 * @param errCode value to be assigned to property errCode
	 */
	public void setErrCode(String errCode) {
		this.errCode = errCode;
	}

	/**
	 * Getter method for property <tt>errMsg</tt>.
	 * 
	 * @return property value of errMsg
	 */
	public String getErrMsg() {
		return errMsg;
	}

	/**
	 * Setter method for property <tt>errMsg</tt>.
	 * 
	 * @param errMsg value to be assigned to property errMsg
	 */
	public void setErrMsg(String errMsg) {
		this.errMsg = errMsg;
	}
	
	/** 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return errCode + "::" + errMsg;
    }
}