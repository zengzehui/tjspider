package com.huawei.tjspider;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletResponse;

import org.apache.http.Header;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(value = "/htm")
public class HtmlController {

	private static final Logger logger = LoggerFactory.getLogger(HtmlController.class);

	@RequestMapping(value = "/{charset}", method = RequestMethod.GET)
	public @ResponseBody String getHtm(@PathVariable String charset, @RequestParam("url") String url,
			HttpServletResponse response) throws IOException {
		logger.info(url + " getHtm STARTS =====");
		HttpGet httpget = new HttpGet(url);
		httpget.setHeader("User-Agent",
				"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/37.0.2062.103 Safari/537.36");
		CloseableHttpClient httpclient = HttpClients.createDefault();
		RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(30000).setConnectTimeout(60000).build();
		httpget.setConfig(requestConfig);
		
		try {
			CloseableHttpResponse httpresponse = httpclient.execute(httpget);
			if (httpresponse.getStatusLine().getStatusCode() == 200) {
				response.setStatus(200);
				response.setContentType("text/html; charset=utf-8");
				String charsetResponse = getResponseCharset(httpresponse.getHeaders("Content-Type"));
				InputStream isContent = httpresponse.getEntity().getContent();

				byte[] byteContent = InputStreamToByte(isContent);

				logger.info("byteContent.length = " + byteContent.length);

				Document doc = Jsoup.parse(byteToInputStream(byteContent), charset, url);
				String charsetHtml = getHtmlCharset(doc);

				String finalCharset = getFinalCharset(charsetHtml, charsetResponse, charset);
				if (charset.equalsIgnoreCase(finalCharset)) {
					response.getWriter().write(doc.html());
				} else {
					doc = Jsoup.parse(byteToInputStream(byteContent), finalCharset, url);
					response.getWriter().write(doc.html());
				}
				response.getWriter().close();
				isContent.close();
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
			logger.info(url + " getHtm Exception *****");
			e.printStackTrace();
			response.setStatus(404);
		}
		logger.info(url + " getHtm ENDS -----");
		return null;
	}

	private byte[] InputStreamToByte(InputStream is) throws IOException {
		ByteArrayOutputStream baoStream = new ByteArrayOutputStream();
		byte[] buff = new byte[100];
		int rc = 0;
		while ((rc = is.read(buff, 0, 100)) > 0) {
			baoStream.write(buff, 0, rc);
		}
		return baoStream.toByteArray();
	}

	private InputStream byteToInputStream(byte[] b) {
		return new ByteArrayInputStream(b);
	}

	private String getFinalCharset(String charsetHtml, String charsetResponse, String charset) {
		String result = charset;
		if (charsetHtml != null && !"".equalsIgnoreCase(charsetHtml)) {
			result = charsetHtml;
		} else if (charsetResponse != null && !"".equalsIgnoreCase(charsetResponse)) {
			result = charsetResponse;
		}
		if ("gb2312".equalsIgnoreCase(result)) {
			result = "gbk";
		}
		return result;
	}

	private String getResponseCharset(Header[] ContentTypeHeaders) {
		if (ContentTypeHeaders.length > 0) {
			String s = ContentTypeHeaders[0].getValue();
			Pattern patternCharset = Pattern.compile("charset=([A-Za-z0-9\\-]+)");
			Matcher matcher = patternCharset.matcher(s);
			while (matcher.find()) {
				return matcher.group(0).split("=")[1].toLowerCase().trim();
			}
		}
		return null;
	}

	private String getHtmlCharset(Document doc) {
		/*
		 * facebook html5 <meta charset="utf-8" />
		 * 
		 * csdn html4 <meta http-equiv="Content-Type"
		 * content="text/html; charset=utf-8" />
		 */
		String result = null;
		Elements heads = doc.getElementsByTag("head");
		if (heads.size() > 0) {
			Element head = heads.get(0);
			Pattern patternCharset = Pattern.compile("charset=([A-Za-z0-9\\-]+)");
			Elements elementCharsets = head.getElementsByAttributeValueMatching("content", patternCharset);
			if (elementCharsets.size() > 0) {
				Element elementCharset = elementCharsets.get(0);
				Matcher matcher = patternCharset.matcher(elementCharset.attr("content"));
				while (matcher.find()) {
					result = matcher.group(0).split("=")[1].toLowerCase().trim();
				}
			} else {
				elementCharsets = head.getElementsByAttribute("charset");
				if (elementCharsets.size() > 0) {
					Element elementCharset = elementCharsets.get(0);
					result = elementCharset.attr("charset");
				}
			}
		}
		if (result == null) {
			logger.error("html charset is null");
		} else if ("gb2312".equalsIgnoreCase(result)) {
			logger.info("html charset is " + result);
			result = "gbk";
		} else {
			logger.info("html charset is " + result);
		}
		logger.info("html charset finally is " + result);
		return result;
	}
}
