package com.amolla.hitungpengeluaran.view.fragment.pemasukan.add;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import com.amolla.hitungpengeluaran.R;
import com.amolla.hitungpengeluaran.model.ModelDatabase;
import com.amolla.hitungpengeluaran.view.MainActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddPemasukanActivity extends AppCompatActivity {

    private static String KEY_IS_EDIT = "key_is_edit";
    private static String KEY_DATA = "key_data";

    public static void startActivity(Context context, boolean isEdit, ModelDatabase pemasukan) {
        Intent intent = new Intent(context, AddPemasukanActivity.class);
        intent.putExtra(KEY_IS_EDIT, isEdit);
        intent.putExtra(KEY_DATA, pemasukan);
        context.startActivity(intent);
    }

    private AddPemasukanViewModel addPemasukanViewModel;

    private boolean mIsEdit = false;
    private String strId = "";

    Toolbar toolbar;
    TextInputEditText etKeterangan, etTanggal, etJmlUang;
    Button btnSimpan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tambah_data);

        toolbar = findViewById(R.id.toolbar);
        etKeterangan = findViewById(R.id.etKeterangan);
        etTanggal = findViewById(R.id.etTanggal);
        etJmlUang = findViewById(R.id.etJmlUang);
        btnSimpan = findViewById(R.id.btnSimpan);

        setSupportActionBar(toolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // Initialize ViewModel for Pemasukan
        addPemasukanViewModel = new ViewModelProvider(this).get(AddPemasukanViewModel.class);

        loadData();
        initAction();
    }

    private void loadData() {
        mIsEdit = getIntent().getBooleanExtra(KEY_IS_EDIT, false);
        if (mIsEdit) {
            ModelDatabase pemasukan = getIntent().getParcelableExtra(KEY_DATA);
            if (pemasukan != null) {
                strId = String.valueOf(pemasukan.uid);
                String keterangan = pemasukan.keterangan;
                String tanggal = pemasukan.tanggal;
                int uang = pemasukan.jmlUang;

                etKeterangan.setText(keterangan);
                etTanggal.setText(tanggal);
                etJmlUang.setText(String.valueOf(uang));
            }
        }
    }

    private void initAction() {
        etTanggal.setOnClickListener(view -> {
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog.OnDateSetListener date = (view1, year, monthOfYear, dayOfMonth) -> {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, monthOfYear);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                String strFormatDefault = "d MMMM yyyy";
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(strFormatDefault, Locale.getDefault());
                etTanggal.setText(simpleDateFormat.format(calendar.getTime()));
            };

            new DatePickerDialog(AddPemasukanActivity.this, date,
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)).show();
        });

        btnSimpan.setOnClickListener(v -> {
            String strTipe = "pemasukan"; // Tipe untuk data Pemasukan
            String strKeterangan = etKeterangan.getText() != null ? etKeterangan.getText().toString().trim() : "";
            String strTanggal = etTanggal.getText() != null ? etTanggal.getText().toString().trim() : "";
            String strJmlUang = etJmlUang.getText() != null ? etJmlUang.getText().toString().trim() : "";

            // Validasi input form
            if (strKeterangan.isEmpty() || strTanggal.isEmpty() || strJmlUang.isEmpty()) {
                Toast.makeText(AddPemasukanActivity.this, "Ups, form tidak boleh kosong!", Toast.LENGTH_SHORT).show();
                return;
            }

            int uang = 0;
            try {
                uang = Integer.parseInt(strJmlUang);
                if (uang <= 0) {
                    Toast.makeText(AddPemasukanActivity.this, "Jumlah uang harus lebih dari 0", Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (NumberFormatException e) {
                Toast.makeText(AddPemasukanActivity.this, "Jumlah uang harus berupa angka valid", Toast.LENGTH_SHORT).show();
                return;
            }

            if (mIsEdit) {
                // Update data di Firestore dan Room
                ModelDatabase updatedData = new ModelDatabase();
                updatedData.setUid(Integer.parseInt(strId));
                updatedData.setKeterangan(strKeterangan);
                updatedData.setTanggal(strTanggal);
                updatedData.setJmlUang(uang);
                updatedData.setTipe(strTipe);
                addPemasukanViewModel.updatePemasukan(updatedData);
            } else {
                FirebaseFirestore firestore = FirebaseFirestore.getInstance();
                int finalUang = uang;
                firestore.collection("pemasukan")
                        .orderBy("uid", Query.Direction.DESCENDING)
                        .limit(1)
                        .get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            int newUid = 1;
                            if (!queryDocumentSnapshots.isEmpty()) {
                                Long lastUid = queryDocumentSnapshots.getDocuments().get(0).getLong("uid");
                                newUid = lastUid != null ? lastUid.intValue() + 1 : 1;
                            }

                            // Data baru untuk Pemasukan
                            ModelDatabase newData = new ModelDatabase();
                            newData.setUid(newUid);
                            newData.setKeterangan(strKeterangan);
                            newData.setTanggal(strTanggal);
                            newData.setJmlUang(finalUang);
                            newData.setTipe(strTipe);

                            // Simpan data ke Firestore
                            firestore.collection("pemasukan")
                                    .document(String.valueOf(newUid))
                                    .set(newData)
                                    .addOnSuccessListener(aVoid -> {
                                        addPemasukanViewModel.addPemasukanToLocalDatabase(newData);
                                        Toast.makeText(AddPemasukanActivity.this, "Data berhasil ditambahkan", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(AddPemasukanActivity.this, "Gagal menambahkan data", Toast.LENGTH_SHORT).show();
                                    });
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(AddPemasukanActivity.this, "Gagal mengambil UID terakhir: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            }
            finish();  // Memanggil finish setelah data benar-benar disimpan
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            startActivity(new Intent(AddPemasukanActivity.this, MainActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

