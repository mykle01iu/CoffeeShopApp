package com.coffeeshop.client.ui.home;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.text.InputType;
import android.view.View;
import android.widget.*;
import com.coffeeshop.client.R;
import com.coffeeshop.client.data.AddressRepository;
import com.coffeeshop.client.model.Address;
import java.util.List;

/** Java/XML-friendly address form and picker shared by checkout, home, and account. */
final class AddressDialogs {
    interface Selection { void onSelected(Address address); }
    private AddressDialogs() {}
    static void showPicker(Context c, Address selected, Selection callback) {
        AddressRepository repo=AddressRepository.get(c); List<Address> rows=repo.getAddresses();
        if(rows.isEmpty()){ showForm(c,null,callback); return; }
        LinearLayout content=new LinearLayout(c); content.setOrientation(LinearLayout.VERTICAL); content.setPadding(dp(c,20),dp(c,8),dp(c,20),dp(c,8));
        RadioGroup group=new RadioGroup(c); final Address[] choice={selected};
        for(Address a:rows){
            LinearLayout row=new LinearLayout(c); row.setOrientation(LinearLayout.HORIZONTAL); row.setGravity(android.view.Gravity.CENTER_VERTICAL);
            RadioButton rb=new RadioButton(c); rb.setText(a.receiverName+"  ·  "+a.phone+"\n"+a.getFullAddress()+(a.isDefault?"\n"+c.getString(R.string.address_default_badge):"")); rb.setTextColor(Color.rgb(76,55,44)); rb.setPadding(0,dp(c,8),0,dp(c,8)); rb.setChecked(selected!=null&&selected.id.equals(a.id)); rb.setOnClickListener(v->choice[0]=a);
            row.addView(rb,new LinearLayout.LayoutParams(0,-2,1)); Button edit=new Button(c); edit.setText(R.string.address_edit); row.addView(edit); edit.setOnClickListener(v->showForm(c,a,callback)); group.addView(row);
        }
        content.addView(group); Button add=new Button(c); add.setText(R.string.address_add_new); content.addView(add); add.setOnClickListener(v->{showForm(c,null,callback);});
        new AlertDialog.Builder(c).setTitle(R.string.address_picker_title).setView(content).setPositiveButton(R.string.address_confirm,(d,w)->{if(choice[0]!=null)callback.onSelected(choice[0]);}).setNegativeButton(android.R.string.cancel,null).show();
    }
    static void showForm(Context c, Address existing, Selection callback) {
        LinearLayout outer=new LinearLayout(c); outer.setOrientation(LinearLayout.VERTICAL); outer.setPadding(dp(c,20),dp(c,8),dp(c,20),dp(c,12));
        ScrollView scroll=new ScrollView(c); LinearLayout fields=new LinearLayout(c); fields.setOrientation(LinearLayout.VERTICAL); scroll.addView(fields); outer.addView(scroll,new LinearLayout.LayoutParams(-1,dp(c,470)));
        String[] labels={c.getString(R.string.address_receiver),c.getString(R.string.address_phone),c.getString(R.string.address_street),c.getString(R.string.address_ward),c.getString(R.string.address_district),c.getString(R.string.address_city)};
        String[] values=existing==null?new String[]{c.getString(R.string.address_profile_name),c.getString(R.string.address_profile_phone),"","","",c.getString(R.string.address_default_city)}:new String[]{existing.receiverName,existing.phone,existing.street,existing.ward,existing.district,existing.city};
        EditText[] input=new EditText[6]; TextView[] errors=new TextView[6];
        for(int i=0;i<6;i++){ EditText e=new EditText(c); e.setSingleLine(true); e.setHint(labels[i]); e.setText(values[i]); e.setTextColor(Color.rgb(57,43,35)); e.setHintTextColor(Color.GRAY); e.setBackgroundResource(R.drawable.bg_input); e.setPadding(dp(c,12),dp(c,8),dp(c,12),dp(c,8)); e.setInputType(i==1?InputType.TYPE_CLASS_PHONE:InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_CAP_SENTENCES); LinearLayout.LayoutParams ep=new LinearLayout.LayoutParams(-1,dp(c,52)); ep.topMargin=dp(c,7); fields.addView(e,ep); TextView er=new TextView(c); er.setTextColor(Color.rgb(153,56,45)); er.setTextSize(12); er.setVisibility(View.GONE); fields.addView(er); input[i]=e;errors[i]=er; }
        TextView label=new TextView(c); label.setText(R.string.address_label); label.setTextColor(Color.rgb(76,55,44)); fields.addView(label);
        RadioGroup labelsGroup=new RadioGroup(c); labelsGroup.setOrientation(RadioGroup.HORIZONTAL); String[] labelTexts={c.getString(R.string.address_home_label),c.getString(R.string.address_office_label),c.getString(R.string.address_other_label)}; int selectedLabel=existing==null?0:existing.label.ordinal();
        for(int i=0;i<3;i++){RadioButton b=new RadioButton(c); b.setText(labelTexts[i]); b.setId(100+i); b.setChecked(i==selectedLabel); labelsGroup.addView(b);} fields.addView(labelsGroup);
        CheckBox save=new CheckBox(c); save.setText(R.string.address_save_book); save.setChecked(existing!=null||true); fields.addView(save); CheckBox def=new CheckBox(c); def.setText(R.string.address_make_default); def.setChecked(existing!=null&&existing.isDefault); def.setVisibility(save.isChecked()?View.VISIBLE:View.GONE); fields.addView(def); save.setOnCheckedChangeListener((b,checked)->def.setVisibility(checked?View.VISIBLE:View.GONE));
        AlertDialog dialog=new AlertDialog.Builder(c).setTitle(R.string.address_form_title).setView(outer).setPositiveButton(R.string.address_confirm,null).setNegativeButton(android.R.string.cancel,null).setNeutralButton(existing==null?null:c.getString(R.string.address_delete),null).create();
        dialog.setOnShowListener(d->{dialog.getButton(-1).setOnClickListener(v->{ boolean valid=true; for(int i=0;i<input.length;i++){String val=input[i].getText().toString().trim();String err=null;if(val.isEmpty())err=c.getString(R.string.address_error_required);else if(i==0&&val.length()<2)err=c.getString(R.string.address_error_name);else if(i==1&&!val.matches("^0\\d{9}$"))err=c.getString(R.string.address_error_phone);errors[i].setText(err==null?"":err);errors[i].setVisibility(err==null?View.GONE:View.VISIBLE); if(err!=null)valid=false;} if(!valid)return;
            int ord=0;for(int i=0;i<3;i++)if(labelsGroup.getCheckedRadioButtonId()==100+i)ord=i; Address.AddressLabel kind=Address.AddressLabel.values()[ord]; Address result=new Address(existing==null?"":existing.id,input[0].getText().toString().trim(),input[1].getText().toString().trim(),input[2].getText().toString().trim(),input[3].getText().toString().trim(),input[4].getText().toString().trim(),input[5].getText().toString().trim(),kind,def.isChecked());
            if(save.isChecked()){AddressRepository r=AddressRepository.get(c); if(existing==null)result=r.add(result);else r.update(result);} callback.onSelected(result); dialog.dismiss(); });
            if(existing!=null){dialog.getButton(-3).setOnClickListener(v->confirmDelete(c,existing));}
        }); dialog.show();
    }
    private static void confirmDelete(Context c,Address a){new AlertDialog.Builder(c).setMessage(R.string.address_delete_confirm).setNegativeButton(android.R.string.cancel,null).setPositiveButton(R.string.address_delete,(d,w)->AddressRepository.get(c).delete(a.id)).show();}
    private static int dp(Context c,int n){return (int)(n*c.getResources().getDisplayMetrics().density+.5f);}
}
