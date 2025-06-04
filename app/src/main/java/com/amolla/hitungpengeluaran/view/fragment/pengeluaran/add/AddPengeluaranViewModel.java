    package com.amolla.hitungpengeluaran.view.fragment.pengeluaran.add;

    import android.app.Application;

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

        public class AddPengeluaranViewModel extends AndroidViewModel {

            private DatabaseDao databaseDao;

            public AddPengeluaranViewModel(@NonNull Application application) {
                super(application);
                databaseDao = DatabaseClient.getInstance(application).getAppDatabase().databaseDao();
            }
        public void updatePengeluaran(final ModelDatabase pengeluaran) {
            Completable.fromAction(new Action() {
                        @Override
                        public void run() throws Exception {
                            // Update data di Room Database
                            databaseDao.updateDataPengeluaran(pengeluaran.getKeterangan(), pengeluaran.getTanggal(), pengeluaran.getJmlUang(), pengeluaran.getUid());

                            // Update data di Firestore
                            FirebaseFirestore firestore = FirebaseFirestore.getInstance();
                            firestore.collection("pengeluaran") // Collection berbeda untuk pengeluaran
                                    .document(String.valueOf(pengeluaran.getUid()))
                                    .update("keterangan", pengeluaran.getKeterangan(),
                                            "tanggal", pengeluaran.getTanggal(),
                                            "jmlUang", pengeluaran.getJmlUang())
                                    .addOnSuccessListener(aVoid -> {
                                        // Berhasil memperbarui data di Firestore dan Room
                                    })
                                    .addOnFailureListener(e -> {
                                        // Gagal memperbarui data di Firestore
                                    });
                        }
                    })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe();
        }

        // Menambahkan data pengeluaran ke Room Database saja
        public void addPengeluaranToLocalDatabase(ModelDatabase pengeluaran) {
            Completable.fromAction(() -> databaseDao.insertPengeluaran(pengeluaran))  // Insert ke Room Database
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(() -> {
                        // Berhasil menyimpan data ke Room Database
                    }, throwable -> {
                        // Gagal menyimpan data ke Room Database
                    });
        }
    }
