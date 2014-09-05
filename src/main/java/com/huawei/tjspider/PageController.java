package com.huawei.tjspider;

import java.io.IOException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletResponse;

import org.jsoup.Connection.Response;
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
@RequestMapping(value = "/html")
public class PageController {

	private static final Logger logger = LoggerFactory.getLogger(PageController.class);

	@RequestMapping(value = "/{charset}", method = RequestMethod.GET)
	public @ResponseBody String getPageUTF8(@PathVariable String charset, @RequestParam("url") String url,
			HttpServletResponse response) throws IOException {
		logger.info("***** " + url);
		Response jsoupResponse = Jsoup.connect(url).timeout(60 * 1000).execute();
		String headerCharset = jsoupResponse.charset();
		logger.info("headerCharset = " + headerCharset);

		Document doc = Jsoup.parse(new URL(url).openStream(), "utf-8", url);
		String htmlCharset = getHtmlCharset(doc);

		if (htmlCharset != null && !"".equalsIgnoreCase(htmlCharset)) {
			charset = htmlCharset;
		} else {
			if (headerCharset != null && !"".equalsIgnoreCase(headerCharset)) {
				charset = headerCharset;
			}
		}
		logger.info("***** InputStream = " + charset);
		doc = Jsoup.parse(new URL(url).openStream(), charset, url);
		response.setContentType("text/html; charset=utf-8");
		response.getWriter().write(doc.html());
		response.getWriter().close();
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
