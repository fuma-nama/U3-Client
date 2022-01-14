package net.sonmoosans.u3.api.model;

import java.time.LocalDateTime;

public class TextMessage {
    public Integer ID, groupID, senderID;
    public String context;
    public String[] file;
    public LocalDateTime date;
    public boolean edited, isHTML;
    public TextMessage replyTo;
}