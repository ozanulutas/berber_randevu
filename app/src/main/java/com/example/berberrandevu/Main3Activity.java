package com.example.berberrandevu;

import androidx.appcompat.app.AppCompatActivity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;

public class Main3Activity extends AppCompatActivity {

    EditText etKesimTur, etFiyat;
    LinearLayout linearLayout;
    TextView tvBaslik;
    ListView lv;
    Button btnKaydet;

    ArrayList<String> liste;
    ArrayAdapter<String> adp;
    VTIslemleri vt;

    int fiyat;
    int silinecekPosition;
    String kesimTur;
    String [] tarife;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);

        etFiyat=findViewById(R.id.edFiyat);
        etKesimTur=findViewById(R.id.etKesimTur);
        lv=findViewById(R.id.listView);
        tvBaslik=findViewById(R.id.textView);
        linearLayout=findViewById(R.id.linearLayout);
        btnKaydet=findViewById(R.id.btnKaydet);

        vt=new VTIslemleri(this); // VTIslemleri sınıfından bir nesne yaratılarak veri tabanı ile bağlantı kurulur.
        vt.createTable("tblTarifeler"); // tblTarifeler tablosu yaratılır.

        liste=new ArrayList<>();
        vt.tarifeListele(liste,"Main3Activity"); // Tarifeler listeye aktarılır.
        adp=new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,liste);
        lv.setAdapter(adp);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                silinecekPosition=position;
                menuAcKapat("aç"); // Gizli menünün açılması

                tarife=vt.listedenTarifeCek(liste.get(position)); // Güncelleme ve silme işlemlerinde kullanılmak üzere tıklanan liste elemanı çekilir.
            }
        });
    }


    public void btnKaydet (View v) { // Yeni tarife ekleme

        if(etKesimTur.getText().toString().equals("") || etFiyat.getText().toString().equals("")) // Eğer edittext'ler boşsa işlem yapılmaz ve kullanıcı uyarılır.
            Toast.makeText(this, "Lütfen Tüm Alanları Doldurun",Toast.LENGTH_SHORT).show();
        else {
            kesimTur=etKesimTur.getText().toString();
            fiyat=Integer.parseInt(etFiyat.getText().toString());

            if(btnKaydet.getText().toString().equals("Kaydet")) {   // Buton'un üzerinde Kaydet yazıyorsa veritabanına kayıt işlemleri yapılır
                if(vt.tarifeEkle(kesimTur,fiyat))                   // tbltariflere değerler parametre ile gönderilir ve geriye bool değer döner ve kullanıcı bilgilendirilir.
                    Toast.makeText(this, kesimTur +" Tarifesi Eklendi.",Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(this, "Ekleme Başarısız",Toast.LENGTH_SHORT).show();
            }
            else if(btnKaydet.getText().toString().equals("Güncelle")) {    // Buton'un üzerinde Güncelle yazıyorsa kaydın güncelleme işlemleri yapılır
                if(vt.tarifeGuncelle(kesimTur, fiyat, tarife[0]))           // tbltariflere değerler parametre ile gönderilir ve geriye bool değer döner ve kullanıcı bilgilendirilir. tarife[0]->PK
                    Toast.makeText(this, "Tarife Başarıyla Güncellendi",Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(this, "Güncelleme Başarısız",Toast.LENGTH_SHORT).show();
            }
            listeGuncelle(); // VT işlemlerinden sonra liste güncellenir

            btnKaydet.setText("Kaydet");
            tvBaslik.setText("TARİFE EKLE");
            etSifirla(etFiyat,etKesimTur);
        }
    }

    public void btnDuzenle (View v) {   // Gizli menüdeki kalem sembolüne tıklandığında veri güncelleme moduna geçilir
        btnKaydet.setText("Güncelle");
        tvBaslik.setText("TARİFE GÜNCELLE");
        etKesimTur.setText(tarife[0]);
        etFiyat.setText(tarife[1]);
        menuAcKapat("kapat"); //Gizli menünün kapanması
    }

    public void btnSil (View v) {   // Gizli menüdeki çöp kutusu sembolüne tıklandığında daha önce seçilmiş olan eleman silinir.
        AlertDialog.Builder alert=new AlertDialog.Builder(Main3Activity.this); // Kullanıcıya silme işlemi konusunda emin olup olmadığını soran dialog penceresi hazırlanır
        alert.setTitle("Uyarı");
        alert.setMessage(tarife[0]+" Tarifesini Silmek İstediğinize Emin Misiniz?");
        alert.setPositiveButton("Sil", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(vt.tarifeSil(liste.get(silinecekPosition))) {
                    Toast.makeText(Main3Activity.this, "Tarife Silindi",Toast.LENGTH_SHORT).show();
                    listeGuncelle();
                }
                else
                    Toast.makeText(Main3Activity.this, "Silme İşlemi Başarısız",Toast.LENGTH_SHORT).show();
            }
        });

        alert.setNegativeButton("İptal", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(Main3Activity.this, "Silme İşlemi İptal Edildi",Toast.LENGTH_SHORT).show();
            }
        });
        alert.show();

        menuAcKapat("kapat");
    }

    public void btnIptal (View v) { // Gizli menüden iptal butonuna basılırsa menü kapanır, duruma göre buton ve başlık yazıları orjnal haline döner ve editText'ler temizlenir
        menuAcKapat("kapat");
        if(btnKaydet.getText().toString().equals("Güncelle")) {
            btnKaydet.setText("Kaydet");
            tvBaslik.setText("TARİFE EKLE");
            etSifirla(etFiyat,etKesimTur);
        }
    }

    void etSifirla (EditText et1, EditText et2) {
        et1.setText("");
        et2.setText("");
    }

    void listeGuncelle() { // Veritabanınadaki değişime göre listeyi günceller
        liste.clear();
        vt.tarifeListele(liste,"Main3Activity");
        lv.setAdapter(adp);
    }

    void menuAcKapat(String islem) { // Parametre olarak aç gelirse gizli menu açılır, aksi taktirde menü kapatılır
        if (islem.equals("aç"))
            linearLayout.animate().translationY(0);
        else
            linearLayout.animate().translationY(-linearLayout.getHeight()-5);
    }
}
