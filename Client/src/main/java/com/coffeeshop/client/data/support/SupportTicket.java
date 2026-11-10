package com.coffeeshop.client.data.support;

public class SupportTicket {
    private final String id;
    private final TicketTopic topic;
    private final String message;
    private final String createdAt;
    private final String status;

    public SupportTicket(String id, TicketTopic topic, String message, String createdAt, String status) {
        this.id = id;
        this.topic = topic;
        this.message = message;
        this.createdAt = createdAt;
        this.status = status != null ? status : "Đã tiếp nhận";
    }

    public SupportTicket(String id, TicketTopic topic, String message, String createdAt) {
        this(id, topic, message, createdAt, "Đã tiếp nhận");
    }

    public String getId() {
        return id;
    }

    public TicketTopic getTopic() {
        return topic;
    }

    public String getMessage() {
        return message;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getStatus() {
        return status;
    }
}
