package com.huawei.tjspider;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLEncoder;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
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
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(value = "/img")
public class ImageController {

	private static final Logger logger = LoggerFactory.getLogger(ImageController.class);

	@RequestMapping(value = "", method = RequestMethod.GET, produces = "image/gif")
	public @ResponseBody byte[] getImg(@RequestParam("url") String url, @RequestParam("referer") String referer,
			HttpServletResponse response) throws IOException {
		String shortUrl = url.substring(url.lastIndexOf("/"));
		logger.info(shortUrl + " ===== STARTS " + url);
		logger.info(shortUrl + " Referer: " + referer);

		DateTime now = new DateTime();
		DateTimeFormatter dtDtf = DateTimeFormat.forPattern("yyyy-MM-dd");

		File imgFile = new File(Thread.currentThread().getContextClassLoader().getResource("").getPath() + "/image/"
				+ dtDtf.print(now) + "/" + URLEncoder.encode(url, "UTF-8"));
		File imgFolder = imgFile.getParentFile();
		if (!imgFolder.exists()) {
			imgFolder.mkdirs();
		}
		if (!imgFile.exists()) {
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
					logger.info(shortUrl + " Writed to " + imgFile.getAbsolutePath() + " size=" + imgFile.length());
				} else {
					response.sendError(404, "Original response IS NOT 200.");
					logger.warn(shortUrl + " Response 404: Original response: "
							+ srcResponse.getStatusLine().toString());
					return null;
				}
			} catch (Exception e) {
				response.sendError(404, " Response 404: Failed to get or write image.");
				logger.error(shortUrl + " Response 404: Failed to get or write image.");
				logger.error(shortUrl + " " + e.getMessage());
				e.printStackTrace();
				if (imgFile.exists()) {
					imgFile.delete();
				}
				return null;
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
				logger.info(shortUrl + " Finally: get and write image.");
			}
		} else {
			logger.info(shortUrl + " Image exists.");
		}

		if (!imgFile.exists()) {
			response.sendError(404, "Original response IS 200, but FAILED to write image to file.");
			logger.warn(shortUrl + " Response 404: Original response IS 200, but FAILED to write image to file.");
			return null;
		} else {
			FileInputStream fis = null;
			try {
				fis = new FileInputStream(imgFile);
				return IOUtils.toByteArray(fis);
			} catch (Exception e) {
				logger.error(shortUrl + " FAILED to serve image to client.");
				logger.error(shortUrl + " " + e.getMessage());
				e.printStackTrace();
				return null;
			} finally {
				if (fis != null) {
					fis.close();
				}
				logger.info(shortUrl + " Finally: serve image.");
			}
		}
	}
}
