package com.example.berberrandevu;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import static android.content.Context.MODE_PRIVATE;

public class VTIslemleri {

    public SQLiteDatabase db;
    // Guncel tarih bilgisi
    final String bugun=Calendar.getInstance().get(Calendar.YEAR)+"-"+tarihFormatla(String.valueOf(Calendar.getInstance().get(Calendar.MONTH)+1),"başına0koy")+"-"+tarihFormatla(String.valueOf(Calendar.getInstance().get(Calendar.DAY_OF_MONTH)),"başına0koy");

    public VTIslemleri (Context context) { // Yapıcı metod. Nesne oluşturulduğunda veritabanına bağlantıyı sağlar
        db=context.openOrCreateDatabase("dbRandevular",MODE_PRIVATE,null);
    }

    public void createTable (String tbl) { // Tablo oluşturur
        try {
            String sql="";
            if (tbl.equals("tblTarifeler"))
                sql="create table if not exists tblTarifeler(tur text PRIMARY KEY, fiyat integer)";
            if(tbl.equals("tblRandevular"))
                sql="create table if not exists tblRandevular(randevuId integer PRIMARY KEY, tel text, tur text, tarih text, saat Text, durum text, FOREIGN KEY(tel) REFERENCES tblMusteriler(tel), FOREIGN KEY(tur) REFERENCES tblTarifeler(tur))";
            if(tbl.equals("tblMusteriler"))
                sql="create table if not exists tblMusteriler(tel text PRIMARY KEY, adSoyad text)";
            db.execSQL(sql);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void alterTable (String tbl) {
        try {
            String [] sql=new String[2];
            if(tbl.equals("tblRandevular")) {
                sql[0]="drop table if exists tblRandevular";
                sql[1]="create table if not exists tblRandevular(randevuId integer PRIMARY KEY, tel text, tur text, tarih text, saat Text, durum text, FOREIGN KEY(tel) REFERENCES tblMusteriler(tel), FOREIGN KEY(tur) REFERENCES tblTarifeler(tur))";
            }
            if(tbl.equals("tblMusteriler")) {
                sql[0]="drop table if exists tblMusteriler";
                sql[1]="create table if not exists tblMusteriler(tel text PRIMARY KEY, adSoyad text)";
            }
            for (int i=0; i<sql.length; i++)
                db.execSQL(sql[i]);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean tarifeEkle(String kesimTur, int fiyat) { //tariflere tablosuna veri ekler
        boolean eklendiMi;
        try {
            String sql = "insert into tblTarifeler VALUES (?,?)";
            SQLiteStatement sqLiteStatement = db.compileStatement(sql);
            sqLiteStatement.bindString(1,kesimTur);
            sqLiteStatement.bindDouble(2,fiyat);
            sqLiteStatement.execute();
            eklendiMi=true;
        }
        catch (Exception e) {
            e.printStackTrace();
            eklendiMi=false;
        }
        return eklendiMi;
    }

    public boolean tarifeSil(String gelenVeri) { // tblTarifeler den ve tariflerin bulunduğu listeden veri silinmesi
        boolean silindiMi;
        try {
            String PK=gelenVeri;//Silinecek verinin primary keyinin(tarifenin adı) çekilmesi
            String [] tarifePK=PK.split("-");
            PK = tarifePK[0].trim();

            String sql = "delete from tblTarifeler where tur = ?";
            SQLiteStatement sqLiteStatement = db.compileStatement(sql);
            sqLiteStatement.bindString(1,PK);
            sqLiteStatement.execute();

            silindiMi=true;
        }
        catch (Exception e) {
            e.printStackTrace();
            silindiMi=false;
        }
        return silindiMi;
    }

    public boolean tarifeGuncelle(String tur, int fiyat, String kosul) { // tblTarifeler
        boolean guncellendiMi;
        try {
            String sql="update tblTarifeler set tur=?, fiyat=? where tur=?";
            SQLiteStatement sqLiteStatement=db.compileStatement(sql);
            sqLiteStatement.bindString(1,tur);
            sqLiteStatement.bindDouble(2,fiyat);
            sqLiteStatement.bindString(3,kosul);
            sqLiteStatement.execute();
            guncellendiMi=true;
        }
        catch (Exception e) {
            e.printStackTrace();
            guncellendiMi=false;
        }
        return guncellendiMi;
    }

    public void tarifeListele(ArrayList<String> liste, String nereye) { /* Tarifeler tablosundaki verleri listeye atar. nereye parametresi metodun hangi aktivitede kullanılacağını belirtir. Main3Activity'te bütün sütunlar listelenirken Main2Activity'de sadece kesim türleri listelenir*/
        try {
            Cursor c = db.rawQuery("select * from tblTarifeler",null);
            while(c.moveToNext()) {
                if(nereye.equals("Main3Activity"))
                    liste.add(c.getString(0)+" - "+c.getInt(1)+" TL"/**/);
                else
                    liste.add(c.getString(0));
            }
            c.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String [] listedenTarifeCek (String gelenVeri) {     // tblTarifeler için veri ayrıştırması
        gelenVeri=gelenVeri.substring(0,gelenVeri.length()-3);  // gelenVerinin sonundaki TL ve boşluk silinir

        String [] tarife = gelenVeri.split("-");
        for (int i=0; i<tarife.length; i++)
            tarife[i]=tarife[i].trim();

        return tarife;
    }

    public void musteriEkle (String tel, String adSoyad) { // tblMusteriler tablosuna veri ekler
        try {
            String sql="insert into tblMusteriler(tel, adSoyad) VALUES (?, ?)";
            SQLiteStatement sqLiteStatement=db.compileStatement(sql);
            sqLiteStatement.bindString(1,tel);
            sqLiteStatement.bindString(2,adSoyad);
            sqLiteStatement.execute();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void musteriGuncelle (String PK, String tel, String adSoyad) { // tblMusteriler tablosundan veri günceller
        try {
            String sql="update tblMusteriler set tel=?, adSoyad=? where tel=?";
            SQLiteStatement sqLiteStatement=db.compileStatement(sql);
            sqLiteStatement.bindString(1,tel);
            sqLiteStatement.bindString(2,adSoyad);
            sqLiteStatement.bindString(3,PK);
            sqLiteStatement.execute();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean randevuEkle(String tel, String tur, String tarih, String saat, String durum) {   // Randevular tablosuna veri ekler
        boolean eklendiMi;
        try {
            String [] gelenTur=tur.split("-");

            String sql="insert into tblRandevular(tel, tur, tarih, saat, durum) VALUES (?, ?, ?, ?, ?)";
            SQLiteStatement sqLiteStatement=db.compileStatement(sql);
            sqLiteStatement.bindString(1,tel);
            sqLiteStatement.bindString(2,gelenTur[0].trim());
            sqLiteStatement.bindString(3,tarih);
            sqLiteStatement.bindString(4,saat);
            sqLiteStatement.bindString(5,durum);
            sqLiteStatement.execute();
            eklendiMi=true;
        }
        catch (Exception e) {
            e.printStackTrace();
            eklendiMi=false;
        }
        return eklendiMi;
    }

    public boolean randevuGuncelle (String PK, String tel, String tur, String tarih, String saat, String durum) {   // Randevular tablosundan veri günceller
        boolean guncellendiMi;
        try {
            String [] gelenTur=tur.split("-");  // Gelen kesim türü ve fiyat bilgisinden sadece tür bilgisini kullanmak için

            String sql="update tblRandevular set tel=?, tur=?, tarih=?, saat=?, durum=? where randevuId=?";
            SQLiteStatement sqLiteStatement=db.compileStatement(sql);
            sqLiteStatement.bindString(1,tel);
            sqLiteStatement.bindString(2,gelenTur[0].trim());
            sqLiteStatement.bindString(3,tarih);
            sqLiteStatement.bindString(4,saat);
            sqLiteStatement.bindString(5,durum);
            sqLiteStatement.bindLong(6,Long.parseLong(PK));
            sqLiteStatement.execute();
            guncellendiMi=true;
        }
        catch (Exception e) {
            e.printStackTrace();
            guncellendiMi=false;
        }
        return guncellendiMi;
    }

    public boolean randevuSil (String PK) { // Randevular listesinden randevu siler
        boolean silindiMi;
        try {
            String sql = "delete from tblRandevular where randevuId = ?";
            SQLiteStatement sqLiteStatement = db.compileStatement(sql);
            sqLiteStatement.bindLong(1,Long.parseLong(PK));
            sqLiteStatement.execute();
            silindiMi=true;
        }
        catch (Exception e) {
            e.printStackTrace();
            silindiMi=false;
        }
        return silindiMi;
    }

    public boolean tarihtekiRandevularıSil (String tarih) { // Randevular listesinden tarihi verilen tüm randevuları siler
        boolean silindiMi;
        tarih=tarihFormatla(tarih,"veritabanına");
        try {
            String sql = "delete from tblRandevular where tarih = ?";
            SQLiteStatement sqLiteStatement = db.compileStatement(sql);
            sqLiteStatement.bindString(1,tarih);
            sqLiteStatement.execute();
            silindiMi=true;
        }
        catch (Exception e) {
            e.printStackTrace();
            silindiMi=false;
        }
        return silindiMi;
    }

    public void randevuTarihListele(String kriter, List<String> listeTarihler, boolean tumuGosterilsinMi) {
        /* ExpandableListView'ın grup başlıklarının oluşturulması için tblRandevulardaki tarih bilgilerini listeye aktarır. Eğer bir arama kriteri geldiyse kriteri içeren tarihleri getirir
        tarih bilgilerini listeye aktarır. Eğer bir arama kriteri geldiyse kriteri içeren tarihleri getirir. tumuGosterilsinMi ile Bugünden önceki randevuların gösterilip gösterilmeyeceği belirlenir*/
        try {
            String tarih;
            Cursor c;

            if(!kriter.equals("")) {
                if(kriter.contains("\\"))   // Eğer arama kriteri kullanıcıya gösterilen formatta tarih bilgisi ise kriteri veritabanına uygun biçimde formatlar
                    kriter=tarihFormatla(kriter,"veritabanına");

                if(tumuGosterilsinMi)
                    c = db.rawQuery("SELECT tarih from tblRandevular, tblMusteriler ,tblTarifeler WHERE tblRandevular.tel=tblMusteriler.tel AND tblTarifeler.tur=tblRandevular.tur AND ? IN(tarih, saat, adsoyad, tblRandevular.tel, tblRandevular.tur, durum) GROUP by tarih ORDER by tarih", new String[] {kriter});
                else
                    c = db.rawQuery("SELECT tarih from tblRandevular, tblMusteriler ,tblTarifeler WHERE tblRandevular.tel=tblMusteriler.tel AND tblTarifeler.tur=tblRandevular.tur AND ? IN(tarih, saat, adsoyad, tblRandevular.tel, tblRandevular.tur, durum) GROUP by tarih HAVING tarih>=? ORDER by tarih", new String[] {kriter, bugun});
            }
            else {
                if(tumuGosterilsinMi)
                    c= db.rawQuery("SELECT tarih from tblRandevular GROUP by tarih ORDER by tarih ",null);
                else
                    c= db.rawQuery("SELECT tarih from tblRandevular GROUP by tarih HAVING tarih>=? ORDER by tarih ",new String[] {bugun});
            }
            while(c.moveToNext()) {
                tarih=c.getString(0);
                tarih=tarihFormatla(tarih,"kullanıcıya");
                listeTarihler.add(tarih);
            }
            c.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void randevuSaatListele(String kriter, List<String> liste, String tarih) { /* ExpandableListView'ın grup elemanlarının içeriklerinin(randevuların kısmi detayı) atanması için. Eğer bir arama kriteri geldiyse kriteri içeren tarihleri getirir*/
        try {
            tarih=tarihFormatla(tarih,"veritabanına");
            Cursor c;
            if(!kriter.equals(""))
                c = db.rawQuery("SELECT saat, adsoyad, tblRandevular.tel from tblRandevular, tblMusteriler ,tblTarifeler WHERE tblRandevular.tel=tblMusteriler.tel AND tblTarifeler.tur=tblRandevular.tur AND tarih=? AND ? IN(tarih, saat, adsoyad, tblRandevular.tel, tblRandevular.tur, durum) ORDER by saat", new String[] {tarih, kriter});
            else
                c=db.rawQuery("SELECT saat, adsoyad, tblRandevular.tel from tblRandevular, tblMusteriler WHERE tblRandevular.tel=tblMusteriler.tel AND tarih=? ORDER by saat", new String[] {tarih});
            while (c.moveToNext())
                liste.add(c.getString(0) + "\n" + c.getString(1) + " - " + c.getString(2));
            c.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String [] randevuDetayListele(String gelenTarih, String gelenDetay, String nereye) { // Gelen tarih ve detay bilgisine göre randevu detaylarını ve turId'yi(PK) çağırır. Nereye parametresi gönderilecek detayın formatını belirler
        String [] randevuDetay = new String[2];
        String [] detaylar = gelenDetay.split("-|\\n"); //gelen saat, adSoyad ve telefon detayları - veya \n karakterlerine göre parçalanır
        String saat=detaylar[0].trim();
        String tel=detaylar[2].trim();
        gelenTarih=tarihFormatla(gelenTarih,"veritabanına");

        try {
            Cursor c = db.rawQuery("SELECT adsoyad, tblRandevular.tel, tblRandevular.tur, fiyat, durum, tarih, saat, randevuId from tblRandevular, tblMusteriler, tblTarifeler WHERE tblRandevular.tel=tblMusteriler.tel AND tblTarifeler.tur=tblRandevular.tur and tarih=? and saat=? and tblRandevular.tel=?", new String[] {gelenTarih, saat, tel});
            while(c.moveToNext()) {
                if(nereye.equals("kullaniciya")) {
                    String tarih=c.getString(5);
                    tarih=tarihFormatla(tarih,"kullanıcıya");
                    randevuDetay[0] = " Adı Soyadı: " + c.getString(0) + "\n Telefonu: " + c.getString(1) + "\n Kesim Türü: " + c.getString(2) + "\n Ücret: " + c.getString(3) + " TL\n Ödeme Durumu: " + c.getString(4) + "\n Tarih: " + tarih + "\n Saat: " + c.getString(6);
                    randevuDetay[1] = String.valueOf(c.getLong(7)); //PK
                }
                else if(nereye.equals("veritabanina")) {
                    randevuDetay[0] = c.getString(0) + "-" + c.getString(1) + "-" + c.getString(2) + "-" + c.getString(4) + "-" + c.getString(5) + "-" + c.getString(6);
                    randevuDetay[1] = String.valueOf(c.getLong(7)); //PK
                }
            }
            c.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return randevuDetay;
    }

    public String tarihFormatla(String gelenTarih, String nereye) { // Tarih bilgisini kullanıcı için kolay okunabilir veya veritabanı için kullanılabilir formata çevirir.
        String [] tarihParca;
        String tarih="";
        if(nereye.equals("kullanıcıya")) {
            tarihParca=gelenTarih.split("-");
            tarih=tarihParca[2]+"\\"+tarihParca[1]+"\\"+tarihParca[0];
        }
        else if(nereye.equals("veritabanına")) {
            tarihParca=gelenTarih.split("[^a-z0-9]"); // \ karakterine göre ayırır
            tarih=tarihParca[2].trim()+"-"+tarihParca[1].trim()+"-"+tarihParca[0].trim();
        }
        else if(nereye.equals("başına0koy")) {
            tarih=gelenTarih;
            if(tarih.length()==1)
                tarih="0"+tarih;
        }
        return tarih;
    }


}
