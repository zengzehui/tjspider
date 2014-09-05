package com.huawei.tjspider;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletResponse;

import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(value = "/page")
public class PageController {

	private static final Logger logger = LoggerFactory.getLogger(PageController.class);

	@RequestMapping(value = "", method = RequestMethod.GET)
	public @ResponseBody String getPage(@RequestParam("url") String url, HttpServletResponse response)
			throws IOException {
		logger.info("------------------------------");
		logger.info("url = " + url);

		Response jsoupResponse = Jsoup.connect(url).timeout(60 * 1000).execute();
		String srcCharset = jsoupResponse.charset();
		String srcContentType = jsoupResponse.contentType();
		int srcStatusCode = jsoupResponse.statusCode();
		String srcStatusMessage = jsoupResponse.statusMessage();

		logger.info("srcCharset = " + srcCharset);
		logger.info("srcContentType = " + srcContentType);
		logger.info("srcStatusCode = " + srcStatusCode);
		logger.info("srcStatusMessage = " + srcStatusMessage);

		String srcHtml = jsoupResponse.body();
		Document doc = Jsoup.parse(srcHtml);
		String srcHtmlCharset = getHtmlCharset(doc);

		String finalCharset = "UTF-8";
		if (srcHtmlCharset != null || !"".equalsIgnoreCase(srcHtmlCharset)) {
			finalCharset = srcHtmlCharset;
		} else if (srcCharset != null || !"".equalsIgnoreCase(srcCharset)) {
			finalCharset = srcCharset;
		}
		logger.info("finalCharset = " + finalCharset);

		String utf8Html = new String(srcHtml.getBytes(finalCharset), "UTF-8");

		response.setCharacterEncoding("UTF-8");
		if (srcContentType != null && !"".equalsIgnoreCase(srcContentType)) {
			response.setContentType(srcContentType);
		} else {
			response.setContentType("text/html");
		}
		
		response.getWriter().write(utf8Html);
		response.getWriter().close();
		return null;
	}

	private String getHtmlCharset(Document doc) {
		Pattern pCharset = Pattern.compile("charset=([A-Za-z0-9\\-]+)");
		Element elementCharset = doc.getElementsByAttributeValueMatching("content", pCharset).get(0);
		logger.info(elementCharset.outerHtml());
		String charset = elementCharset.attr("content").split(";")[1].split("=")[1].trim().toUpperCase();
		return charset;
	}
}
