package io.vertx.componentdiscovery.reports

import groovy.json.JsonSlurper
import groovy.text.GStringTemplateEngine
import io.vertx.componentdiscovery.config.Config
import io.vertx.componentdiscovery.model.Artifact


Config.instance().with {
	JsonSlurper slurper = new JsonSlurper()
	List rawServices = slurper.parse(exportFile)
	List<Artifact> services = []
	rawServices.each { Map map ->
		services << Artifact.fromExport(map)
	}

	InputStream tplIs = this.getClass().classLoader.getResourceAsStream("template.html")


	Map binding = [services:services]
	GStringTemplateEngine engine = new GStringTemplateEngine()
	tplIs.withReader { Reader reader ->
		String rendered = engine.createTemplate(reader).make(binding)
		htmlReport.withWriter { Writer writer -> writer << rendered }
	}
}