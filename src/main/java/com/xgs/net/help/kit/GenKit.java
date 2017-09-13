package com.xgs.net.help.kit;

import java.util.List;

import javax.sql.DataSource;

import com.jfinal.plugin.activerecord.generator.BaseModelGenerator;
import com.jfinal.plugin.activerecord.generator.Generator;
import com.jfinal.plugin.activerecord.generator.TableMeta;

public class GenKit extends Generator{

	private String modelPackageName, modelOutputDir;
	public GenKit(DataSource dataSource, BaseModelGenerator baseModelGenerator) {
		super(dataSource, baseModelGenerator);
	}

	public GenKit(DataSource dataSource, String baseModelPackageName, String baseModelOutputDir, String modelPackageName, String modelOutputDir){
		super(dataSource, baseModelPackageName, baseModelOutputDir, modelPackageName, modelOutputDir);
		this.modelPackageName = modelPackageName;
		this.modelOutputDir =modelOutputDir;
	}
	
	public void genTable(){
		List<TableMeta> tableMetas = metaBuilder.build();
		TableKitGenerator tableKitGenerator = new TableKitGenerator(this.modelPackageName, this.modelOutputDir);
		
		System.out.println("Generate TableKit file ...");
		tableKitGenerator.generate(tableMetas);
	}
	
	public void generate(){
		super.generate();
		this.genTable();
	}
}
