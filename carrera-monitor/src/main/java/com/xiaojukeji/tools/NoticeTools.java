package com.xiaojukeji.tools;

import com.xiaojukeji.protocol.ChopperConfiguration;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.lang.Throwable;

public class NoticeTools {
    private static final Logger logger = LoggerFactory.getLogger(NoticeTools.class);

    private static Set<String> distinct(List<String> reviewers) {
        return new HashSet<>(reviewers);
    }

    public static boolean sendEmail(String group, String context) {
        try {
            boolean warningOn = ChopperConfiguration.warningOn();
            if (! warningOn) {
                logger.info("send email warning off");
                return false;
            }

            HtmlEmail email = new HtmlEmail();
            email.setHostName(ChopperConfiguration.mailServerHost());
            email.setSmtpPort(ChopperConfiguration.mailServerPort());
            //set charset
            email.setCharset("UTF-8");
            // SSL verification
            email.setSSLOnConnect(ChopperConfiguration.mailSmtpSslEnable());
            List<String> receivers = ChopperConfiguration.mailReceivers(group);
            for (String receiver : distinct(receivers)) {
                email.addTo(receiver);
            }
            email.setFrom(ChopperConfiguration.mailUsername(), ChopperConfiguration.mailNickname());
            email.setAuthentication(ChopperConfiguration.mailUsername(), ChopperConfiguration.mailPassword());
            email.setSubject(ChopperConfiguration.mailSubject(group));
            email.setMsg(makeBody(Collections.singletonList(context)));
            // send
            email.send();

            return true;
        } catch (EmailException e) {
            logger.debug("send email error, context:{}", context, e);
        } catch (Throwable e) {
            logger.error("send email error, context:{}", context, e);
        }
        return false;
    }

    /**
     * 将元素 添加到html页面的 body中
     *
     * @param elements
     * @return
     */
    public static String makeBody(List<String> elements) {
        StringBuilder body = new StringBuilder();
        body.append(getHead());
        body.append("<body>");
        for (String ele : elements) {
            body.append(ele);
        }

        body.append("</body>");
        body.append(getNoReplyNotice());

        return body.toString();
    }

    /**
     * 加入了table 的样式
     *
     * @return
     */
    private static String getHead() {
        return "<head>\n" +
                "<style>\n" +
                "#customers {\n" +
                "    font-family: \"Trebuchet MS\", Arial, Helvetica, sans-serif;\n" +
                "    border-collapse: collapse;\n" +
                "    width: 100%;\n" +
                "}\n" +
                "\n" +
                "#customers td, #customers th {\n" +
                "    border: 1px solid #ddd;\n" +
                "    padding: 8px;\n" +
                "}\n" +
                "\n" +
                "#customers tr:nth-child(even){background-color: #f2f2f2;}\n" +
                "\n" +
                "#customers tr:hover {background-color: #ddd;}\n" +
                "\n" +
                "#customers th {\n" +
                "    padding-top: 12px;\n" +
                "    padding-bottom: 12px;\n" +
                "    text-align: left;\n" +
                "    background-color: #545b00;\n" +
                "    color: white;\n" +
                "}\n" +
                "</style>\n" +
                "</head>";
    }

    /**
     * 生成 url链接
     *
     * @param url  链接
     * @param text 链接上显示文本
     * @return
     */
    public static String getLink(String url, String text) {
        return String.format("<a href=\"%s\" style=\"color:red;\" target=\"_blank\">%s</a>", url, text);
    }

    /**
     * 邮件勿回复提示
     *
     * @return
     */
    public static String getNoReplyNotice() {
        return "<div style=\"margin:50px 0 0 20px; font-size:14px;\">\n" +
                "    <i>注意：该邮件由系统自动发送，请勿回复！</i>\n" +
                "</div>\n";
    }
}
