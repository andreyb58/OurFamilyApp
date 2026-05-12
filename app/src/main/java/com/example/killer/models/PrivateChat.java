package com.example.killer.models;

public class PrivateChat {
    private String id;
    private String[] participantIds;
    private String[] participantNames;
    private String lastMessage;
    private long lastMessageTime;
    private int unreadCount;

    public PrivateChat() {
        // Пустой конструктор для Firebase
    }

    // Геттеры и сеттеры
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String[] getParticipantIds() { return participantIds; }
    public void setParticipantIds(String[] participantIds) { this.participantIds = participantIds; }

    public String[] getParticipantNames() { return participantNames; }
    public void setParticipantNames(String[] participantNames) { this.participantNames = participantNames; }

    public String getLastMessage() { return lastMessage; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }

    public long getLastMessageTime() { return lastMessageTime; }
    public void setLastMessageTime(long lastMessageTime) { this.lastMessageTime = lastMessageTime; }

    public int getUnreadCount() { return unreadCount; }
    public void setUnreadCount(int unreadCount) { this.unreadCount = unreadCount; }
}