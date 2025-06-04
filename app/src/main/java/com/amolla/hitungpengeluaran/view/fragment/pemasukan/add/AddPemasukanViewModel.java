package com.amolla.hitungpengeluaran.view.fragment.pemasukan.add;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import com.amolla.hitungpengeluaran.database.DatabaseClient;
import com.amolla.hitungpengeluaran.database.dao.DatabaseDao;
import com.amolla.hitungpengeluaran.model.ModelDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.functions.Action;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class AddPemasukanViewModel extends AndroidViewModel {

    private DatabaseDao databaseDao;

    public AddPemasukanViewModel(@NonNull Application application) {
        super(application);
        databaseDao = DatabaseClient.getInstance(application).getAppDatabase().databaseDao();
    }

    // Update data pemasukan di Firestore dan Room Database
    public void updatePemasukan(final ModelDatabase pemasukan) {
        Completable.fromAction(new Action() {
                    @Override
                    public void run() throws Exception {
                        // Update data in Room Database
                        databaseDao.updateDataPemasukan(pemasukan.getKeterangan(), pemasukan.getTanggal(), pemasukan.getJmlUang(), pemasukan.getUid());

                        // Update data in Firestore
                        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
                        firestore.collection("pemasukan")
                                .document(String.valueOf(pemasukan.getUid()))
                                .update("keterangan", pemasukan.getKeterangan(), "tanggal", pemasukan.getTanggal(), "jmlUang", pemasukan.getJmlUang())
                                .addOnSuccessListener(aVoid -> {
                                    // Successfully updated both Room and Firestore
                                    Log.d("AddPemasukanViewModel", "Data successfully updated in Firestore and Room");
                                })
                                .addOnFailureListener(e -> {
                                    // Failed to update data in Firestore
                                    Log.e("AddPemasukanViewModel", "Failed to update data in Firestore: " + e.getMessage());
                                });
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();
    }

    public void addPemasukanToLocalDatabase(ModelDatabase pemasukan) {
        Completable.fromAction(() -> databaseDao.insertPemasukan(pemasukan))  // Insert ke Room Database
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    Log.d("AddPemasukanViewModel", "Data berhasil disimpan ke Room Database");
                }, throwable -> {
                    Log.e("AddPemasukanViewModel", "Gagal menyimpan data ke Room Database: " + throwable.getMessage());
                });
    }
}
