/**
 * Copyright 2020 OPSLI 快速开发平台 https://www.opsli.com
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.opsli.limiter.core.ret;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.http.HttpStatus;

import java.io.Serializable;

/**
 * API 统一返回参数
 *
 * @date 2020年5月15日10:40:54
 * @author Parker
 *
 * 在 Feign 的调用过程中，无法直接序列化数据
 *
 * 所以要加上 泛型对象 @JsonProperty ，否者返回则为一个null
 *
 */
public class ResultVo<T> implements Serializable {

	private static final long serialVersionUID = 1L;

	/** 成功状态 */
	@JsonProperty("success")
	private boolean success;

	/** 消息 */
	@JsonProperty("msg")
	private String msg;

	/** 状态码 */
	@JsonProperty("code")
	private Integer code;

	/** 时间戳 */
	@JsonProperty("timestamp")
	private Long timestamp;

	/** 数据对象 */
	@JsonProperty("data")
	private T data;

	public T getData() {
		return data;
	}

	public ResultVo<T> setData(T data) {
		this.data = data;
		return this;
	}


	// ===========================================

	/**
	 * 构造函数
	 */
	public ResultVo() {
		// 初始化值
		this.success = true;
		this.msg = "操作成功！";
		this.code = HttpStatus.OK.value();
		this.timestamp = System.currentTimeMillis();
	}

	// ================================== 静态方法 ===================================

	/**
	 * 返回成功状态
	 * @return ResultVo<Object>
	 */
	@JsonIgnore//返回对象时忽略此属性
	public static ResultVo<Object> success() {
		return new ResultVo<>();
	}

	/**
	 * 返回成功状态
	 * @param msg 返回信息
	 * @return ResultVo<Object>
	 */
	@JsonIgnore//返回对象时忽略此属性
	public static ResultVo<Object> success(String msg) {
		ResultVo<Object> ret = new ResultVo<>();
		ret.setMsg(msg);
		return ret;
	}

	/**
	 * 返回成功状态
	 * @param data 返回数据
	 * @param <T> 泛型
	 * @return ResultVo<T>
	 */
	@JsonIgnore//返回对象时忽略此属性
	public static <T> ResultVo<T> success(T data) {
		ResultVo<T> ret = new ResultVo<>();
		ret.setData(data);
		return ret;
	}

	/**
	 * 返回成功状态
	 * @param msg 返回信息
	 * @param data 返回数据
	 * @param <T> 泛型
	 * @return ResultVo<T>
	 */
	@JsonIgnore//返回对象时忽略此属性
	public static <T> ResultVo<T> success(String msg, T data) {
		ResultVo<T> ret = new ResultVo<>();
		ret.setData(data);
		return ret;
	}


	/**
	 * 返回错误状态
	 * @param msg 返回信息
	 * @return ResultVo<Object>
	 */
	@JsonIgnore//返回对象时忽略此属性
	public static <T> ResultVo<T> error(String msg) {
		ResultVo<T> ret = new ResultVo<>();
		ret.setMsg(msg);
		ret.setCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		ret.setSuccess(false);
		return ret;
	}

	/**
	 * 返回错误状态
	 * @param code 错误编号
	 * @param msg 返回信息
	 * @return ResultVo<T>
	 */
	@JsonIgnore//返回对象时忽略此属性
	public static <T> ResultVo<T> error(int code, String msg) {
		ResultVo<T> ret = new ResultVo<>();
		ret.setMsg(msg);
		ret.setCode(code);
		ret.setSuccess(false);
		return ret;
	}

	/**
	 * 返回成功状态
	 * @param code 错误编号
	 * @param data 返回数据
	 * @param <T> 泛型
	 * @return ResultVo<T>
	 */
	@JsonIgnore//返回对象时忽略此属性
	public static <T> ResultVo<T> error(int code, String msg, T data) {
		ResultVo<T> ret = new ResultVo<>();
		ret.setMsg(msg);
		ret.setCode(code);
		ret.setData(data);
		ret.setSuccess(false);
		return ret;
	}


	// =============


	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public Integer getCode() {
		return code;
	}

	public void setCode(Integer code) {
		this.code = code;
	}

	public Long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}
}
