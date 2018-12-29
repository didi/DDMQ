package com.didi.carrera.console.web;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.didi.carrera.console.web.util.DateTimePropertyEditor;
import com.didi.carrera.console.web.util.StringPropertyEditor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;


@SuppressWarnings("rawtypes")
public abstract class AbstractBaseController {

	public Logger logger = LoggerFactory.getLogger(this.getClass());

	public static final String DATA = "type=data";
	protected static final String MOB_REGEX = "^13[0-9]{9}$|^14[0-9]{9}$|^15[0-9]{9}$|^17[0-9]{9}$|^18[0-9]{9}$"; // 手机号校验正则

	@InitBinder
	protected void initBinder(ServletRequestDataBinder binder) {
		binder.registerCustomEditor(String.class, new StringPropertyEditor());
		binder.registerCustomEditor(Date.class, new DateTimePropertyEditor());
	}

	public class MyHandlerExceptionResolver implements HandlerExceptionResolver {
		@Override
		public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler,
				Exception ex) {
			return new ModelAndView("exception");
		}
	}

	/**
	 * 默认是第一页
	 * 
	 * @param request
	 * @return
	 */
	public int getPageNo(HttpServletRequest request) {
		String pageNoStr = request.getParameter("pageNum");
		return StringUtils.isNotBlank(pageNoStr) ? Integer.parseInt(pageNoStr) : 1;
	}

	/**
	 * 默认每页20条
	 * 
	 * @param request
	 * @return
	 */
	public int getPageSize(HttpServletRequest request) {
		String pageSizeStr = request.getParameter("pageSize");
		return StringUtils.isNotBlank(pageSizeStr) ? Integer.parseInt(pageSizeStr) : 20;
	}

	protected String getParameter(HttpServletRequest request, String para) {
		String value = request.getParameter(para);
		if (StringUtils.isBlank(value)) {
			return "";
		}
		return value.trim();
	}

	protected int getParameterInt(HttpServletRequest request, String para) {
		String value = request.getParameter(para);
		if (StringUtils.isBlank(value)) {
			return 0;
		}
		if (!StringUtils.isNumeric(value)) {
			return 0;
		}
		try {
			return Integer.parseInt(value);
		} catch (Exception e) {
			logger.error("getParameter error");
			return 0;
		}
	}

	protected float getParameterFloat(HttpServletRequest request, String para) {
		String value = request.getParameter(para);
		if (StringUtils.isBlank(value)) {
			return 0;
		}
		try {
			return Float.valueOf(value);
		} catch (Exception e) {
			logger.error("getParameter error");
			return 0;
		}
	}

	protected long getParameterLong(HttpServletRequest request, String para) {
		String value = request.getParameter(para);
		if (StringUtils.isBlank(value)) {
			return 0;
		}
		if (!StringUtils.isNumeric(value)) {
			return 0;
		}
		try {
			return Long.parseLong(value);
		} catch (Exception e) {
			logger.error("getParameter error" + e);
			return 0;
		}
	}

	protected JSONObject getBodyParameter(HttpServletRequest request) {
		JSONObject json = null;
		byte[] bytes = new byte[1024 * 1024];
		InputStream is;
		try {
			is = request.getInputStream();

			int nRead = 1;
			int nTotalRead = 0;
			while (nRead > 0) {
				nRead = is.read(bytes, nTotalRead, bytes.length - nTotalRead);
				if (nRead > 0)
					nTotalRead = nTotalRead + nRead;
			}
			String str = new String(bytes, 0, nTotalRead, "utf-8");
			if (str.indexOf("{") == 0) {
				json = JSON.parseObject(str);
			}
		} catch (IOException e) {
			logger.error("getParameter error" + e);
		}
		return json;
	}

	public String getBindingResultErrorInfo(BindingResult bindingResult) {
		return "bind error";
	}

}