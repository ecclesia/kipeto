package de.ecclesia.kipeto.bootstrap;

import java.io.File;
import java.io.OutputStream;
import java.util.Date;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.ecclesia.kipeto.common.util.Streams;

public class HttpUpdateStrategy implements IUpdateStrategy {

	private static final Logger log = LoggerFactory.getLogger(HttpUpdateStrategy.class);
	private String jarUrl;
	private DefaultHttpClient client;
	private long contentLength;

	public HttpUpdateStrategy(String repositoryUrl) {
		jarUrl = String.format("%s/%s/%s", repositoryUrl, BootstrapApp.DIST_DIR, BootstrapApp.JAR_FILENAME);
		client = new DefaultHttpClient();
	}
	
	public String getUpdateUrl() {
		return jarUrl;
	}
	
	public Date downloadUpdate(OutputStream destinationStream) throws Exception {
		HttpGet httpget = new HttpGet();
		HttpResponse contentResponse = client.execute(httpget);
		HttpEntity entity = contentResponse.getEntity();

		Header lastModifiedHeader = contentResponse.getFirstHeader("Last-Modified");
		Date lastModified = DateUtils.parseDate(lastModifiedHeader.getValue());

		int statusCode = contentResponse.getStatusLine().getStatusCode();
		log.debug("HttpGet at {}, Status is {}", jarUrl, statusCode);
		if (statusCode != HttpStatus.SC_OK) {
			throw new RuntimeException(contentResponse.getStatusLine().toString());
		}
		
		Streams.copyStream(entity.getContent(), destinationStream, true);
		return lastModified;
}
	
	public boolean isUpdateAvailable(File localJarFile) throws Exception {
		HttpHead httpHead = new HttpHead(jarUrl);
		HttpResponse headResponse = client.execute(httpHead);
		int statusCode = headResponse.getStatusLine().getStatusCode();
		log.debug("HttpHead at {}, Status is {}", jarUrl, statusCode);
		if (statusCode != HttpStatus.SC_OK) {
			throw new RuntimeException(headResponse.getStatusLine().toString());
		}

		Header lastModifiedHeader = headResponse.getFirstHeader("Last-Modified");
		Date lastModified = DateUtils.parseDate(lastModifiedHeader.getValue());
		log.debug("HttpHead {} - Last-Modified: {}", jarUrl, lastModified);

		Header contentLengthHeader = headResponse.getFirstHeader("Content-Length");
		contentLength = Long.parseLong(contentLengthHeader.getValue());
		log.debug("HttpHead {} - Content-Length: {}", jarUrl, contentLength);

		log.debug("Local Kipeto Jar {} - Last-Modified: {}", localJarFile, new Date(localJarFile.lastModified()));
		log.debug("Local Kipeto Jar {} - Content-Length: {}", localJarFile, localJarFile.length());

		return (!localJarFile.exists() || lastModified.getTime() != localJarFile.lastModified() || contentLength != localJarFile.length());
	}

}
