package com.imadcn.framework.dubbo.filter;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.rpc.Filter;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.RpcInvocation;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.imadcn.framework.common.time.DateFormatUtil;
import com.imadcn.framework.dubbo.logger.LogUtil;

/**
 * 生产者日志拦截器
 * 
 * @author yangchao
 * @since 2017-08-09
 */
@Activate(group = Constants.PROVIDER)
public class ProviderLogFilter implements Filter {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	public static final String CLIENT_REQUEST_ID = "clientRequestId";

	@Override
	public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {

		long startTime = System.currentTimeMillis();
		long endTime = 0;
		// RpcContext.getContext().setInvoker(invoker).setInvocation(invocation).setLocalAddress(NetUtils.getLocalHost(), 0).setRemoteAddress(invoker.getUrl().getHost(), invoker.getUrl().getPort());

		if (invocation instanceof RpcInvocation) {
			((RpcInvocation) invocation).setInvoker(invoker);
		}

		Object obj = null;
		String className = invoker.getInterface().getCanonicalName();
		String methodName = invocation.getMethodName();
		Object[] arguments = invocation.getArguments();
		Map<String, String> attachments = RpcContext.getContext().getAttachments();
		String logId = "";

		if (attachments != null) {
			logId = attachments.get(LogUtil.LOG_ID);
		}

		try {
			obj = invoker.invoke(invocation);
			Result result = getResult(obj, invoker);
			return result;
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
				String inputParams = "";
				String rspResult = "";

				if (arguments != null) {
					inputParams = JSON.toJSONStringWithDateFormat(arguments, DateFormatUtil.FULL_TIME, SerializerFeature.DisableCircularReferenceDetect);
				}

				if (obj != null) {
					rspResult = JSON.toJSONStringWithDateFormat(obj, DateFormatUtil.FULL_TIME, SerializerFeature.DisableCircularReferenceDetect);
				}

				endTime = (endTime == 0 ? System.currentTimeMillis() : endTime);
				// 打印日志
				String rpcLog = getRpcLog(className, methodName, inputParams, rspResult, startTime, endTime);
				int logLength = 10240;

				if (logLength != -1 && rpcLog.length() > logLength) {
					rpcLog = rpcLog.substring(0, logLength);
				}

				LogUtil.log(logger, logId, rpcLog);
			} catch (Exception e) {
				logger.error("DubboClientFilter error", e);
			}

			RpcContext.getContext().clearAttachments();
		}
	}

	private Result getResult(Object obj, Invoker<?> invoker) {
		Result result = (Result) obj;
		return result;
	}

	private String getRpcLog(String className, String methodName, String inputParams, String rspResult, long startTime, long endTime) {
		String localAddress = RpcContext.getContext().getLocalAddressString();
		String remoteAddress = RpcContext.getContext().getRemoteAddressString();
		long cost = endTime - startTime;
		String startTimeStr = DateFormatUtil.format(startTime, DateFormatUtil.FULL_TIME);
		String endTimeStr = DateFormatUtil.format(endTime, DateFormatUtil.FULL_TIME);
		return String.format("[DUBBO-Provider] %s->%s - %s|%s|IN:%s|OUT:%s|[start:%s, end:%s, cost:%dms]", remoteAddress, localAddress, className, methodName, inputParams, rspResult, startTimeStr, endTimeStr, cost);
	}
}
