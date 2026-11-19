package com.coffeeshop.client.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.coffeeshop.client.data.support.FaqItem;
import com.coffeeshop.client.data.support.SupportRepository;
import com.coffeeshop.client.data.support.SupportTicket;
import com.coffeeshop.client.data.support.TicketTopic;

import java.util.List;

public class SupportViewModel extends ViewModel {
    private final MutableLiveData<TicketTopic> selectedTopic = new MutableLiveData<>(null);
    private final MutableLiveData<String> messageText = new MutableLiveData<>("");
    private final MutableLiveData<Integer> expandedFaqIndex = new MutableLiveData<>(-1);
    private final LiveData<List<SupportTicket>> tickets = SupportRepository.getInstance().getTicketsLiveData();
    private final List<FaqItem> faqs = FaqItem.getDefaultFaqs();

    public LiveData<TicketTopic> getSelectedTopic() {
        return selectedTopic;
    }

    public void setSelectedTopic(TicketTopic topic) {
        if (selectedTopic.getValue() == topic) {
            selectedTopic.setValue(null);
        } else {
            selectedTopic.setValue(topic);
        }
    }

    public LiveData<String> getMessageText() {
        return messageText;
    }

    public void setMessageText(String text) {
        messageText.setValue(text != null ? text : "");
    }

    public LiveData<Integer> getExpandedFaqIndex() {
        return expandedFaqIndex;
    }

    public void toggleFaqIndex(int index) {
        Integer current = expandedFaqIndex.getValue();
        if (current != null && current == index) {
            expandedFaqIndex.setValue(-1);
        } else {
            expandedFaqIndex.setValue(index);
        }
    }

    public LiveData<List<SupportTicket>> getTickets() {
        return tickets;
    }

    public List<FaqItem> getFaqs() {
        return faqs;
    }

    public boolean canSubmit() {
        TicketTopic topic = selectedTopic.getValue();
        String msg = messageText.getValue();
        return topic != null && msg != null && msg.trim().length() >= 10 && msg.trim().length() <= 500;
    }

    public boolean submitTicket() {
        if (!canSubmit()) return false;
        TicketTopic topic = selectedTopic.getValue();
        String msg = messageText.getValue();
        if (msg == null) msg = "";
        SupportRepository.getInstance().submit(topic, msg.trim());
        selectedTopic.setValue(null);
        messageText.setValue("");
        return true;
    }
}
