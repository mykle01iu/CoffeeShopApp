package com.coffeeshop.client.ui.home;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.view.View;
import android.widget.*;
import com.coffeeshop.client.R;
import com.coffeeshop.client.data.AddressRepository;
import com.coffeeshop.client.model.Address;
import com.coffeeshop.client.ui.common.FontUtils;
import com.coffeeshop.client.ui.common.SystemInsets;

public class AddressActivity extends Activity implements AddressRepository.Listener {
    private LinearLayout list;
    private AddressRepository repo;
    @Override protected void onCreate(Bundle b){super.onCreate(b); repo=AddressRepository.get(this); LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(dp(20),dp(24),dp(20),dp(20));root.setBackgroundColor(getColor(R.color.coffee_cream));
        TextView title=new TextView(this);title.setText(R.string.address_picker_title);title.setTextSize(24);title.setTextColor(getColor(R.color.coffee_brown_dark));title.setTypeface(FontUtils.lora(this,600));root.addView(title);
        Button add=new Button(this);add.setText(R.string.address_add);root.addView(add);add.setOnClickListener(v->AddressDialogs.showForm(this,null,a->render()));
        ScrollView scroll=new ScrollView(this);list=new LinearLayout(this);list.setOrientation(LinearLayout.VERTICAL);scroll.addView(list);root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));setContentView(root);SystemInsets.apply(this);render(); }
    @Override protected void onResume(){super.onResume();if(repo!=null)repo.addListener(this);render();}
    @Override protected void onPause(){if(repo!=null)repo.removeListener(this);super.onPause();}
    @Override public void onAddressesChanged(java.util.List<Address> ignored){runOnUiThread(this::render);}
    private void render(){if(list==null)return;list.removeAllViews();if(repo.getAddresses().isEmpty()){TextView empty=new TextView(this);empty.setText("⌖\n"+getString(R.string.address_empty_title));empty.setGravity(17);empty.setTextColor(getColor(R.color.coffee_muted));empty.setPadding(0,dp(48),0,dp(48));list.addView(empty);return;}
        for(Address a:repo.getAddresses()){LinearLayout card=new LinearLayout(this);card.setOrientation(LinearLayout.VERTICAL);card.setPadding(dp(14),dp(12),dp(14),dp(12));card.setBackgroundResource(R.drawable.bg_product_card);LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(-1,-2);cp.topMargin=dp(10);list.addView(card,cp);
            TextView text=new TextView(this);text.setText(a.receiverName+" · "+a.phone+"\n"+a.getFullAddress()+"\n"+getString(a.label==Address.AddressLabel.HOME?R.string.address_home_label:a.label==Address.AddressLabel.OFFICE?R.string.address_office_label:R.string.address_other_label)+(a.isDefault?" · "+getString(R.string.address_default_badge):""));text.setTextColor(getColor(R.color.coffee_text));text.setTextSize(14);card.addView(text);
            LinearLayout actions=new LinearLayout(this);card.addView(actions);Button edit=new Button(this);edit.setText(R.string.address_edit);actions.addView(edit);edit.setOnClickListener(v->AddressDialogs.showForm(this,a,x->{render();}));Button def=new Button(this);def.setText(R.string.address_make_default);actions.addView(def);def.setEnabled(!a.isDefault);def.setOnClickListener(v->{repo.setDefault(a.id);render();});Button del=new Button(this);del.setText(R.string.address_delete);actions.addView(del);del.setOnClickListener(v->new android.app.AlertDialog.Builder(this).setMessage(R.string.address_delete_confirm).setNegativeButton(android.R.string.cancel,null).setPositiveButton(R.string.address_delete,(d,w)->repo.delete(a.id)).show()); }
    }
    private int dp(int n){return (int)(n*getResources().getDisplayMetrics().density+.5f);}
}
