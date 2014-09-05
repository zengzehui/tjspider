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
		String docHtml = doc.html();
		
		response.setContentType("text/html; charset=utf-8");
		response.getWriter().write(docHtml);
		response.getWriter().close();
		return null;
	}
	
	@RequestMapping(value = "/gb2312", method = RequestMethod.GET)
	public @ResponseBody String getPageGBK(@RequestParam("url") String url, HttpServletResponse response)
			throws IOException {
		logger.info("------------------------------");
		logger.info("url = " + url);
		Document doc = Jsoup.parse(new URL(url), 60*1000);
		
		response.setContentType("text/html; charset=utf-8");
		response.getWriter().write(new String(doc.html().getBytes("gb2312"), "utf-8"));
		response.getWriter().close();
		return null;
	}
}
