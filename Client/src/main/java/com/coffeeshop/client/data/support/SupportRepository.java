package com.coffeeshop.client.data.support;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SupportRepository {
    private static SupportRepository instance;
    private final List<SupportTicket> ticketList = new ArrayList<>();
    private final MutableLiveData<List<SupportTicket>> ticketsLiveData = new MutableLiveData<>(Collections.emptyList());
    private int ticketCounter = 1;

    private SupportRepository() { }

    public static synchronized SupportRepository getInstance() {
        if (instance == null) {
            instance = new SupportRepository();
        }
        return instance;
    }

    public LiveData<List<SupportTicket>> getTicketsLiveData() {
        return ticketsLiveData;
    }

    public List<SupportTicket> getTickets() {
        return new ArrayList<>(ticketList);
    }

    public synchronized void submit(TicketTopic topic, String message) {
        String id = "#TK" + String.format(Locale.getDefault(), "%04d", ticketCounter++);
        String createdAt = new SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault()).format(new Date());
        SupportTicket ticket = new SupportTicket(id, topic, message, createdAt, "Đã tiếp nhận");
        ticketList.add(0, ticket);
        ticketsLiveData.setValue(new ArrayList<>(ticketList));
    }
}
