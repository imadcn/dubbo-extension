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
				String rpcLog = getRpcLog(invoker, invocation, obj, startTime, true);
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
}
