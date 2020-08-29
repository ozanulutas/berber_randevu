package com.example.berberrandevu;

import androidx.appcompat.app.AppCompatActivity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Calendar;

public class Main2Activity extends AppCompatActivity {

    EditText etAdSoyad, etTelefon;
    Spinner spTur;  //Kesim türleri
    Button btnTarih, btnSaat, btnKaydet;
    TextView tvBaslik;
    Switch swDurum; //Ödeme durumu

    String adSoyad,telefon,tur,durum,tarih,saat;
    String[] randevuDetay, detaylar;
    ArrayAdapter adpTurler;
    ArrayList<String> listeTurler;

    VTIslemleri vt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        tvBaslik=findViewById(R.id.tvBaslik);
        etAdSoyad=findViewById(R.id.etAdSoyad);
        etTelefon=findViewById(R.id.etTel);
        spTur=findViewById(R.id.spTur);
        btnTarih=findViewById(R.id.btnTarih);
        btnSaat=findViewById(R.id.btnSaat);
        btnKaydet=findViewById(R.id.btnKaydet);
        swDurum=findViewById(R.id.swDurum);

        durum="Alınmadı";
        swDurum.setText("Ödemesi Alınmadı"); //switch metninin ayarlanması

        //güncel tarih ve saat bilgisi alınır
        tarih=basinaSifirKoy(Calendar.getInstance().get(Calendar.YEAR))+"-"+basinaSifirKoy(Calendar.getInstance().get(Calendar.MONTH)+1)+"-"+basinaSifirKoy(Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
        saat=basinaSifirKoy(Calendar.getInstance().get(Calendar.HOUR_OF_DAY))+":"+basinaSifirKoy(Calendar.getInstance().get(Calendar.MINUTE));

        vt=new VTIslemleri(this);       //veritabanı bağlantısı oluşturuluyor
        vt.createTable("tblRandevular");    //tablolar yaratılıyor
        vt.createTable("tblMusteriler");

        listeTurler=new ArrayList<>();
        vt.tarifeListele(listeTurler, "Main2Activity"); // Kesim Türleri(Tarifeler) veritabanından spinnera aktarılır
        adpTurler=new ArrayAdapter(this, android.R.layout.simple_spinner_item,listeTurler);
        spTur.setAdapter(adpTurler);

        Intent intent =getIntent();
        randevuDetay=intent.getStringArrayExtra("güncelle"); // Randevu detayları. İki elemanlı bir dizi olarak gelir. İlk eleman detayları tutar, ikinci eleman PK'yı(randevuId) tutar

        if(randevuDetay!=null) { //mainactivityden güncelle isminde, randevu detaylarının bulunduğu intent gönderildiyse girdi alanları doldurulur ve sayfa güncelleme sayfasına dönüştürülür
            detaylar=detayParcala(randevuDetay[0]);

            tvBaslik.setText("RANDEVU GÜNCELLE");
            btnKaydet.setText("Güncelle");

            etAdSoyad.setText(detaylar[0]); adSoyad=detaylar[0];
            etTelefon.setText(detaylar[1]); telefon=detaylar[1];

            tur=detaylar[2];
            for (int i=0; i<listeTurler.size(); i++) { //kesim türünün ayarlanması
                if(detaylar[2].equals(listeTurler.get(i)))
                    spTur.setSelection(i);
            }

            if(detaylar[3].equals("Alındı")) { //ödeme durmunun ayarlanması
                swDurum.setChecked(true);
                durum="Alındı";
                swDurum.setText("Ödemesi Alındı");
            }
            else {
                swDurum.setChecked(false);
                durum="Alınmadı";
                swDurum.setText("Ödemesi Alınmadı");
            }

            tarih=detaylar[4]+"-"+detaylar[5]+"-"+detaylar[6];
            saat=detaylar[7];

            btnTarih.setText(detaylar[6]+"\\"+detaylar[5]+"\\"+detaylar[4]);
            btnSaat.setText(saat);
        }

        spTur.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) { // Spinnerdan eleman seçildiğinde tur değişkenine ataması yapılır
                tur=listeTurler.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {  }
        });

        swDurum.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {    // Switch seçeneğinin değişimiyle durum değişkenine veri atanır.
                if (isChecked) {
                    durum="Alındı";
                    swDurum.setText("Ödemesi Alındı");
                }
                else {
                    durum="Alınmadı";
                    swDurum.setText("Ödemesi Alınmadı");
                }
            }
        });

    }

    public void btnKaydet (View v) {    // Veri tabanına randevu kayıt işlemleri.
        adSoyad=etAdSoyad.getText().toString();
        telefon=etTelefon.getText().toString();

        if(btnKaydet.getText().equals("Kaydet")) {              // Eğer butonun üzerinde Kaydet yazıyorsa kayıt işlemi yapılır.
            vt.musteriEkle(telefon,adSoyad);                    // Önce müşteri bilgisi müşteriler tablosuna eklenir.
            if(vt.randevuEkle(telefon,tur,tarih,saat,durum))    // Sonra randevular tablosuna kayıt yapılır eğer kayıt başarılıysa geriye bool değer döner ve kullanıcı bilgilendirilir
                Toast.makeText(Main2Activity.this, "Kayıt Başarılı.",Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(Main2Activity.this, "Kayıt Yapılamadı.",Toast.LENGTH_SHORT).show();
        }
        else if (btnKaydet.getText().equals("Güncelle")) {                          // Eğer MainActivityden güncelle isminde bir intent geldiyse sayfa güncelleme sayfasına dönüşmüştür ve kaydet butonu güncelle butonu olmuştur.
            vt.musteriGuncelle(detaylar[1],telefon,adSoyad);                        // detaylar[1]->Müşteriler tablosunun PK; tel. Müşteri bilgisi güncellenir.
            if(vt.randevuGuncelle(randevuDetay[1],telefon,tur,tarih,saat,durum))    // randevuDetay[1]->Randevular tablosunun PK; randevuId. Randevu bilgisi güncellenir ve geriye bool değer döner, kullanıcı bilgilendirilir.
                Toast.makeText(Main2Activity.this, "Güncelleme Başarılı.",Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(Main2Activity.this, "Güncelleme Yapılamadı.",Toast.LENGTH_SHORT).show();
        }
    }

    public void btnTarih (View v) {                         // Randevu tarihinin seçileceği button.
        Calendar mcurrentTime = Calendar.getInstance();
        int year = mcurrentTime.get(Calendar.YEAR);         // Güncel Yılı alıyoruz
        int month = mcurrentTime.get(Calendar.MONTH);       // Güncel Ayı alıyoruz
        int day = mcurrentTime.get(Calendar.DAY_OF_MONTH);  // Güncel Günü alıyoruz

        DatePickerDialog datePicker;                        // Datepicker objemiz
        datePicker = new DatePickerDialog(Main2Activity.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {                      // Tarih seçildiğinde
            tarih = year + "-" + basinaSifirKoy(monthOfYear+1) + "-" + basinaSifirKoy(dayOfMonth);              // Seç butonu tıklandığında seçili tarihi alıyoruz ve veritabanına göre formatlıyoruz
            btnTarih.setText( basinaSifirKoy(dayOfMonth) + "\\" + basinaSifirKoy(monthOfYear+1) + "\\" + year); // Tarih seçme butonunun üzerine seçili tarihi yazdırıyoruz.

            }
        },year,month,day);  //Başlarken set edilcek değerlerimizi atıyoruz

        datePicker.setTitle("Tarih Seçiniz");
        datePicker.setButton(DatePickerDialog.BUTTON_POSITIVE, "Seç", datePicker);
        datePicker.setButton(DatePickerDialog.BUTTON_NEGATIVE, "İptal", datePicker);

        datePicker.show();
    }

    public void btnSaat (View v) {
        Calendar mcurrentTime = Calendar.getInstance();
        int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);  // Güncel saati aldık
        int minute = mcurrentTime.get(Calendar.MINUTE);     // Güncel dakikayı aldık

        TimePickerDialog timePicker;                        // Time Picker referansımızı oluşturduk
        timePicker = new TimePickerDialog(Main2Activity.this, new TimePickerDialog.OnTimeSetListener() { //TimePicker objemizi oluşturuyor ve click listener ekliyoruz
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
             saat=basinaSifirKoy(selectedHour) + ":" + basinaSifirKoy(selectedMinute);//Seç butonu tıklandığında seçili saati alıyoruz
             btnSaat.setText(saat);
            }
        }, hour, minute, true);                // true 24 saatli sistem için

        timePicker.setTitle("Saat Seçiniz");
        timePicker.setButton(DatePickerDialog.BUTTON_POSITIVE, "Seç", timePicker);
        timePicker.setButton(DatePickerDialog.BUTTON_NEGATIVE, "İptal", timePicker);

        timePicker.show();
    }

    String basinaSifirKoy(int sayi) {   // Seçilen tarih ve saatin veritabanına yollanırken uygun formata dönüştürülmesi için
        String s=Integer.toString(sayi);
        if(s.length()==1)
            s="0"+s;
        return s;
    }

    public String [] detayParcala (String gelenDetay) { // MainActivity'den gelen intent içindeki detayları ayrıştırır
        String [] detaylar=gelenDetay.split("-");
        return detaylar;
    }
}
