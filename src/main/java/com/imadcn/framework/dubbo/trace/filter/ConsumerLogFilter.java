package com.imadcn.framework.dubbo.trace.filter;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.rpc.Filter;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.RpcInvocation;
import com.imadcn.framework.common.time.DateFormatUtil;
import com.imadcn.framework.dubbo.common.Constant;

/**
 * 消费者日志拦截器
 * @author yangchao
 * @since 1.0.0
 */
@Activate(group = Constants.CONSUMER)
public class ConsumerLogFilter extends LogFilter implements Filter {

	@Override
	public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
		long startTime = System.currentTimeMillis();
		
		if (invocation instanceof RpcInvocation) {
			((RpcInvocation) invocation).setInvoker(invoker);
		}
		
		Map<String, String> attachments = RpcContext.getContext().getAttachments();
		String traceId = getTraceId(attachments);
		attachments.put(Constant.TRACE_ID, traceId);
		
		Object obj = null;
		try {
			obj = invoker.invoke(invocation);
			return (Result) obj;
		} catch (Throwable t) {
			if (t instanceof InvocationTargetException) {
				InvocationTargetException ite = (InvocationTargetException) t;
				Throwable e = ite.getTargetException();
				obj = e.getClass().getCanonicalName() + ":" + e.getMessage();
			} else {
				obj = t.getClass().getCanonicalName() + ":" + t.getMessage();
			}
			throw t;
		} finally {
			try {
				// 打印日志
				String rpcLog = getRpcLog(invoker, invocation, obj, startTime);
				if (getMaxLogLength() > 0 && rpcLog.length() > getMaxLogLength()) {
					rpcLog = rpcLog.substring(0, getMaxLogLength());
				}
				logger.info(rpcLog);
			} catch (Exception e) {
				logger.error("DubboClientFilter error", e);
			}
			RpcContext.getContext().clearAttachments();
		}
	}

	/**
	 * RPC日志
	 * @param invoker Invoker 
	 * @param invocation invocation
	 * @param result 返回结果
	 * @param startTimestamp 开始时间
	 * @return LOG STR
	 * @since 1.1.0
	 */
	private String getRpcLog(Invoker<?> invoker, Invocation invocation, Object result, long startTimestamp) {
		long endTimestamp = System.currentTimeMillis();
		long cost = endTimestamp - startTimestamp;
		
		// 调用接口
		String className = invoker.getInterface().getCanonicalName();
		// 方法
		String methodName = invocation.getMethodName();
		// 参数
		String arguments = getJSONString(invocation.getArguments());
		// 返回结果
		Object response = null;
//		if (result instanceof Result) {
//			result = ((Result) result).getResult()();
//		} else {
			response = getJSONString(result);
//		}
		
		String localAddress = RpcContext.getContext().getLocalAddressString();
		String remoteAddress = RpcContext.getContext().getRemoteAddressString();
		
		String startTime = DateFormatUtil.format(startTimestamp, DateFormatUtil.FULL_TIME);
		String endTime = DateFormatUtil.format(endTimestamp, DateFormatUtil.FULL_TIME);
		
		return String.format("[Consumer] %s->%s - %s|%s|IN:%s|OUT:%s|[start:%s, end:%s, cost:%dms]", localAddress, remoteAddress, className, methodName, arguments, response, startTime, endTime, cost);
	}
}
