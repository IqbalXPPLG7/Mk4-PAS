package com.amolla.hitungpengeluaran.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

@Entity(tableName = "tbl_keuangan")
public class ModelDatabase implements Parcelable {

    @PrimaryKey(autoGenerate = true)
    public int uid;

    @ColumnInfo(name = "tipe")
    public String tipe;



    @ColumnInfo(name = "keterangan")
    public String keterangan;

    @ColumnInfo(name = "document_id")
    public String documentId;

    @ColumnInfo(name = "jml_uang")
    public int jmlUang;

    @ColumnInfo(name = "tanggal")
    public String tanggal;

    public ModelDatabase() {
    }

    // Getter & Setter uid
    public int getUid() {
        return uid;
    }

    public String getDocumentId() {
        return documentId;
    }



    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    // Getter & Setter tipe
    public String getTipe() {
        return tipe;
    }

    public void setTipe(String tipe) {
        this.tipe = tipe;
    }

    // Getter & Setter keterangan
    public String getKeterangan() {
        return keterangan;
    }

    public void setKeterangan(String keterangan) {
        this.keterangan = keterangan;
    }

    // Getter & Setter jmlUang
    public int getJmlUang() {
        return jmlUang;
    }

    public void setJmlUang(int jmlUang) {
        this.jmlUang = jmlUang;
    }

    // Getter & Setter tanggal
    public String getTanggal() {
        return tanggal;
    }

    public void setTanggal(String tanggal) {
        this.tanggal = tanggal;
    }

    // Method static untuk dapatkan tanggal sekarang
    public static String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(new Date());
    }

    // Parcelable implementation (tetap seperti kode kamu)
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.uid);
        dest.writeString(this.tipe);
        dest.writeString(this.keterangan);
        dest.writeInt(this.jmlUang);
        dest.writeString(this.tanggal);
        dest.writeString(this.documentId);
    }


    protected ModelDatabase(Parcel in) {
        this.uid = in.readInt();
        this.tipe = in.readString();
        this.keterangan = in.readString();
        this.jmlUang = in.readInt();
        this.tanggal = in.readString();
        this.documentId = in.readString();
    }

    public static final Creator<ModelDatabase> CREATOR = new Creator<ModelDatabase>() {
        @Override
        public ModelDatabase createFromParcel(Parcel source) {
            return new ModelDatabase(source);
        }

        @Override
        public ModelDatabase[] newArray(int size) {
            return new ModelDatabase[size];
        }
    };

    public ModelDatabase(int uid, String tipe, String keterangan, String tanggal, int jmlUang) {
        this.uid = uid;
        this.tipe = tipe;
        this.keterangan = keterangan;
        this.tanggal = tanggal;
        this.jmlUang = jmlUang;
    }

}


