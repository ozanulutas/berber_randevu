package com.example.berberrandevu;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    ImageButton btnGeri;    // Arama işleminin iptal edilmesi için
    Button btnRandevu;      // Randevu Kayıt sayfasını açar
    LinearLayout linearLayoutGizliMenu, linearLayoutSecenekler, linearLayoutSil;
    ConstraintLayout clAnaEkran;
    EditText etAra; // Arama kriterinin grileceği yer
    ExpandableListView expListView; // Randevuların listeleneceği yapı
    ExpandableListAdapter expListAdapter;
    Switch swTumunuGoster;  // Eski randevuların gösterilip gösterilmeyeceği seçilir.
    SharedPreferences spTercihler; // Kullanıcının randevu listeleme tercihinin kayıt edilmesi için.(Eski randevuların gösterilip gösterilmeyeceği)

    List<String> listDataHeader;            // ExpandableListView grup başlıkları
    HashMap<String, List<String>> listHash; // Başlıkların altınadki elemanlar
    String [] randevuDetay; // Veri tabanından gelen randevu detayları ve randevuId(PK) bilgisini tutan dizi
    String aramaKriteri, silinecekTarih;
    boolean basildiMi, tumuGosterilsinMi;

    VTIslemleri vt; // Veri tabanı işlemlerinin yapılacağı nesne.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        expListView=findViewById(R.id.lvExp);
        linearLayoutGizliMenu=findViewById(R.id.linearLayout);
        linearLayoutSecenekler=findViewById(R.id.linearLayout2);
        clAnaEkran=findViewById(R.id.clAnaEkran);
        btnGeri=findViewById(R.id.btnGeri);
        etAra=findViewById(R.id.etAra);
        btnRandevu=findViewById(R.id.btnRandevu);
        linearLayoutSil=findViewById(R.id.linearLayoutSil);
        swTumunuGoster=findViewById(R.id.swTumunuGoster);
        spTercihler=this.getSharedPreferences("com.example.berberrandevu", MODE_PRIVATE);

        aramaKriteri="";

        tumuGosterilsinMi=spTercihler.getBoolean("tumuGosterilsinMi",true); // Eski randevular da gösterilsin mi
        if(tumuGosterilsinMi)   // Duruma göre switchin pozisyonu ayarlanır.
            swTumunuGoster.setChecked(true);
        else
            swTumunuGoster.setChecked(false);

        vt=new VTIslemleri(this); // Veritabanına işlemleri için nesne oluşturuldu

        initData("listele");        // Verilerin hashmap'a ve grup başlıklarına aktarılması
        expListAdapter=new ExpandableListAdapter(this,listDataHeader,listHash); // Hashmapteki verilerin ExpandableListView'a aktarılması
        expListView.setAdapter(expListAdapter);

        expListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() { //ExpandableListView'daki grupların alt elemanlarına tıklandığında
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, final int groupPosition, final int childPosition, long id) {

                menuyuGizle();
                final String aranacakTarih=expListAdapter.getGroup(groupPosition).toString();               // Grup başlığındaki tarihin çekilmesi
                final String aranacakDetay=expListAdapter.getChild(groupPosition,childPosition).toString(); // Seçili alt grup elemanının bilgileri
                randevuDetay=vt.randevuDetayListele(aranacakTarih, aranacakDetay, "kullaniciya");    // Tıklanan öğeye göre randevu detayları çekilir

                AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();      // Randevunun üzerine tıklandığında randevu detaylarını gösterecek olan pencere
                alertDialog.setTitle("Randevu Detayları");
                alertDialog.setMessage(randevuDetay[0]);    // Dialog pencereisinin içeriğinin randevu detaylarıyla doldurulması

                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Güncelle", new DialogInterface.OnClickListener() {  // Dialog penceresinde güncelleye basıldığında rezervasyonun güncelleneceği sayfaya geçilir
                    public void onClick(DialogInterface dialog, int id) {

                        Intent intent=new Intent(MainActivity.this,Main2Activity.class);
                        intent.putExtra("güncelle", vt.randevuDetayListele(aranacakTarih, aranacakDetay, "veritabanina")); // main2Actiity yani Randevu ekleme sayfasına  randevu detayları gönderilir
                        startActivity(intent);
                    }
                });

                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Sil", new DialogInterface.OnClickListener() {   // Dialog penceresindeki sil düğmesine basıldığında kullanıcının onayıyla kayt silinir
                    public void onClick(DialogInterface dialog, int id) {

                        android.app.AlertDialog.Builder alert=new android.app.AlertDialog.Builder(MainActivity.this);
                        alert.setTitle("Uyarı");
                        alert.setMessage("Randevuyu Silmek İstediğinize Emin Misiniz?");
                        alert.setPositiveButton("Sil", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                if(vt.randevuSil(randevuDetay[1])) {        // randevuDetay[1]->randevuId ile silme işlemi yapılır, başarılıysa kullanıcı bilgilendirilir
                                    Toast.makeText(MainActivity.this, "Randevu Silindi", Toast.LENGTH_SHORT).show();
                                    expListViewGuncelle("listele");   // expListView yeni verilerle güncellenir
                                }
                                else
                                    Toast.makeText(MainActivity.this, silinecekTarih+"Silme İşlemi Başarısız", Toast.LENGTH_SHORT).show();
                            }
                        });

                        alert.setNegativeButton("İptal", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                Toast.makeText(MainActivity.this, "Silme İşlemi İptal Edildi",Toast.LENGTH_SHORT).show();
                            }
                        });
                        alert.show();

                    }
                });

                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Tamam", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {   // Dialog penceresinin nötr düğmesi

                        dialog.cancel();
                    }
                });
                alertDialog.show();

                return true;
            }
        });

        expListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {  // Grup başlığına uzun tıklandığında gizli olan silme seçeneği açılır
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                long pos = expListView.getExpandableListPosition(position);
                int itemType = expListView.getPackedPositionType(pos);
                int groupPosition = expListView.getPackedPositionGroup(pos);

                if (itemType == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {    // Gizli olan silme seçeneği açılır ve silinmek istenen tarih silinecekTarih değişkenine atanır
                    linearLayoutSecenekler.setVisibility(View.INVISIBLE);
                    basildiMi=true;
                    linearLayoutSil.animate().translationY(0);

                    silinecekTarih=listDataHeader.get(groupPosition);   // Tıklanan tarihin değeri listeden çekilir
                }
                return true;
            }
        });


        expListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() { // Grup başlığına tıklandığında menü kapanır
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                menuyuGizle();
                return false;
            }
        });

        swTumunuGoster.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {    // Switch seçeneğinin değişimiyle spTercihler isimli shared preferencesa ver tumuGosterilsinMi değişkenine veri atanır.
                if (isChecked)
                    spTercihler.edit().putBoolean("tumuGosterilsinMi",true).apply();
                else
                    spTercihler.edit().putBoolean("tumuGosterilsinMi",false).apply();

                tumuGosterilsinMi=spTercihler.getBoolean("tumuGosterilsinMi",true);
                if(etAra.getText().equals(""))  // Eğer arama kısmı boşsa listeleme yapılır
                    expListViewGuncelle("listele");
                else
                    expListViewGuncelle("ara");
            }
        });

    }

    public void initData(String islem) {    // Verilerin hashmap'a aktarılması. İşlem parametresi arama mı yoksa listeleme mi yapıldığını belirtir
        listDataHeader=new ArrayList<>();
        listHash=new HashMap<>();

        if(islem.equals("listele"))
            vt.randevuTarihListele("",listDataHeader,tumuGosterilsinMi);        // Tarih bilgilerinin grup başlıklarına alınması
        else if(islem.equals("ara"))
            vt.randevuTarihListele(aramaKriteri,listDataHeader,tumuGosterilsinMi);    // Arama kriterine göre tarih bilgisi çekilir

        List<String> liste;
        for (int i=0; i<listDataHeader.size(); i++) {
            liste=new ArrayList<>();

            if(islem.equals("listele"))
                vt.randevuSaatListele("", liste, listDataHeader.get(i)); // Tarih bilgisi verilen randevular listeye aktarılır
            else if(islem.equals("ara"))
                vt.randevuSaatListele(aramaKriteri, liste, listDataHeader.get(i));

            listHash.put(listDataHeader.get(i),liste); // Tarihler ve ilgili tarihteki randevular hasmap'e aktarılıyor
        }
    }

    public void expListViewGuncelle(String islem) { // expListView'ı yeni verilerle günceller
        initData(islem);
        expListAdapter.setNewItems(listDataHeader,listHash);
    }

    public void btnEkle (View v) {  // Gizli menünüyü açar ve seçenek elemanlarını gizler
        linearLayoutSecenekler.setVisibility(View.INVISIBLE);
        swTumunuGoster.setVisibility(View.INVISIBLE);
        linearLayoutGizliMenu.animate().translationY(0);
        basildiMi=true;
    }
    public void clAnaEkran (View v) {  // Gizli menünün kapanması
        menuyuGizle();
    }

    public void btnRandevu (View v) {   // Randevu kayıt sayfasına geçiş
        menuyuGizle();
        Intent intent=new Intent(MainActivity.this,Main2Activity.class);
        startActivity(intent);
    }

    public void btnTarife (View v) {    // Tarife kayıt sayfasına geçiş
        menuyuGizle();
        Intent intent=new Intent(MainActivity.this,Main3Activity.class);
        startActivity(intent);
    }

    public void btnSil(View v) {    // Tarihteki tüm kayıtları silen düğme
        menuyuGizle();
        android.app.AlertDialog.Builder alert=new android.app.AlertDialog.Builder(MainActivity.this);
        alert.setTitle("Uyarı");
        alert.setMessage(silinecekTarih+" Tarihindeki Tüm Randevuları Silmek İstediğinize Emin Misiniz?");
        alert.setPositiveButton("Sil", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if(vt.tarihtekiRandevularıSil(silinecekTarih)) {        // Tarihteki tüm randevular silinir, işlem başarılıysa kullanıcı bilgilendirilir
                    Toast.makeText(MainActivity.this, silinecekTarih+" Tarihli Randevular Silindi.", Toast.LENGTH_SHORT).show();
                    expListViewGuncelle("listele");   // expListView yeni verilerle güncellenir
                }
                else
                    Toast.makeText(MainActivity.this, silinecekTarih+"Silme İşlemi Başarısız", Toast.LENGTH_SHORT).show();
            }
        });

        alert.setNegativeButton("İptal", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(MainActivity.this, "Silme İşlemi İptal Edildi.",Toast.LENGTH_SHORT).show();
                menuyuGizle();
            }
        });
        alert.show();
    }

    public void btnIptal (View v) {
        menuyuGizle();
    }

    public void btnAra (View v) {   // Randevular içinde arama
        menuyuGizle();
        btnGeri.setVisibility(View.VISIBLE);
        aramaKriteri=etAra.getText().toString();
        expListViewGuncelle("ara");
    }
    public void btnGeri (View v) { // Aramanın iptal edilmesi
        btnGeri.setVisibility(View.INVISIBLE);
        expListViewGuncelle("listele");
        etAra.setText("");
        aramaKriteri="";
    }

    public void menuyuGizle() { // Açılmış olan gizli menüyü kapatır ve gizlenen seçenek elemanlarını gösterir
        if (basildiMi==true) {
            linearLayoutGizliMenu.animate().translationY(-linearLayoutGizliMenu.getHeight()-5);
            linearLayoutSil.animate().translationY(-linearLayoutSil.getHeight()-40);
            linearLayoutSecenekler.setVisibility(View.VISIBLE);
            swTumunuGoster.setVisibility(View.VISIBLE);
        }
        basildiMi=false;
    }

    @Override
    protected void onResume() { // Activity geçişlerinde expListView yeni verilerle güncellenir
        expListViewGuncelle("listele");
        btnGeri.setVisibility(View.INVISIBLE);
        etAra.setText("");

        super.onResume();
    }
}
