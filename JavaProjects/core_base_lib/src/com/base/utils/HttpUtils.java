package com.base.utils;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.base.enums.HttpHeaders;
import com.base.model.BaseModel;
import com.google.gson.JsonObject;

/**
 * 
 * @author Sankha
 *
 */
public final class HttpUtils {

	private static final String USER_ID = "userId";
	private static final String USER_NAME = "userName";
	private static final String ACTION = "action";
	private static final String ROLE = "role";


	private HttpUtils() {

	}

	public static String getHeader(String header) {
		RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
		if (requestAttributes instanceof ServletRequestAttributes) {
			HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
			return request.getHeader(header);
		}
		return null;
	}

	public static String getHeader(HttpHeaders header) {
		RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
		if (requestAttributes instanceof ServletRequestAttributes) {
			HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
			return request.getHeader(header.getValue());
		}
		return null;
	}

	public static void setHeaderToPayloadByKey(String header, Object object, String attribute) {
		// reflection set object attribute
		String headerVal = getHeader(header);
		if (object instanceof JsonObject) {
			JsonObject payLoadJson = (JsonObject) object;
			payLoadJson.addProperty(attribute, headerVal);
		}
	}

	public static void setHeadersToRequestPayload(JsonObject payload) {
		payload.addProperty(USER_NAME, getHeader(HttpHeaders.USER_NAME.getValue()));
		payload.addProperty(USER_ID, getHeader(HttpHeaders.USER_ID.getValue()));
		payload.addProperty(ACTION, getHeader(HttpHeaders.ACTION.getValue()));
		payload.addProperty(ROLE, getHeader(HttpHeaders.ROLE.getValue()));	}

	public static void setHeadersToPayloadForSave(Object object) {
		if (object instanceof BaseModel) {
		//TODO: add more metadata for auditTrail
		}
		
		
	}

}
