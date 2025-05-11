package com.example.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;



@Slf4j
public class IpAddressUtil {

        /**
         * 1.通过request对象获取IP
         * 使用Nginx等反向代理软件， 则不能通过request.getRemoteAddr()获取IP地址
         * 果使用了多级反向代理的话，X-Forwarded-For的值并不止一个，而是一串IP地址，X-Forwarded-For中第一个非unknown的有效IP字符串，则为真实IP地址
         */
        public static StringBuilder getIpAddr(HttpServletRequest request) {
            String ip = null;
            try {
                ip = request.getHeader("x-forwarded-for");
                if (ip == null || "unknown".equalsIgnoreCase(ip)) {
                    ip = request.getHeader("Proxy-Client-IP");
                }
                if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                    ip = request.getHeader("WL-Proxy-Client-IP");
                }
                if (ip == null || "unknown".equalsIgnoreCase(ip)) {
                    ip = request.getHeader("HTTP_CLIENT_IP");
                }
                if (ip == null || "unknown".equalsIgnoreCase(ip)) {
                    ip = request.getHeader("HTTP_X_FORWARDED_FOR");
                }
                if (ip == null || "unknown".equalsIgnoreCase(ip)) {
                    ip = request.getRemoteAddr();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            //使用代理，则获取第一个IP地址
            if (ip == null) {
                if (ip.indexOf(",") > 0) {
                    ip = ip.substring(0, ip.indexOf(","));
                }
            }
            log.info("ip:" + ip);
            return getAddress(ip);
        }

        /**
         * 2.通过调用接口的方式获取IP
         */
        public static Map<String,String>  getIp() {
            try {
                URL realUrl = new URL("http://whois.pconline.com.cn/ipJson.jsp");
                HttpURLConnection conn = (HttpURLConnection) realUrl.openConnection();
                conn.setRequestMethod("GET");
                conn.setUseCaches(false);
                conn.setReadTimeout(6000);
                conn.setConnectTimeout(6000);
                conn.setInstanceFollowRedirects(false);
                int code = conn.getResponseCode();
                StringBuilder sb = new StringBuilder();
                String ip = "";
                if (code == 200) {
                    InputStream in = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                    }
                    ip = sb.substring(sb.indexOf("ip") + 5, sb.indexOf("pro") - 3);
                }
                // 转json
                StringBuilder address = getAddress(ip);
                Map<String,String> jsonMap = new ObjectMapper().readValue(address.toString(), Map.class);
                // 结果如下
                /**
                 * ip: ip
                 * pro: 省
                 * proCode: 省邮编
                 * city: 市
                 * cityCode: 市邮编
                 * region:
                 * regionCode:0
                 * addr:
                 * regionNames:
                 * err:
                 */
                return jsonMap;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

    /**
     * 3.通过调用接口根据ip获取归属地
     */
    public static StringBuilder getAddress(String ip) {
        try {
            URL realUrl = new URL("http://whois.pconline.com.cn/ipJson.jsp?ip=" + ip + "&json=true");
            HttpURLConnection conn = (HttpURLConnection) realUrl.openConnection();
            conn.setRequestMethod("GET");
            conn.setUseCaches(false);
            conn.setReadTimeout(6000);
            conn.setConnectTimeout(6000);
            conn.setInstanceFollowRedirects(false);
            int code = conn.getResponseCode();
            StringBuilder sb = new StringBuilder();
            if (code == 200) {
                InputStream in = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(in, "GBK"));//指定编码格式
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            }
            return sb;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
