package com.coffeeshop.client.data;

import android.content.Context;
import android.content.SharedPreferences;
import com.coffeeshop.client.model.Address;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

/** Shared, persisted address book. Starts empty; legacy mock address preferences are deliberately ignored. */
public final class AddressRepository {
    public interface Listener { void onAddressesChanged(List<Address> addresses); }
    private static final String PREF="shared_address_book", KEY="addresses";
    private static AddressRepository instance;
    private final SharedPreferences prefs;
    private final CopyOnWriteArrayList<Listener> listeners=new CopyOnWriteArrayList<>();
    private List<Address> addresses;
    private AddressRepository(Context c) { prefs=c.getApplicationContext().getSharedPreferences(PREF,Context.MODE_PRIVATE); addresses=read(); }
    public static synchronized AddressRepository get(Context c) { if(instance==null) instance=new AddressRepository(c); return instance; }
    public synchronized List<Address> getAddresses() { return Collections.unmodifiableList(new ArrayList<>(addresses)); }
    public void addListener(Listener l) { if(l!=null) listeners.addIfAbsent(l); }
    public void removeListener(Listener l) { listeners.remove(l); }
    public synchronized Address add(Address a) { boolean first=addresses.isEmpty(); Address saved=new Address(UUID.randomUUID().toString(),a.receiverName,a.phone,a.street,a.ward,a.district,a.city,a.label,first||a.isDefault); if(saved.isDefault) for(int i=0;i<addresses.size();i++) addresses.set(i,addresses.get(i).withDefault(false)); addresses.add(saved); save(); return saved; }
    public synchronized void update(Address a) { for(int i=0;i<addresses.size();i++) if(addresses.get(i).id.equals(a.id)){ if(a.isDefault) clearDefaults(); addresses.set(i,a); save(); return; } }
    public synchronized void delete(String id) { boolean wasDefault=false; for(Address a:addresses) if(a.id.equals(id)) wasDefault=a.isDefault; for(int i=addresses.size()-1;i>=0;i--) if(addresses.get(i).id.equals(id)) addresses.remove(i); if(wasDefault&&!addresses.isEmpty()) addresses.set(0,addresses.get(0).withDefault(true)); save(); }
    public synchronized void setDefault(String id) { for(int i=0;i<addresses.size();i++) addresses.set(i,addresses.get(i).withDefault(addresses.get(i).id.equals(id))); save(); }
    public synchronized Address getDefault() { for(Address a:addresses) if(a.isDefault) return a; return addresses.isEmpty()?null:addresses.get(0); }
    private void clearDefaults(){ for(int i=0;i<addresses.size();i++) addresses.set(i,addresses.get(i).withDefault(false)); }
    private void save(){ JSONArray arr=new JSONArray(); try { for(Address a:addresses){ JSONObject o=new JSONObject(); o.put("id",a.id);o.put("name",a.receiverName);o.put("phone",a.phone);o.put("street",a.street);o.put("ward",a.ward);o.put("district",a.district);o.put("city",a.city);o.put("label",a.label.name());o.put("default",a.isDefault);arr.put(o); } prefs.edit().putString(KEY,arr.toString()).apply(); } catch(Exception ignored){} for(Listener l:listeners) l.onAddressesChanged(getAddresses()); }
    private List<Address> read(){ List<Address> out=new ArrayList<>(); String raw=prefs.getString(KEY,null); if(raw==null||raw.isEmpty()) return out; try { JSONArray a=new JSONArray(raw); for(int i=0;i<a.length();i++){ JSONObject o=a.getJSONObject(i); out.add(new Address(o.optString("id"),o.optString("name"),o.optString("phone"),o.optString("street"),o.optString("ward"),o.optString("district"),o.optString("city"),Address.AddressLabel.valueOf(o.optString("label","HOME")),o.optBoolean("default"))); } }catch(Exception ignored){} return out; }
}
