package com.amolla.hitungpengeluaran.view.fragment.pemasukan;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.amolla.hitungpengeluaran.database.DatabaseClient;
import com.amolla.hitungpengeluaran.database.dao.DatabaseDao;
import com.amolla.hitungpengeluaran.model.ModelDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.functions.Action;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class PemasukanViewModel extends AndroidViewModel {

    private LiveData<List<ModelDatabase>> mPemasukan;
    private DatabaseDao databaseDao;
    private LiveData<Integer> mTotalPrice;

    public PemasukanViewModel(@NonNull Application application) {
        super(application);

        databaseDao = DatabaseClient.getInstance(application).getAppDatabase().databaseDao();
        mPemasukan = databaseDao.getAllPemasukan();
        mTotalPrice = databaseDao.getTotalPemasukan();
    }

    public LiveData<List<ModelDatabase>> getPemasukan() {
        return mPemasukan;
    }

    public LiveData<Integer> getTotalPemasukan() {
        return mTotalPrice;
    }

    public void deleteAllData() {
        Completable.fromAction(new Action() {
                    @Override
                    public void run() throws Exception {
                        databaseDao.deleteAllPemasukan();
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();
    }

    public String deleteSingleData(final int uid) {
        String sKeterangan;
        try {
            Completable.fromAction(() -> {
                        databaseDao.deleteSinglePemasukan(uid);  // Penghapusan dari Room Database
                    })
                    .subscribeOn(Schedulers.io())  // Pastikan ini berjalan di background thread
                    .observeOn(AndroidSchedulers.mainThread())  // Update UI setelah selesai
                    .subscribe();

            sKeterangan = "OK";
        } catch (Exception e) {
            sKeterangan = "NO";
            Log.e("PemasukanViewModel", "Error deleting data: " + e.getMessage());
        }
        return sKeterangan;
    }
    public void deletePemasukanFromFirestoreByUid(int uid) {
        // Cari documentId berdasarkan uid dari Firestore
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection("pemasukan")
                .whereEqualTo("uid", uid) // Mencari berdasarkan uid
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Ambil documentId dari hasil pencarian
                        String documentId = queryDocumentSnapshots.getDocuments().get(0).getId();

                        // Hapus data di Firestore menggunakan documentId
                        firestore.collection("pemasukan").document(documentId)
                                .delete()
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("PemasukanViewModel", "Data berhasil dihapus dari Firestore");
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("PemasukanViewModel", "Gagal menghapus data dari Firestore: " + e.getMessage());
                                });
                    } else {
                        Log.e("PemasukanViewModel", "Data dengan uid " + uid + " tidak ditemukan di Firestore");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("PemasukanViewModel", "Gagal mencari documentId berdasarkan uid: " + e.getMessage());
                });
    }

}
