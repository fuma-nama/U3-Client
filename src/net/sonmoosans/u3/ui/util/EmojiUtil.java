package net.sonmoosans.u3.ui.util;

import net.sonmoosans.u3.api.core.APICaller;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EmojiUtil {
    public static String getEmojiUrl(int emojiID) {
        return APICaller.webURL + "images/emoji/" + emojiID;
    }

    public static String parseEmoji(String html) {

        Matcher m = Pattern.compile("\\{\\:([0-9]+)\\:\\}").matcher(html);

        StringBuilder result = new StringBuilder();

        while (m.find()) {
            int emojiID = Integer.parseInt(m.group(1));
            m.appendReplacement(result, getImgHtml(getEmojiUrl(emojiID)));
        }

        m.appendTail(result);

        return result.toString();
    }

    private static String getImgHtml(String url) {
        return "<img src=\"" + url + "\" width=\"30\" height=\"30\" />";
    }
}
