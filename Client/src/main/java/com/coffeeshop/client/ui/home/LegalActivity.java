package com.coffeeshop.client.ui.home;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.coffeeshop.client.R;
import com.coffeeshop.client.data.legal.LegalDocument;
import com.coffeeshop.client.data.legal.LegalRepository;
import com.coffeeshop.client.data.legal.LegalSection;
import com.coffeeshop.client.data.legal.LegalType;
import com.coffeeshop.client.ui.common.FontUtils;
import com.coffeeshop.client.ui.common.SystemInsets;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class LegalActivity extends Activity {
    public static final String EXTRA_TYPE = "legal_type";

    private ScrollView legalScrollView;
    private LinearLayout chipsContainer;
    private LinearLayout sectionsContainer;
    private FloatingActionButton fabScrollToTop;

    private final List<View> sectionViews = new ArrayList<>();
    private final List<TextView> chipViews = new ArrayList<>();
    private int activeIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_legal);
        SystemInsets.apply(this);
        FontUtils.applyInter(findViewById(android.R.id.content));

        String typeStr = getIntent() != null ? getIntent().getStringExtra(EXTRA_TYPE) : null;
        LegalType type = LegalType.TERMS;
        if ("PRIVACY".equalsIgnoreCase(typeStr)) {
            type = LegalType.PRIVACY;
        }

        LegalDocument doc = LegalRepository.getInstance().getDocument(type);

        TextView title = findViewById(R.id.legalTitle);
        if (title != null) {
            title.setText(doc.getTitle());
            title.setTypeface(FontUtils.lora(this, 600));
        }

        ImageButton backBtn = findViewById(R.id.legalBack);
        if (backBtn != null) {
            backBtn.setOnClickListener(v -> finish());
        }

        TextView lastUpdatedText = findViewById(R.id.legalLastUpdatedText);
        if (lastUpdatedText != null) {
            lastUpdatedText.setText(getString(R.string.legal_last_updated_format, doc.getLastUpdated(), doc.getVersion()));
        }

        legalScrollView = findViewById(R.id.legalScrollView);
        chipsContainer = findViewById(R.id.legalChipsContainer);
        sectionsContainer = findViewById(R.id.legalSectionsContainer);
        fabScrollToTop = findViewById(R.id.fabScrollToTop);

        View contactCard = findViewById(R.id.legalContactCard);
        if (contactCard != null) {
            contactCard.setOnClickListener(v ->
                    startActivity(new Intent(this, SupportActivity.class)));
        }

        if (fabScrollToTop != null) {
            fabScrollToTop.setOnClickListener(v -> {
                if (legalScrollView != null) {
                    legalScrollView.smoothScrollTo(0, 0);
                }
            });
        }

        if (legalScrollView != null) {
            legalScrollView.getViewTreeObserver().addOnScrollChangedListener(() -> {
                int scrollY = legalScrollView.getScrollY();
                if (fabScrollToTop != null) {
                    fabScrollToTop.setVisibility(scrollY > 300 ? View.VISIBLE : View.GONE);
                }
                updateActiveChipByScroll(scrollY);
            });
        }

        renderDocument(doc);
    }

    private void renderDocument(LegalDocument doc) {
        if (doc == null || doc.getSections() == null) return;

        sectionViews.clear();
        chipViews.clear();
        if (chipsContainer != null) chipsContainer.removeAllViews();
        if (sectionsContainer != null) sectionsContainer.removeAllViews();

        List<LegalSection> sections = doc.getSections();
        LayoutInflater inflater = LayoutInflater.from(this);

        for (int i = 0; i < sections.size(); i++) {
            final int index = i;
            LegalSection section = sections.get(i);

            // Add Index Chip
            TextView chip = new TextView(this);
            String chipLabel = extractChipText(section.getHeading());
            chip.setText(chipLabel);
            chip.setTextSize(13);
            chip.setTypeface(FontUtils.inter(this, 600));
            chip.setPadding(dp(14), dp(8), dp(14), dp(8));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            lp.setMarginEnd(dp(8));
            chip.setLayoutParams(lp);

            updateChipStyle(chip, i == 0);
            chip.setOnClickListener(v -> scrollToSection(index));

            if (chipsContainer != null) {
                chipsContainer.addView(chip);
            }
            chipViews.add(chip);

            // Add Section View
            View sectionView = inflater.inflate(R.layout.item_legal_section, sectionsContainer, false);
            TextView headingView = sectionView.findViewById(R.id.legalSectionHeading);
            TextView bodyView = sectionView.findViewById(R.id.legalSectionBody);

            if (headingView != null) {
                headingView.setText(section.getHeading());
            }
            if (bodyView != null) {
                bodyView.setText(section.getBody());
            }

            if (sectionsContainer != null) {
                sectionsContainer.addView(sectionView);
            }
            sectionViews.add(sectionView);
        }
    }

    private String extractChipText(String heading) {
        if (heading == null) return "";
        int dotIndex = heading.indexOf('.');
        if (dotIndex > 0 && dotIndex < heading.length() - 1) {
            String prefix = heading.substring(0, dotIndex + 1).trim();
            String title = heading.substring(dotIndex + 1).trim();
            if (title.length() > 15) {
                title = title.substring(0, 15) + "…";
            }
            return prefix + " " + title;
        }
        return heading.length() > 18 ? heading.substring(0, 18) + "…" : heading;
    }

    private void scrollToSection(int index) {
        if (index < 0 || index >= sectionViews.size() || legalScrollView == null) return;
        View targetView = sectionViews.get(index);
        int targetY = targetView.getTop();
        if (sectionsContainer != null) {
            targetY += sectionsContainer.getTop();
        }
        legalScrollView.smoothScrollTo(0, targetY - dp(16));
        setActiveChip(index);
    }

    private void updateActiveChipByScroll(int scrollY) {
        if (sectionViews.isEmpty() || sectionsContainer == null) return;
        int containerTop = sectionsContainer.getTop();
        int closestIndex = 0;
        int minDiff = Integer.MAX_VALUE;

        for (int i = 0; i < sectionViews.size(); i++) {
            View v = sectionViews.get(i);
            int sectionTop = containerTop + v.getTop() - dp(24);
            int diff = Math.abs(scrollY - sectionTop);
            if (scrollY >= sectionTop && diff < minDiff) {
                minDiff = diff;
                closestIndex = i;
            }
        }
        setActiveChip(closestIndex);
    }

    private void setActiveChip(int index) {
        if (index == activeIndex || index < 0 || index >= chipViews.size()) return;
        activeIndex = index;
        for (int i = 0; i < chipViews.size(); i++) {
            updateChipStyle(chipViews.get(i), i == activeIndex);
        }
    }

    private void updateChipStyle(TextView chip, boolean active) {
        if (chip == null) return;
        chip.setBackgroundResource(active ? R.drawable.bg_category_chip_selected : R.drawable.bg_category_chip);
        chip.setTextColor(getColor(active ? R.color.coffee_surface : R.color.coffee_muted));
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
