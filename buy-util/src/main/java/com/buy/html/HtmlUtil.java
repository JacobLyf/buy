package com.buy.html;

import java.util.regex.Pattern;

import com.buy.string.StringUtil;

public class HtmlUtil {

	public static String html2Text(String inputString) {
		if (StringUtil.isBlank(inputString)) {
			return inputString;
		}
		String htmlStr = inputString; // 含html标签的字符串.
		String textStr = "";
		java.util.regex.Pattern p_script;
		java.util.regex.Matcher m_script;
		java.util.regex.Pattern p_style;
		java.util.regex.Matcher m_style;
		java.util.regex.Pattern p_html;
		java.util.regex.Matcher m_html;

		java.util.regex.Pattern p_html1;
		java.util.regex.Matcher m_html1;

		try {
			String regEx_script = "<script[^>]*?>[\\s\\S]*?<\\/script>"; // 定义script的正则表达式{或<script[^>]*?>[//s//S]*?<///script>
			String regEx_style = "<style[^>]*?>[\\s\\S]*?<\\/style>"; // 定义style的正则表达式{或<style[^>]*?>[//s//S]*?<///style>
			String regEx_html1 = "<[^>]+>"; // 定义HTML标签的正则表达式
			String regEx_html2 = "<[^>]+";
			p_script = Pattern.compile(regEx_script, Pattern.CASE_INSENSITIVE);
			m_script = p_script.matcher(htmlStr);
			htmlStr = m_script.replaceAll(""); // 过滤script标签.

			p_style = Pattern.compile(regEx_style, Pattern.CASE_INSENSITIVE);
			m_style = p_style.matcher(htmlStr);
			htmlStr = m_style.replaceAll(""); // 过滤style标签.

			p_html = Pattern.compile(regEx_html1, Pattern.CASE_INSENSITIVE);
			m_html = p_html.matcher(htmlStr);
			htmlStr = m_html.replaceAll(" "); // 过滤html标签.

			p_html1 = Pattern.compile(regEx_html2, Pattern.CASE_INSENSITIVE);
			m_html1 = p_html1.matcher(htmlStr);
			htmlStr = m_html1.replaceAll(" "); // 过滤html标签.

			textStr = htmlStr.replaceAll(" ", "");

		} catch (Exception e) {
			System.err.println("Html2Text: " + e.getMessage());
		}

		return textStr;
	}

	public static boolean is_html(String inputString) {
		if (StringUtil.isBlank(inputString)) {
			return false;
		}
		String htmlStr = inputString; // 含html标签的字符串.
		java.util.regex.Pattern p_script;
		java.util.regex.Matcher m_script;
		java.util.regex.Pattern p_style;
		java.util.regex.Matcher m_style;
		java.util.regex.Pattern p_html;
		java.util.regex.Matcher m_html;

		java.util.regex.Pattern p_html1;
		java.util.regex.Matcher m_html1;

		try {
			String regEx_script = "<script[^>]*?>[\\s\\S]*?<\\/script>"; // 定义script的正则表达式{或<script[^>]*?>[//s//S]*?<///script>
			String regEx_style = "<style[^>]*?>[\\s\\S]*?<\\/style>"; // 定义style的正则表达式{或<style[^>]*?>[//s//S]*?<///style>
			String regEx_html1 = "<[^>]+>"; // 定义HTML标签的正则表达式.
			String regEx_html2 = "<[^>]+";
			p_script = Pattern.compile(regEx_script, Pattern.CASE_INSENSITIVE);
			m_script = p_script.matcher(htmlStr);
			if (m_script.find()) {
				return true;
			}
			htmlStr = m_script.replaceAll(""); // 过滤script标签.

			p_style = Pattern.compile(regEx_style, Pattern.CASE_INSENSITIVE);
			m_style = p_style.matcher(htmlStr);
			if (m_style.find()) {
				return true;
			}
			htmlStr = m_style.replaceAll(""); // 过滤style标签.

			p_html = Pattern.compile(regEx_html1, Pattern.CASE_INSENSITIVE);
			m_html = p_html.matcher(htmlStr);
			if (m_html.find()) {
				return true;
			}
			htmlStr = m_html.replaceAll(" "); // 过滤html标签.

			p_html1 = Pattern.compile(regEx_html2, Pattern.CASE_INSENSITIVE);
			m_html1 = p_html1.matcher(htmlStr);
			if (m_html1.find()) {
				return true;
			}
		} catch (Exception e) {
			System.err.println("Html2Text: " + e.getMessage());
		}

		return false;
	}

}