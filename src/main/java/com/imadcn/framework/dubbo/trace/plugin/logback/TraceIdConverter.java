package com.imadcn.framework.dubbo.trace.plugin.logback;

import com.imadcn.framework.dubbo.common.Constant;
import com.imadcn.framework.dubbo.trace.plugin.mdc.TraceIdGenerator;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

/**
 * 利用MDC来打印当前请求的唯一序列号
 * @author yangc
 * @since 1.1.0
 */
public class TraceIdConverter extends ClassicConverter {
	
	@Override
	public String convert(ILoggingEvent event) {
		String traceId = null;
		if (event.getMDCPropertyMap() != null) {
			traceId = event.getMDCPropertyMap().get(Constant.TRACE_ID);
		} 
		if (traceId == null || traceId.isEmpty()) {
			traceId = TraceIdGenerator.getTraceId();
		}
		return traceId;
	}

	/**
	 * 清除 traceId
	 */
	public static void clear() {
		TraceIdGenerator.clearTraceId();
	}
}
