package io.vertx.componentdiscovery.crawling

import groovy.json.JsonSlurper
import groovyx.net.http.HTTPBuilder
import io.vertx.componentdiscovery.config.Config
import io.vertx.componentdiscovery.model.Artifact

import java.security.MessageDigest

import org.apache.commons.codec.binary.Hex

Closure calcMD5 = { File f ->
	MessageDigest digest = MessageDigest.getInstance("MD5")
	f.withInputStream { InputStream is ->
		is.eachByte(1024) { byte[] buffer, int bytesRead ->
			digest.update(buffer, 0, bytesRead)
		}
	}
	new String(Hex.encodeHex(digest.digest()))
}



Config.instance().with {
	HTTPBuilder httpClient = new HTTPBuilder(hostname)
	// Load artifacts from export
	JsonSlurper slurper = new JsonSlurper()
	Set jsonArtifacts = slurper.parse(exportFile)
	Set artifacts = []
	jsonArtifacts.each {
		artifacts << Artifact.fromExport(it)
	}


	artifacts.each { Artifact artifact ->
		// try to find previously fetched artifacts
		File pack = new File("${artifactsDir}/${artifact.jarLocalPath()}")
		if (pack.exists() && pack.isFile()) {
			// check MD5
			String md5 = calcMD5(pack)
			if (md5 == artifact.md5) {
				println "Artifact $artifact hasn't changed, no need to download it"
				return
			} else {
				println "Artifact ${artifact.downloadMd5Params()} 's MD5 has changed, old=$artifact.md5, new=$md5"
			}
		}
		// Download file
		println "Downloading $artifact , ${hostname}/${dlPath}?filepath=${artifact.downloadRequestParams()['filePath']}"
		httpClient.get([
			path:dlPath,
			query:artifact.downloadRequestParams()
		]) { resp, inputStream ->
			pack.withOutputStream { OutputStream writer ->
				// pipe streams
				writer << inputStream
			}
		}

	}

}


