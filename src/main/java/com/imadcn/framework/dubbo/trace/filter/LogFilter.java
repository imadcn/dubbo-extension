/*
 * Copyright 2013-2018 imadcn Group.
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.imadcn.framework.dubbo.trace.filter;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.imadcn.framework.common.time.DateFormatUtil;
import com.imadcn.framework.dubbo.common.Constant;
import com.imadcn.framework.dubbo.trace.plugin.mdc.TraceIdGenerator;

/**
 * 日志跟踪
 * @author yangc
 * @since 1.1.0
 */
public abstract class LogFilter {

	protected final Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * 最大日志显示长度
	 */
	private int maxLogLength = Constant.DEFAULT_LOG_LENGTH;

	/**
	 * 最大日志显示长度
	 * @return 最大日志显示长度
	 */
	public int getMaxLogLength() {
		return maxLogLength;
	}

	/**
	 * 最大日志显示长度 
	 * @param maxLogLength 最大日志显示长度
	 */
	public void setMaxLogLength(int maxLogLength) {
		this.maxLogLength = maxLogLength;
	}
	
	/**
	 * 设置 链路跟踪Id
	 * @param traceId 链路跟踪Id
	 */
	public void setTraceId(String traceId) {
		TraceIdGenerator.setTraceId(traceId);
	}
	
	/**
	 * 获取 链路跟踪Id 
	 * @param attachments Dubbo Attachment
	 * @return 链路跟踪Id
	 */
	public String getTraceId(Map<String, String> attachments) {
		String traceId = attachments.get(Constant.TRACE_ID);
		if (traceId == null || traceId.isEmpty()) {
			traceId = TraceIdGenerator.getTraceId();
		}
		return traceId;
	}
	
	/**
	 * 获取 链路跟踪Id
	 * @param attachments Dubbo Attachment
	 * @return 链路跟踪Id
	 */
	public String getAndSetTraceId(Map<String, String> attachments) {
		String traceId = attachments.get(Constant.TRACE_ID);
		if (traceId != null && !traceId.isEmpty()) {
			setTraceId(traceId);
		}
		return traceId;
	}
	
	/**
	 * 清除 链路跟踪Id
	 */
	public void clearTraceId() {
		TraceIdGenerator.clearTraceId();
	}
	
	/**
	 * 获取JSON格式字符串
	 * @param object 待转换对象
	 * @return JSON格式字符串
	 */
	protected String getJSONString(Object object) {
		return JSON.toJSONStringWithDateFormat(object, DateFormatUtil.FULL_TIME, SerializerFeature.DisableCircularReferenceDetect);
	}
}
