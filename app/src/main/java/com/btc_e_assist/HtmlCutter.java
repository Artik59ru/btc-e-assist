package com.btc_e_assist;

import android.annotation.SuppressLint;

import com.assist.Mirrors;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HtmlCutter {
    public final static String LANG_RU = "ru";
    public final static String LANG_EN = "en";
    public final static String LANG_CN = "cn";
    private final static int TIMEOUT_MILLIS = 20000;
    private final static String REG_EXP_COOKIE = "\\w{30,}+";
    private final static String REG_EXP_CHART = "\\[\\[.*\\]\\]";
    public static volatile ArrayList<HashMap<String, Object>> chatData = new ArrayList<HashMap<String, Object>>();
    public static volatile ArrayList<HashMap<String, Object>> newsData = new ArrayList<HashMap<String, Object>>();
    public static volatile ArrayList<Double> chartPriceData = new ArrayList<Double>();
    public static volatile ArrayList<String> chartTimeData = new ArrayList<String>();
    public static volatile String news = "";
    private static volatile int lastMessageHash = 0;
    private static String currentLanguage = "";
    private static int newsCount = 10;
    private static int cookieDetectionLimitLength = 1000;

    private static String cookie = "";

    private static Pattern cookiePattern;
    private static Pattern chartPattern;
    private static Document fullHtml;


    public static void setLanguage(String lang) {
        currentLanguage = lang;
    }

    public static void setNewsCount(int cn) {
        newsCount = cn;
    }

    private static void getHtmlPage(String target) throws IOException {
        if (cookiePattern == null) {
            cookiePattern = Pattern.compile(REG_EXP_COOKIE);
        }
        Connection connection = Jsoup.connect(target);
        fullHtml = connection.cookie("a", cookie)
                .cookie("locale", currentLanguage).timeout(TIMEOUT_MILLIS)
                .get();
        if (fullHtml.toString().length() < cookieDetectionLimitLength) {
            String scriptData = fullHtml.getElementsByTag("script").get(0)
                    .data();
            Matcher matcher = cookiePattern.matcher(scriptData);
            matcher.find();
            cookie = matcher.group();
            fullHtml = Jsoup.connect(target).cookie("a", cookie)
                    .cookie("locale", currentLanguage).get();
        }
    }

    /**
     * Load page with chat
     *
     * @return false if has ANY trouble
     */
    public static boolean loadChatData() {
        try {
            getHtmlPage(Mirrors.getMirror());
            return true;
        } catch (Exception e) {
        }
        return false;
    }

    /**
     * Run the process of packing html data to the chatData. Sometimes has empty
     * map with "checked" key with value "1" for checked messages.
     *
     * @return false if has ANY trouble
     */
    public static boolean setChatData() {
        try {
            Elements classElements = fullHtml.getElementsByClass("chatmessage");
            String message = "";
            int count = 0;
            int lastPosition = Integer.MIN_VALUE;
            int hash = 0;
            chatData.clear();
            for (Element s : classElements) {
                HashMap<String, Object> itemMap = new HashMap<String, Object>();
                itemMap.put("nickname", s.getElementsByTag("a").text());
                message = s.getElementsByTag("span").text();
                if (lastMessageHash != 0) {
                    hash = message.hashCode();
                    if (hash == lastMessageHash) {
                        lastPosition = count;
                    }
                }
                itemMap.put("message", message);
                chatData.add(itemMap);
                count++;
            }
            if (hash != 0) {
                lastMessageHash = hash;
            } else {
                lastMessageHash = message.hashCode();
            }
            if (lastPosition != Integer.MIN_VALUE) {
                HashMap<String, Object> itemMap = new HashMap<String, Object>();
                itemMap.put("checked", "1");
                chatData.add(lastPosition + 1, itemMap);
            }
            return true;
        } catch (Exception e) {
        }
        return false;
    }

    /**
     * Load page with news list
     *
     * @return false if has ANY trouble
     */
    public static boolean loadNewsList() {
        try {
            getHtmlPage(Mirrors.getMirror() + "/news");
            return true;
        } catch (Exception e) {
        }
        return false;
    }

    /**
     * Run the process of packing html to the newsData
     *
     * @return false if has ANY trouble
     */
    public static boolean setNewsList() {
        try {
            Elements trElements = fullHtml.getElementsByTag("tr");
            newsData.clear();
            int count = 0;
            for (Element s : trElements) {
                count++;
                if (count > newsCount)
                    break;
                HashMap<String, Object> itemMap = new HashMap<String, Object>();
                Element link = s.select("a").first();
                Element date = s.select("b").first();
                itemMap.put("link", link.attr("href"));
                itemMap.put("text", link.text());
                itemMap.put("date", date.text());
                newsData.add(itemMap);
            }
            return true;
        } catch (Exception e) {
        }
        return false;
    }

    /**
     * Run the process of getting news content using link received from newsData
     *
     * @param index
     * @return false if has ANY trouble
     */
    public static boolean runGettingNewsContent(int index) {
        try {
            getHtmlPage(newsData.get(index).get("link").toString());
            Elements pElements = fullHtml.getElementsByClass("block");
            Elements divElements = pElements.get(1).select("div");
            Element elem = divElements.get(1);
            String newsDataHtml = elem.html();
            newsDataHtml = newsDataHtml.replaceAll("<p>", "BRN");
            newsDataHtml = newsDataHtml.replaceAll("</p>", "BRN");
            newsDataHtml = newsDataHtml.replaceAll("<br>", "BRN");
            newsDataHtml = newsDataHtml.replaceAll("<br />", "BRN");
            newsDataHtml = newsDataHtml.replaceAll("<br/>", "BRN");
            newsDataHtml = newsDataHtml.replaceAll("</br>", "BRN");
            fullHtml = Jsoup.parse(newsDataHtml);
            news = fullHtml.text().replace("BRN", "\n");
            news = news.replaceFirst(".*\\n", "").replaceFirst(" ", "");
            if (news != null && news.length() > 0) {
                return true;
            }
        } catch (Exception e) {
        }
        return false;
    }

    /**
     * Load page with pair graph
     *
     * @return false if has ANY trouble
     */
    @SuppressLint("DefaultLocale")
    public static boolean loadChartData(String pairName) {
        try {
            StringBuilder compositTarget = new StringBuilder(Mirrors.getMirror());
            compositTarget.append("/exchange/");
            compositTarget.append(pairName.replace('-', '_').toLowerCase());
            if (chartPattern == null) {
                chartPattern = Pattern.compile(REG_EXP_CHART);
            }
            getHtmlPage(compositTarget.toString());
            return true;
        } catch (Exception e) {
        }
        return false;
    }

    /**
     * Run the process of packing html to the chartData
     *
     * @return false if has ANY trouble
     */
    @SuppressLint("DefaultLocale")
    public static boolean setChartData() {
        try {
            String scriptData = fullHtml.getElementsByTag("script").get(4).html();
            if (scriptData.length() == 0) {
                return false;
            }
            Matcher matcher = chartPattern.matcher(scriptData);
            matcher.find();
            String data = matcher.group();
            String[] priceArray = data.split(",");
            chartPriceData.clear();
            chartTimeData.clear();
            Double tempPrice = 0.0;
            int count = 0;
            String tempTime = "";
            for (String s : priceArray) {
                if (count == 5) {
                    count = 0;
                    continue;
                }
                if (count == 0) {
                    tempTime = s.replace("\"", "").replace("[", "");
                } else if (count == 2) {
                    tempPrice = Double.parseDouble(s);
                } else if (count == 3) {
                    tempPrice = (tempPrice + Double.parseDouble(s)) / 2;
                    chartPriceData.add(tempPrice);
                    chartTimeData.add(tempTime);
                }
                count++;
            }

            if (chartPriceData.size() == chartTimeData.size()
                    && !chartPriceData.isEmpty()) {
                return true;
            }
        } catch (Exception e) {
        }
        return false;
    }
}