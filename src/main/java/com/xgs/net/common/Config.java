package com.xgs.net.common;

import com.jfinal.config.Constants;
import com.jfinal.config.Handlers;
import com.jfinal.config.Interceptors;
import com.jfinal.config.JFinalConfig;
import com.jfinal.config.Plugins;
import com.jfinal.config.Routes;
import com.jfinal.kit.PropKit;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.plugin.activerecord.CaseInsensitiveContainerFactory;
import com.jfinal.plugin.activerecord.ModelRecordElResolver;
import com.jfinal.plugin.druid.DruidPlugin;
import com.jfinal.render.ViewType;
import com.jfinal.template.Engine;
import com.xgs.net.app.TestUpload;
import com.xgs.net.app.UpdateController;
import com.xgs.net.model._MappingKit;


public class Config extends JFinalConfig{

	@Override
	public void configConstant(Constants me) {
		me.setDevMode(true);
		me.setViewType(ViewType.JSP);
		me.setError404View("/error/404.html");
		me.setError500View("/error/500.html");
		
		me.setBaseUploadPath(PropKit.use("cnf.txt").get("file.upload.dir"));
		me.setMaxPostSize(PropKit.use("cnf.txt").getInt("file.upload.maxSize"));

		ModelRecordElResolver.setWorking(false);
	}

	@Override
	public void configRoute(Routes me) {
		me.add("/" , TestUpload.class);
		me.add("/update" , UpdateController.class);
	}

	@Override
	public void configPlugin(Plugins me) {
		loadPropertyFile("jdbc_app_config.txt");
		DruidPlugin druidPlugin = new DruidPlugin(getProperty("jdbcUrl"),
		getProperty("user"), getProperty("password"));
		druidPlugin.setMaxPoolPreparedStatementPerConnectionSize(getPropertyToInt("min_poll",10));
		druidPlugin.setMaxActive(getPropertyToInt("max_poll",100));
		druidPlugin.setValidationQuery(getProperty("validation_query"));
		me.add(druidPlugin);
		ActiveRecordPlugin arp = new ActiveRecordPlugin(druidPlugin);
		arp.setShowSql(getPropertyToBoolean("show_sql",false));
		arp.setContainerFactory(new CaseInsensitiveContainerFactory(true));
		_MappingKit.mapping(arp);
		me.add(arp);
	}

	@Override
	public void configInterceptor(Interceptors me) {}

	@Override
	public void configHandler(Handlers me) {
		
	}
	@Override
	public void afterJFinalStart(){
		
	};
	public void beforeJFinalStop(){
		
	}

	@Override
	public void configEngine(Engine arg0) {
		
	}
}