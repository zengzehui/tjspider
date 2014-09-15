package com.huawei.tjspider;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.apache.http.Header;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
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

	@RequestMapping(value = "", method = RequestMethod.GET)
	public void getImg(@RequestParam("url") String url, @RequestParam("referer") String referer,
			HttpServletResponse response) throws IOException {
		logger.info(url + " getImg STARTS =====");
		HttpGet httpget = new HttpGet(url);
		httpget.setHeader("Referer", referer);
		httpget.setHeader("User-Agent",
				"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/37.0.2062.103 Safari/537.36");
		CloseableHttpClient httpclient = HttpClients.createDefault();
		RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(30000).setConnectTimeout(30000).build();
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
			logger.info(url + " getImg Exception *****");
			e.printStackTrace();
		} finally {
			if (bis != null)
				bis.close();
			if (bos != null)
				bos.close();
			httpclient.close();
		}
		logger.info(url + " getImg ENDS -----");
	}
}
