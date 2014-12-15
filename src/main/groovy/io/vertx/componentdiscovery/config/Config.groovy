package io.vertx.componentdiscovery.config

import groovy.json.JsonSlurper

class Config {

	private static final File CONFIG = new File("conf.json")
	private static Config instance
	private static final JsonSlurper slurper = new JsonSlurper()

	File exportFile
	File htmlReport
	File artifactsDir
	String hostname
	String searchPath
	String dlPath
	String classifier

	private Config(){
	}

	private static Config instance(){
		if(!instance) {
			Map conf = slurper.parse(CONFIG)
			instance = new Config()
			instance.with {
				exportFile = new File(conf["EXPORT_FILE"])
				htmlReport = new File(conf["HTML_REPORT"])
				artifactsDir = new File(conf["ARTIFACTS_DIR"])
				hostname = conf["HOSTNAME"]
				searchPath = conf["SEARCH_PATH"]
				dlPath = conf["DL_PATH"]
				classifier = conf["CLASSIFIER"]
			}
		}
		instance
	}
}
