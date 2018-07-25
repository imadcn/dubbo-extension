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
package com.imadcn.framework.dubbo.trace.plugin.mdc;

import java.util.UUID;

import org.slf4j.MDC;

import com.imadcn.framework.dubbo.common.Constant;

/**
 * 消息ID生成工具
 * @author yangc
 * @since 1.1.0
 */
public class TraceIdGenerator {

	/**
	 * 通过 MDC来获取链路跟踪ID
	 * @return 链路跟踪ID
	 */
	public static String getTraceId() {
		String traceId = MDC.get(Constant.TRACE_ID);
		if (traceId == null || traceId.isEmpty()) {
			traceId = genTraceId();
		}
		return traceId;
	}
	
	/**
	 * 存储外部TraceId
	 * @param traceId
	 */
	public static void setTraceId(String traceId) {
		if (traceId == null || traceId.isEmpty()) {
			traceId = genTraceId();
		} else {
			MDC.put(Constant.TRACE_ID, traceId);
		}
	}
	
	/**
	 * 清除 链路跟踪ID
	 */
	public static void clearTraceId() {
		MDC.remove(Constant.TRACE_ID);
	}
	
	private static String genTraceId() {
		String traceId = UUID.randomUUID().toString().replaceAll("-", "");
		MDC.put(Constant.TRACE_ID, traceId);
		return traceId;
	}
}
