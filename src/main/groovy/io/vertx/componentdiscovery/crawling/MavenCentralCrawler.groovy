package io.vertx.componentdiscovery.crawling

import static io.vertx.componentdiscovery.utils.DoWhile.*
import groovy.json.JsonBuilder
import groovyx.net.http.HTTPBuilder
import io.vertx.componentdiscovery.config.Config
import io.vertx.componentdiscovery.model.Artifact
import io.vertx.componentdiscovery.model.Version


Closure updateMd5 = { Artifact newArtifact, String dlPath, HTTPBuilder httpClient ->
}




Config.instance().with {
	Map<String,String> queryParams = [
		q:"l:\"${classifier}\"",
		wt:"json",
		rows:20
	]

	Set<Artifact> artifacts = []


	int totalResults = 0
	int resultsFetched = 0

	HTTPBuilder httpClient = new HTTPBuilder(hostname)

	def result

	repeat {
		queryParams["start"] = resultsFetched
		result = httpClient.get([
			path:searchPath,
			contentType:"application/json", // even though "&wt=json" should do it
			query:queryParams
		])
		Map response = result["response"]
		totalResults = response["numFound"]
		List docs = response["docs"]

		docs.each { Map doc ->
			Artifact artifact = artifacts.find {
				it.groupId == doc["g"] && it.artifactId == doc["a"]
			}
			Version oldVersion = artifact?.versions[-1]
			if (artifact) {
				artifact.versions << new Version(doc["v"], doc["timestamp"] as Long)
				if(!artifact.tags)
					artifact.tags = doc["tags"]
				else if(doc["tags"])
					artifact.tags.addAll(doc["tags"])
			} else {
				artifact = Artifact.fromCentral(doc)
				artifacts << artifact
			}
			// update MD5 (either it's a new Artifact or we might have a more recent version)
			Version newVersion = artifact.versions[-1]
			if (!oldVersion || oldVersion != newVersion) {
				try {
					def md5Result = httpClient.get([
						path:dlPath,
						query:artifact.downloadMd5Params()
					]) { resp, reader ->
						artifact.md5 = reader.text
					}
				} catch(all) {
					// FIXME
					println "${artifact} md5 cannot be fetched, please look into it, ${all}"
				}
			}
		}
		resultsFetched += docs.size()
		println "$resultsFetched artifacts fetched / ${artifacts.size()} artifacts retrieved"
	} until { resultsFetched >= totalResults }


	JsonBuilder builder = new JsonBuilder(artifacts)
	exportFile.withWriter {
		it << builder.toPrettyString()
	}
}