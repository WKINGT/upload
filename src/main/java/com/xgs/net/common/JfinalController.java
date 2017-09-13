package com.xgs.net.common;

import javax.servlet.http.HttpSession;

import com.jfinal.core.Controller;
import com.jfinal.core.Injector;

public class JfinalController extends Controller {

	public HttpSession getSession() {
//		try {
//			return SessionManager.getSession(getRequest(), getResponse());
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		return null;
		return getRequest().getSession();
	}
	
	protected boolean isGet() {
		if (getRequest().getMethod().toLowerCase().equals("get")) {
			return true;
		}
		return false;
	}
	
	
	/**
	 * Get model from http request.
	 */
	public <T> T getModel(Class<T> modelClass) {
		return (T)Injector.injectModel(modelClass, null, getRequest(), true);
	}
	
}
