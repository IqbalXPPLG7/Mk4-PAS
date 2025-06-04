package com.amolla.hitungpengeluaran.view.fragment.pengeluaran.add;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.amolla.hitungpengeluaran.R;
import com.amolla.hitungpengeluaran.model.ModelDatabase;
import com.amolla.hitungpengeluaran.view.MainActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddPengeluaranActivity extends AppCompatActivity {

    private static String KEY_IS_EDIT = "key_is_edit";
    private static String KEY_DATA = "key_data";

    public static void startActivity(Context context, boolean isEdit, ModelDatabase pengeluaran) {
        Intent intent = new Intent(new Intent(context, AddPengeluaranActivity.class));
        intent.putExtra(KEY_IS_EDIT, isEdit);
        intent.putExtra(KEY_DATA, pengeluaran);
        context.startActivity(intent);
    }

    private AddPengeluaranViewModel addPengeluaranViewModel;

    private boolean mIsEdit = false;
    private int strUid = 0;

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

        addPengeluaranViewModel = new ViewModelProvider(this).get(AddPengeluaranViewModel.class);

        loadData();
        initAction();
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

            new DatePickerDialog(AddPengeluaranActivity.this, date,
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)).show();
        });

        btnSimpan.setOnClickListener(v -> {
            String strTipe = "pengeluaran";
            String strKeterangan = etKeterangan.getText().toString();
            String strTanggal = etTanggal.getText().toString();
            String strJmlUang = etJmlUang.getText().toString();

            if (strKeterangan.isEmpty() || strTanggal.isEmpty() || strJmlUang.isEmpty()) {
                Toast.makeText(AddPengeluaranActivity.this, "Ups, form tidak boleh kosong!", Toast.LENGTH_SHORT).show();
                return;
            }

            int uang = 0;
            try {
                uang = Integer.parseInt(strJmlUang);
                if (uang <= 0) {
                    Toast.makeText(AddPengeluaranActivity.this, "Jumlah uang harus lebih dari 0", Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (NumberFormatException e) {
                Toast.makeText(AddPengeluaranActivity.this, "Jumlah uang harus berupa angka valid", Toast.LENGTH_SHORT).show();
                return;
            }

            if (mIsEdit) {
                // Update pengeluaran
                ModelDatabase updatedData = new ModelDatabase();
                updatedData.setUid(strUid);
                updatedData.setKeterangan(strKeterangan);
                updatedData.setTanggal(strTanggal);
                updatedData.setJmlUang(uang);
                updatedData.setTipe(strTipe);

                addPengeluaranViewModel.updatePengeluaran(updatedData);
            } else {
                FirebaseFirestore firestore = FirebaseFirestore.getInstance();
                int finalUang = uang;
                firestore.collection("pengeluaran")
                        .orderBy("uid", Query.Direction.DESCENDING)
                        .limit(1)
                        .get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            int newUid;
                            if (!queryDocumentSnapshots.isEmpty()) {
                                Long lastUid = queryDocumentSnapshots.getDocuments().get(0).getLong("uid");
                                newUid = lastUid != null ? lastUid.intValue() + 1 : 1;
                            } else {
                                newUid = 1;
                            }

                            // Simpan data pengeluaran ke firestore
                            ModelDatabase newData = new ModelDatabase();
                            newData.setUid(newUid);
                            newData.setKeterangan(strKeterangan);
                            newData.setTanggal(strTanggal);
                            newData.setJmlUang(finalUang);
                            newData.setTipe(strTipe);

                            firestore.collection("pengeluaran")
                                    .document(String.valueOf(newUid))
                                    .set(newData)
                                    .addOnSuccessListener(aVoid -> {
                                        addPengeluaranViewModel.addPengeluaranToLocalDatabase(newData);
                                        Toast.makeText(AddPengeluaranActivity.this, "Data berhasil ditambahkan", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(AddPengeluaranActivity.this, "Gagal menambahkan data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(AddPengeluaranActivity.this, "Gagal mengambil UID terakhir: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            }

            finish();
        });
    }

    private void loadData() {
        mIsEdit = getIntent().getBooleanExtra(KEY_IS_EDIT, false);
        if (mIsEdit) {
            ModelDatabase pengeluaran = getIntent().getParcelableExtra(KEY_DATA);
            if (pengeluaran != null) {
                strUid = pengeluaran.uid;
                String keterangan = pengeluaran.keterangan;
                String tanggal = pengeluaran.tanggal;
                int uang = pengeluaran.jmlUang;

                etKeterangan.setText(keterangan);
                etTanggal.setText(tanggal);
                etJmlUang.setText(String.valueOf(uang));
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            startActivity(new Intent(AddPengeluaranActivity.this, MainActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
