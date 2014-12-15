package io.vertx.componentdiscovery.crawling

import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import io.vertx.componentdiscovery.config.Config
import io.vertx.componentdiscovery.model.Artifact

import java.util.zip.ZipFile

Config.instance().with {
	// Load artifacts from export
	JsonSlurper slurper = new JsonSlurper()
	Set jsonArtifacts = slurper.parse(exportFile)
	Set artifacts = []
	jsonArtifacts.each {
		artifacts << Artifact.fromExport(it)
	}
	artifacts.each { Artifact artifact ->
		File pack = new File("${artifactsDir}/${artifact.jarLocalPath()}")
		try {
			ZipFile zip = new ZipFile(pack)
			InputStream modIs = zip.getInputStream(zip.getEntry("mod.json"))
			Map infos = slurper.parse(modIs)
			artifact.complementaryInfos = infos
		} catch(all) {
			println "Cannot get complementary informations regarding ${artifact}. Error is ${all}"
		}

		JsonBuilder builder = new JsonBuilder(artifacts)
		exportFile.withWriter {
			it << builder.toPrettyString()
		}
	}
}