package com.huawei.tjspider;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLEncoder;

import javax.servlet.http.HttpServletResponse;

import org.apache.http.Header;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping(value = "/img")
public class ImageController {

	private static final Logger logger = LoggerFactory.getLogger(ImageController.class);

	@RequestMapping(value = "/0917", method = RequestMethod.GET)
	public void getImg0917(@RequestParam("url") String url, @RequestParam("referer") String referer,
			HttpServletResponse response) throws IOException {
		logger.info(url + " STARTS =====");
		HttpGet httpget = new HttpGet(url);
		httpget.setHeader("Referer", referer);
		httpget.setHeader("User-Agent",
				"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/37.0.2062.103 Safari/537.36");
		CloseableHttpClient httpclient = HttpClients.createDefault();
		/*
		 * The Connection Timeout (http.connection.timeout) – the time to
		 * establish the connection with the remote host
		 * 
		 * The Socket Timeout (http.socket.timeout) – the time waiting for data
		 * – after the connection was established; maximum time of inactivity
		 * between two data packets
		 * 
		 * The Connection Manager Timeout (http.connection-manager.timeout) –
		 * the time to wait for a connection from the connection manager/pool
		 * 
		 * The first two parameters – the connection and socket timeouts – are
		 * the most important, but setting a timeout for obtaining a connection
		 * is definitly important in high load scenarios, which is why the third
		 * parameter shouldn’t be ignored.
		 */
		RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(30 * 1000).setSocketTimeout(90 * 1000)
				.setConnectionRequestTimeout(10 * 1000).build();
		httpget.setConfig(requestConfig);

		BufferedInputStream bis = null;
		BufferedOutputStream bos = null;
		try {
			CloseableHttpResponse httpresponse = httpclient.execute(httpget);

			if (httpresponse.getStatusLine().getStatusCode() == 200) {
				response.setStatus(200);
				Header[] allHeaders = httpresponse.getAllHeaders();
				for (int i = 0; i < allHeaders.length; i++) {
					response.setHeader(allHeaders[i].getName(), allHeaders[i].getValue());
				}
				bis = new BufferedInputStream(httpresponse.getEntity().getContent());
				bos = new BufferedOutputStream(response.getOutputStream());
				byte[] buff = new byte[4096];
				int bytesRead;
				while (-1 != (bytesRead = bis.read(buff, 0, buff.length))) {
					bos.write(buff, 0, bytesRead);
				}
			} else {
				logger.error(url);
				logger.error(httpresponse.getStatusLine().toString());
				Header[] allHeaders = httpresponse.getAllHeaders();
				for (int i = 0; i < allHeaders.length; i++) {
					logger.error(allHeaders[i].getName() + ": " + allHeaders[i].getValue());
				}
				response.setStatus(404);
			}
		} catch (Exception e) {
			logger.info(url + " Exception *****");
			e.printStackTrace();
			response.setStatus(404);
		} finally {
			if (bis != null)
				bis.close();
			if (bos != null)
				bos.close();
			httpclient.close();
		}
		logger.info(url + " ENDS -----");
	}

	@RequestMapping(value = "", method = RequestMethod.GET)
	public void getImg(@RequestParam("url") String url, @RequestParam("referer") String referer,
			HttpServletResponse response) throws IOException {
		logger.info(url + " STARTS =====");
		HttpGet httpget = new HttpGet(url);
		httpget.setHeader("Referer", referer);
		httpget.setHeader("User-Agent",
				"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/37.0.2062.103 Safari/537.36");
		CloseableHttpClient httpclient = HttpClients.createDefault();
		RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(30 * 1000).setSocketTimeout(90 * 1000)
				.setConnectionRequestTimeout(10 * 1000).build();
		httpget.setConfig(requestConfig);

		BufferedInputStream bisFile = null;
		BufferedOutputStream bosFile = null;
		BufferedOutputStream bosResponse = null;
		DateTime now = new DateTime();
		DateTimeFormatter dtDtf = DateTimeFormat.forPattern("yyyy-MM-dd");

		File imgFile = new File(Thread.currentThread().getContextClassLoader().getResource("").getPath() + "/image/"
				+ dtDtf.print(now) + "/" + URLEncoder.encode(url, "UTF-8"));
		File imgFolder = imgFile.getParentFile();
		if (!imgFolder.exists()) {
			imgFolder.mkdirs();
		}
		try {
			CloseableHttpResponse srcResponse = httpclient.execute(httpget);
			if (srcResponse.getStatusLine().getStatusCode() == 200) {
				bosFile = new BufferedOutputStream(new FileOutputStream(imgFile));
				srcResponse.getEntity().writeTo(bosFile);
				bosFile.close();
				logger.info(url + " write to " + imgFile.getAbsolutePath());
				response.setStatus(200);
				Header[] allHeaders = srcResponse.getAllHeaders();
				for (int i = 0; i < allHeaders.length; i++) {
					response.setHeader(allHeaders[i].getName(), allHeaders[i].getValue());
				}
				bisFile = new BufferedInputStream(new FileInputStream(imgFile));
				bosResponse = new BufferedOutputStream(response.getOutputStream());
				byte[] buff = new byte[4096];
				int bytesRead;
				while (-1 != (bytesRead = bisFile.read(buff, 0, buff.length))) {
					bosResponse.write(buff, 0, bytesRead);
				}
				response.flushBuffer();
			} else {
				logger.info(url + " Source Response is " + srcResponse.getStatusLine().toString());
				response.sendError(404);
				Header[] allHeaders = srcResponse.getAllHeaders();
				for (int i = 0; i < allHeaders.length; i++) {
					logger.error(allHeaders[i].getName() + ": " + allHeaders[i].getValue());
				}
			}
		} catch (Exception e) {
			logger.info(url + " Exception *****");
			e.printStackTrace();
			if (imgFile.exists()) {
				imgFile.delete();
				logger.info(url + " Deleted *****");
			}
			response.sendError(404);
		} finally {
			if (bisFile != null) {
				bisFile.close();
			}
			if (bosFile != null) {
				bosFile.close();
			}
			if (bosResponse != null) {
				bosResponse.close();
			}
			httpclient.close();
		}

		logger.info(url + " ENDS -----");
	}

