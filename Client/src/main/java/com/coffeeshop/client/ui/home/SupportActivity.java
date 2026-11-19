package com.coffeeshop.client.ui.home;

import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.coffeeshop.client.R;
import com.coffeeshop.client.data.support.FaqItem;
import com.coffeeshop.client.data.support.SupportInfo;
import com.coffeeshop.client.data.support.SupportTicket;
import com.coffeeshop.client.data.support.TicketTopic;
import com.coffeeshop.client.ui.common.FontUtils;
import com.coffeeshop.client.ui.common.SystemInsets;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.snackbar.Snackbar;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SupportActivity extends AppCompatActivity {
    private SupportViewModel viewModel;
    private ChipGroup chipGroupTopics;
    private EditText edtTicketMessage;
    private Button btnSubmitTicket;
    private LinearLayout faqContainer;
    private LinearLayout myTicketsContainer;
    private View sectionMyTickets;
    private View supportStatusDot;
    private TextView supportStatusText;

    private final Map<Integer, TicketTopic> chipIdToTopicMap = new HashMap<>();
    private final Map<TicketTopic, Integer> topicToChipIdMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_support);
        SystemInsets.apply(this);
        FontUtils.applyInter(findViewById(android.R.id.content));

        viewModel = new ViewModelProvider(this).get(SupportViewModel.class);

        TextView title = findViewById(R.id.supportTitle);
        if (title != null) {
            title.setTypeface(FontUtils.lora(this, 600));
        }

        ImageButton backBtn;
        backBtn = findViewById(R.id.supportBack);
        if (backBtn != null) {
            backBtn.setOnClickListener(v -> finish());
        }

        initViews();
        setupStatusHeader();
        setupQuickContact();
        setupFaqSection();
        setupTicketSubmissionSection();
        setupMyTicketsSection();
    }

    private void initViews() {
        supportStatusDot = findViewById(R.id.supportStatusDot);
        supportStatusText = findViewById(R.id.supportStatusText);
        faqContainer = findViewById(R.id.faqContainer);
        chipGroupTopics = findViewById(R.id.chipGroupTopics);
        edtTicketMessage = findViewById(R.id.edtTicketMessage);
        btnSubmitTicket = findViewById(R.id.btnSubmitTicket);
        sectionMyTickets = findViewById(R.id.sectionMyTickets);
        myTicketsContainer = findViewById(R.id.myTicketsContainer);

        chipIdToTopicMap.put(R.id.chipOrder, TicketTopic.ORDER);
        chipIdToTopicMap.put(R.id.chipPayment, TicketTopic.PAYMENT);
        chipIdToTopicMap.put(R.id.chipVoucher, TicketTopic.VOUCHER);
        chipIdToTopicMap.put(R.id.chipFeedback, TicketTopic.FEEDBACK);
        chipIdToTopicMap.put(R.id.chipOther, TicketTopic.OTHER);

        for (Map.Entry<Integer, TicketTopic> entry : chipIdToTopicMap.entrySet()) {
            topicToChipIdMap.put(entry.getValue(), entry.getKey());
        }
    }

    private void setupStatusHeader() {
        boolean isOpen = SupportInfo.isSupportOpen();
        if (supportStatusDot != null) {
            supportStatusDot.setBackgroundResource(isOpen ? R.drawable.bg_status_dot_green : R.drawable.bg_status_dot_gray);
        }
        if (supportStatusText != null) {
            if (isOpen) {
                supportStatusText.setText(getString(R.string.support_status_open, "22:00"));
            } else {
                supportStatusText.setText(getString(R.string.support_status_closed, "07:00"));
            }
        }
    }

    private void setupQuickContact() {
        TextView txtHotlineDesc = findViewById(R.id.txtHotlineDesc);
        if (txtHotlineDesc != null) {
            txtHotlineDesc.setText(getString(R.string.support_hotline_desc, SupportInfo.HOTLINE, SupportInfo.HOURS));
        }

        TextView txtEmailDesc = findViewById(R.id.txtEmailDesc);
        if (txtEmailDesc != null) {
            txtEmailDesc.setText(SupportInfo.EMAIL);
        }

        TextView txtFanpageDesc = findViewById(R.id.txtFanpageDesc);
        if (txtFanpageDesc != null) {
            txtFanpageDesc.setText(SupportInfo.FANPAGE_NAME);
        }

        TextView txtAddressDesc = findViewById(R.id.txtAddressDesc);
        if (txtAddressDesc != null) {
            txtAddressDesc.setText(SupportInfo.ADDRESS);
        }

        // Action Dial (Hotline)
        View rowHotline = findViewById(R.id.rowHotline);
        if (rowHotline != null) {
            rowHotline.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + SupportInfo.HOTLINE_RAW));
                safeStartActivity(intent);
            });
        }
        View btnCopyHotline = findViewById(R.id.btnCopyHotline);
        if (btnCopyHotline != null) {
            btnCopyHotline.setOnClickListener(v -> copyToClipboard(SupportInfo.HOTLINE));
        }

        // Action SendTo (Email)
        View rowEmail = findViewById(R.id.rowEmail);
        if (rowEmail != null) {
            rowEmail.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:" + SupportInfo.EMAIL));
                intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.support_email_subject));
                safeStartActivity(intent);
            });
        }
        View btnCopyEmail = findViewById(R.id.btnCopyEmail);
        if (btnCopyEmail != null) {
            btnCopyEmail.setOnClickListener(v -> copyToClipboard(SupportInfo.EMAIL));
        }

        // Action View (Fanpage)
        View rowFanpage = findViewById(R.id.rowFanpage);
        if (rowFanpage != null) {
            rowFanpage.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(SupportInfo.FANPAGE_URL));
                safeStartActivity(intent);
            });
        }
        View btnCopyFanpage = findViewById(R.id.btnCopyFanpage);
        if (btnCopyFanpage != null) {
            btnCopyFanpage.setOnClickListener(v -> copyToClipboard(SupportInfo.FANPAGE_URL));
        }

        // Action View Map (Trụ sở)
        View rowAddress = findViewById(R.id.rowAddress);
        if (rowAddress != null) {
            rowAddress.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=" + Uri.encode(SupportInfo.ADDRESS)));
                safeStartActivity(intent);
            });
        }
        View btnCopyAddress = findViewById(R.id.btnCopyAddress);
        if (btnCopyAddress != null) {
            btnCopyAddress.setOnClickListener(v -> copyToClipboard(SupportInfo.ADDRESS));
        }
    }

    private void safeStartActivity(Intent intent) {
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            showSnackbar(getString(R.string.support_no_app_toast));
        }
    }

    private void copyToClipboard(String content) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null) {
            ClipData clip = ClipData.newPlainText("SupportInfo", content);
            clipboard.setPrimaryClip(clip);
            showSnackbar(getString(R.string.support_copied_toast, content));
        }
    }

    private void setupFaqSection() {
        List<FaqItem> faqs = viewModel.getFaqs();
        LayoutInflater inflater = LayoutInflater.from(this);
        if (faqContainer != null) {
            faqContainer.removeAllViews();
            for (int i = 0; i < faqs.size(); i++) {
                final int index = i;
                FaqItem item = faqs.get(i);
                View itemView = inflater.inflate(R.layout.item_faq, faqContainer, false);
                TextView qText = itemView.findViewById(R.id.faqQuestionText);
                TextView aText = itemView.findViewById(R.id.faqAnswerText);

                qText.setText(item.getQuestion());
                aText.setText(item.getAnswer());

                itemView.setOnClickListener(v -> viewModel.toggleFaqIndex(index));
                faqContainer.addView(itemView);
            }
        }

        viewModel.getExpandedFaqIndex().observe(this, expandedIndex -> {
            if (faqContainer == null) return;
            for (int i = 0; i < faqContainer.getChildCount(); i++) {
                View itemView = faqContainer.getChildAt(i);
                View answerContainer = itemView.findViewById(R.id.faqAnswerContainer);
                ImageView arrow = itemView.findViewById(R.id.faqArrowIcon);
                boolean isExpanded = expandedIndex != null && expandedIndex == i;
                if (answerContainer != null) {
                    answerContainer.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
                }
                if (arrow != null) {
                    arrow.setImageResource(isExpanded ? R.drawable.ic_chevron_up : R.drawable.ic_chevron_down);
                }
            }
        });
    }

    private void setupTicketSubmissionSection() {
        if (chipGroupTopics != null) {
            chipGroupTopics.setOnCheckedChangeListener((group, checkedId) -> {
                TicketTopic selected = chipIdToTopicMap.get(checkedId);
                viewModel.setSelectedTopic(selected);
            });
        }

        viewModel.getSelectedTopic().observe(this, topic -> {
            if (chipGroupTopics == null) return;
            Integer chipId = topicToChipIdMap.get(topic);
            if (chipId != null) {
                if (chipGroupTopics.getCheckedChipId() != chipId) {
                    chipGroupTopics.check(chipId);
                }
            } else {
                chipGroupTopics.clearCheck();
            }
            updateSubmitButtonState();
        });

        if (edtTicketMessage != null) {
            edtTicketMessage.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    viewModel.setMessageText(s.toString());
                }
                @Override public void afterTextChanged(Editable s) { }
            });
        }

        viewModel.getMessageText().observe(this, text -> {
            if (edtTicketMessage != null && !edtTicketMessage.getText().toString().equals(text)) {
                edtTicketMessage.setText(text);
                if (text != null) edtTicketMessage.setSelection(text.length());
            }
            updateSubmitButtonState();
        });

        if (btnSubmitTicket != null) {
            btnSubmitTicket.setOnClickListener(v -> {
                if (viewModel.submitTicket()) {
                    showSnackbar(getString(R.string.support_ticket_submitted_toast));
                }
            });
        }
    }

    private void updateSubmitButtonState() {
        if (btnSubmitTicket != null) {
            boolean canSubmit = viewModel.canSubmit();
            btnSubmitTicket.setEnabled(canSubmit);
            btnSubmitTicket.setAlpha(canSubmit ? 1.0f : 0.5f);
        }
    }

    private void setupMyTicketsSection() {
        viewModel.getTickets().observe(this, tickets -> {
            if (sectionMyTickets == null || myTicketsContainer == null) return;
            if (tickets == null || tickets.isEmpty()) {
                sectionMyTickets.setVisibility(View.GONE);
            } else {
                sectionMyTickets.setVisibility(View.VISIBLE);
                myTicketsContainer.removeAllViews();
                LayoutInflater inflater = LayoutInflater.from(this);
                for (SupportTicket ticket : tickets) {
                    View itemView = inflater.inflate(R.layout.item_support_ticket, myTicketsContainer, false);
                    TextView topicText = itemView.findViewById(R.id.ticketTopicText);
                    TextView idText = itemView.findViewById(R.id.ticketIdText);
                    TextView messageText = itemView.findViewById(R.id.ticketMessageText);
                    TextView statusText = itemView.findViewById(R.id.ticketStatusText);

                    if (topicText != null) topicText.setText(ticket.getTopic().getLabel());
                    if (idText != null) idText.setText(ticket.getId() + " · " + ticket.getCreatedAt());
                    if (messageText != null) messageText.setText(ticket.getMessage());
                    if (statusText != null) statusText.setText(ticket.getStatus());

                    myTicketsContainer.addView(itemView);
                }
            }
        });
    }

    private void showSnackbar(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT).show();
    }
}
