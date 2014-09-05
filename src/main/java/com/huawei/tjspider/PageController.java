package com.huawei.tjspider;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
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
		String docHtml = doc.html();

		response.setContentType("text/html; charset=utf-8");
		response.getWriter().write(docHtml);
		response.getWriter().close();
		return null;
	}

	@RequestMapping(value = "/gb2312", method = RequestMethod.GET)
	public @ResponseBody String getPageGBK1(@RequestParam("url") String url, HttpServletResponse response)
			throws IOException {
		logger.info("------------------------------");
		logger.info("url = " + url);
		Document doc = Jsoup.parse(new URL(url), 60 * 1000);
		logger.info("111111111111111111111111111\n" + doc.html());
		logger.info("222222222222222222222222222\n" + new String(doc.html().getBytes("gb2312"), "utf-8"));

		response.setContentType("text/html; charset=utf-8");
		response.getWriter().write(new String(doc.html().getBytes("gb2312"), "utf-8"));
		response.getWriter().close();
		return null;
	}

	@RequestMapping(value = "/gb23122", method = RequestMethod.GET)
	public @ResponseBody String getPageGBK2(@RequestParam("url") String url, HttpServletResponse response)
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
		logger.info("111111111111111111111111111\n" + srcHtml);
		logger.info("222222222222222222222222222\n" + new String(srcHtml.getBytes("gb2312"), "utf-8"));

		response.setContentType("text/html; charset=utf-8");
		response.getWriter().write(new String(srcHtml.getBytes("gb2312"), "utf-8"));
		response.getWriter().close();
		return null;
	}

	@RequestMapping(value = "/gb23123", method = RequestMethod.GET)
	public @ResponseBody String getPageGBK3(@RequestParam("url") String url, HttpServletResponse response)
			throws IOException {
		logger.info("------------------------------");
		logger.info("url = " + url);

		Document doc = Jsoup.parse(new URL(url).openStream(), "gb2312", url);

		response.setContentType("text/html; charset=utf-8");
		response.getWriter().write(doc.html());
		response.getWriter().close();
		return null;
	}

	@RequestMapping(value = "/gbk", method = RequestMethod.GET)
	public @ResponseBody String getPageGBK(@RequestParam("url") String url, HttpServletResponse response)
			throws IOException {
		logger.info("------------------------------");
		logger.info("url = " + url);

		Document doc = Jsoup.parse(new URL(url).openStream(), "gbk", url);

		response.setContentType("text/html; charset=utf-8");
		response.getWriter().write(doc.html());
		response.getWriter().close();
		return null;
	}

	@RequestMapping(value = "/get/{charset}", method = RequestMethod.GET)
	public @ResponseBody String getPageUTF8(@PathVariable String charset, @RequestParam("url") String url, HttpServletResponse response)
			throws IOException {
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
			// charset大小写问题遗留
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
