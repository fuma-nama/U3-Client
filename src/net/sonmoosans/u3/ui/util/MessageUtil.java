package net.sonmoosans.u3.ui.util;

import com.github.rjeschke.txtmark.Configuration;
import com.github.rjeschke.txtmark.Processor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageUtil {
    public static String getTimeString(LocalDateTime time) {
        return LocalDate.now().getDayOfYear() == time.getDayOfYear()?
                "Today " + time.getHour() + ":" + getMinuteString(time.getMinute())
                :
                time.getDayOfMonth() + "/" + time.getMonthValue() + "/" + time.getYear();
    }

    private static String getMinuteString(int minute) {
        return minute >= 10? String.valueOf(minute) : "0" + minute;
    }

    public static String markdownToHtml(String md) {
        return "<html>" + Processor.process(EmojiUtil.parseEmoji(parseLinks(md))).replace("\n", "<br>") + "</html>";
    }

    public static String addHtmlTag(String html) {
        return "<html>" + html + "</html>";
    }

    private static String parseLinks(String s) {
        Pattern pattern = Pattern.compile(
                "(?<!\\[video\\])(?:https|http)://([^\\s\\|]+)(?!\\[video\\])");

        Matcher m = pattern.matcher(s);

        StringBuilder sb = new StringBuilder();
        while (m.find())
            m.appendReplacement(sb, "<a href=\""+m.group()+"\">"+m.group() +"</a>");
        m.appendTail(sb);

        return sb.toString();
    }
}