	@RequestMapping(value = "/1017", method = RequestMethod.GET)
	public void getImg1017(@RequestParam("url") String url, @RequestParam("referer") String referer,
			HttpServletResponse response) throws IOException {
		
		String shortUrl = url.substring(url.lastIndexOf("/"));
		logger.info(shortUrl + " STARTS ===== " + url);

		DateTime now = new DateTime();
		DateTimeFormatter dtDtf = DateTimeFormat.forPattern("yyyy-MM-dd");

		File imgFile = new File(Thread.currentThread().getContextClassLoader().getResource("").getPath() + "/image/"
				+ dtDtf.print(now) + "/" + URLEncoder.encode(url, "UTF-8"));
		File imgFolder = imgFile.getParentFile();
		if (!imgFolder.exists()) {
			imgFolder.mkdirs();
		}
		if (!imgFile.exists()) {
			logger.info(shortUrl + " Start getting image.");
			CloseableHttpResponse srcResponse = null;
			BufferedOutputStream bosFile = null;
			CloseableHttpClient httpclient = null;
			try {
				HttpGet httpget = new HttpGet(url);
				httpget.setHeader("Referer", referer);
				httpget.setHeader("User-Agent",
						"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/37.0.2062.103 Safari/537.36");

				httpget.setConfig(RequestConfig.custom().setConnectTimeout(30 * 1000).setSocketTimeout(90 * 1000)
						.setConnectionRequestTimeout(10 * 1000).build());

				httpclient = HttpClients.createDefault();
				srcResponse = httpclient.execute(httpget);
				if (srcResponse.getStatusLine().getStatusCode() == 200) {
					bosFile = new BufferedOutputStream(new FileOutputStream(imgFile));
					srcResponse.getEntity().writeTo(bosFile);
					bosFile.close();
					srcResponse.close();
					httpclient.close();
					logger.info(shortUrl + " Writed to " + imgFile.getAbsolutePath() + " size=" + imgFile.length());
				} else {
					response.sendError(404, "Original response IS NOT 200.");
					logger.info(shortUrl + " Response 404: Original response is not 200.");
					return;
				}
				srcResponse.close();
				httpclient.close();
			} catch (Exception e) {
				logger.info(shortUrl + " Exception: Failed to get image or write image to file.");
				e.printStackTrace();
				if (imgFile.exists()) {
					imgFile.delete();
				}
			} finally {
				if (bosFile != null) {
					bosFile.close();
				}
				if (srcResponse != null) {
					srcResponse.close();
				}
				if (httpclient != null) {
					httpclient.close();
				}
				logger.info(shortUrl + " Finally: getting and writing image to file.");
			}
		} else {
			logger.info(shortUrl + " Skip getting image.");
		}
		if (!imgFile.exists()) {
			response.sendError(404, "Original response IS 200, but FAILED to write image to file.");
			logger.info(shortUrl + " Response 404: Original response is not 200.");
			return;
		} else {
			logger.info(shortUrl + " Start serving image.");
			BufferedInputStream bisFile = null;
			BufferedOutputStream bosResponse = null;
			try {
				bisFile = new BufferedInputStream(new FileInputStream(imgFile));
				response.setContentLength((int) imgFile.length());
				bosResponse = new BufferedOutputStream(response.getOutputStream());
				byte[] buff = new byte[4096];
				int bytesRead;
				while (-1 != (bytesRead = bisFile.read(buff, 0, buff.length))) {
					bosResponse.write(buff, 0, bytesRead);
				}
				bosResponse.close();
				response.flushBuffer();
				bisFile.close();
			} catch (Exception e) {
				logger.info(shortUrl + " Exception: FAILED to serve image to client.");
				e.printStackTrace();
			} finally {
				logger.info(shortUrl + " Finally: serving image to client.");
			}
			logger.info(shortUrl + " ENDS =======");
		}
	}
}
