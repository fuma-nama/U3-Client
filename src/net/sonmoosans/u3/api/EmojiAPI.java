package net.sonmoosans.u3.api;

import net.sonmoosans.u3.api.core.APICaller;
import net.sonmoosans.u3.api.core.Parameter;
import net.sonmoosans.u3.api.model.Emoji;
import net.sonmoosans.u3.api.model.Result;

import java.io.File;

public class EmojiAPI {
    public static Result<Emoji[]> getEmojis(int offset) {
        return APICaller.callGET(Emoji[].class, "emoji",
                Parameter.intValue("offset", offset)
        );
    }

    public static Result<Emoji[]> getEmojis(int offset, int creatorID) {
        return APICaller.callGET(Emoji[].class, "emoji",
                Parameter.intValue("creator", creatorID),
                Parameter.intValue("offset", offset)
        );
    }

    public static Result<Emoji[]> getEmojis(int offset, String name) {
        return APICaller.callGET(Emoji[].class, "emoji",
                new Parameter("name", name),
                Parameter.intValue("offset", offset)
        );
    }

    public static boolean addEmoji(String name, File image) {
        return APICaller.callPOSTFile(image, "emoji",
                new Parameter("name", name));
    }

    public static boolean saveEmoji(int emojiID) {
        return APICaller.callPOST("emoji/save",
                Parameter.intValue("id", emojiID)
        );
    }

    public static boolean unSaveEmoji(int emojiID) {
        return APICaller.callDELETE("emoji/save",
                Parameter.intValue("id", emojiID)
        );
    }

    public static Result<int[]> getSavedEmojis() {
        return APICaller.callGET(int[].class, "emoji/save");
    }

    public static boolean deleteEmoji(int emojiID) {
        return APICaller.callDELETE("emoji",
                Parameter.intValue("id", emojiID));
    }
}
