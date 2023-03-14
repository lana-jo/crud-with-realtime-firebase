package com.example.curd;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements MainAdapter.FirebasedataListener {
//inisiasi variabel yang digunakan
    FirebaseStorage storage;
    StorageReference storageReference;
    Dialog dialog;
    DialogInterface dialogInterface;
    ExtendedFloatingActionButton floatingActionButton;
    EditText editNama, editMerk, editHarga;
    Button btnpilihfoto;
    ImageView imageView;
    RecyclerView recyclerView;
    MainAdapter mainAdapter;
    ArrayList<ModelBarang> daftarBarang;
    DatabaseReference databaseReference;
    FirebaseDatabase firebaseDatabase;

    // Uri indicates, where the image will be picked from
    Uri filePath;

    // request code
    final int PICK_IMAGE_REQUEST = 22;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//untuk membuat layout semakin berwarna, tidak statis
        if (Build.VERSION.SDK_INT >= 19 && Build.VERSION.SDK_INT < 21) {
            setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, true);
        }
        if (Build.VERSION.SDK_INT >= 19) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            );
        }
        if (Build.VERSION.SDK_INT >= 21) {
            setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        //menjelaskan varibel yang akan ditambpilkan
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
//menegaskan bahwa layout yang digunakan adalah ini
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
//konfigurasi aplikasi dengan firebaseapp
        FirebaseApp.initializeApp(this);
//pembuatan file yang bernama images pada storage database firebase
        storageReference = storage.getInstance().getReference("images");
//inisiasi untuk penyimpanan database pada firebase
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference(" barang");
        //membuat inputan pada saat menyimpan di firebase realtime
        databaseReference.child("data_barang").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
//data diambil dari daftarBarang yang terdapat pada file ModelBarang
                daftarBarang = new ArrayList<>();
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    ModelBarang barang = dataSnapshot1.getValue(ModelBarang.class);
                    barang.setKey(dataSnapshot1.getKey());
                    daftarBarang.add(barang);
                }
                //data yang akan ditampilkan melalui proses adaptor
                mainAdapter = new MainAdapter(MainActivity.this, daftarBarang);
                recyclerView.setAdapter(mainAdapter);
            }
//notifikasi jika eror dalam melakukan akses database
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MainActivity.this,
                        databaseError.getDetails() + "" + databaseError.getMessage(), Toast.LENGTH_LONG)
                        .show();
            }
        });
        //inisiasi variabel
        floatingActionButton = findViewById(R.id.tambah_barang);
        floatingActionButton.setOnClickListener(view -> dialogTambahBarang());
    }
//inisiasi tampilan mengambang
    public static void setWindowFlag(@NonNull Activity mainActivity, final int bits, boolean on) {

        Window win = mainActivity.getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();

        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }
//dialog untuk melakukan update dan delete
    public void onDataClick(final ModelBarang barang, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pilih Aksi");
//dialog untuk update barang
        builder.setPositiveButton("UPDATE", (dialog, id) -> dialogUpdateBarang(barang));
//dialog untuk hapus barang
        builder.setNegativeButton("DELETE", (dialog, id) -> hapusDataBarang(barang));
        //dialog untuk batal melakukan perintah
        builder.setNeutralButton("BATAL", (dialog, id) -> dialog.dismiss());
        Dialog dialog = builder.create();
        dialog.show();
    }
//class untuk melakukan create, merujuk pada floatingbutton
    private void dialogTambahBarang() {
        //tampilan berbentuk dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //judul tampilan
        builder.setTitle("TAMBAH DATA MODEL BARANG");
        //ditampilkan di layout edit barang
        View view = getLayoutInflater().inflate(R.layout.layout_edit_barang, null);

        //inisiasi variabel yang digunakan
        editNama = view.findViewById(R.id.nama_barang);
        editMerk = view.findViewById(R.id.merk_barang);
        editHarga = view.findViewById(R.id.harga_barang);
        btnpilihfoto = view.findViewById(R.id.pilihFoto);
        imageView = view.findViewById(R.id.image);
        btnpilihfoto.setOnClickListener(v -> SelectImage());
        builder.setView(view);

//perintah menambahkan tombol simpan dan aktifitas simpan crud firebase
        builder.setPositiveButton("SIMPAN", (dialog, id) -> {
//inisiasi variabel untuk mengirim data ke firebase
            String namaBarang = editNama.getText().toString();
            String merkBarang = editMerk.getText().toString();
            String hargaBarang = editHarga.getText().toString();
            String foto = filePath.getPath();
//variabel yang akan dikirim dikoreksi dahulu apakah ada yang kosong, datanya merujuk pada file model barang yang berguna untuk inisiasi database
            if (!namaBarang.isEmpty() && !merkBarang.isEmpty() && !hargaBarang.isEmpty() && !foto.isEmpty()) {
                //jika tidak kosong maka data akan dikirim ke database dengan  format yang ada pada kelas submitDataBarang
                submitDataBarang(new ModelBarang(namaBarang, merkBarang, hargaBarang, foto));
            }
            //inisiasi untuk mengirim foto
            if (filePath!=null){
                ProgressDialog progressDialog = new ProgressDialog(this);
                progressDialog.setTitle("Upload");
                progressDialog.show();
                //mengirim data berupa foto ke storage firebase

                StorageReference reference = storageReference.child("images/"
                        + UUID.randomUUID().toString());
                reference.putFile(filePath)
                        .addOnSuccessListener(taskSnapshot -> {
                                    // Image telah berhasil diupdate
                                    // menghilangkan dialog ketika telah sukses
                                    progressDialog.dismiss();
                                    Toast.makeText(MainActivity.this,
                                            "Image Uploaded!!",
                                            Toast.LENGTH_SHORT)
                                            .show();});

            }
            //notifikasi ketika tidak diisi
            else {
                Toast.makeText(MainActivity.this, "Harus Diisi!", Toast.LENGTH_SHORT).show();
            }
        });
//membuat tombol batal dan menghilangkan notifikasi untuk upload foto
        builder.setNegativeButton("BATAL", (dialogInterface, id) -> dialogInterface.dismiss());
        dialog = builder.create();
        dialog.show();
    }
    //mengambil data dari penyimpanan lokal
    private void SelectImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(
                Intent.createChooser(
                        intent,
                        "Select Image from here..."),
                PICK_IMAGE_REQUEST);
    }
    //ketika menjalankan perintah mengambil foto dari penyimapanan lokal
    @Override
    protected void onActivityResult(int requestCode,int resultCode,Intent data){
        super.onActivityResult(requestCode,resultCode,data);

        // sedang melakukan cek apa yang dilakukan pada permintaan kode dan hasilnya
        // jika kode permintaan dari PICK_IMAGE_REQUEST dan
        // hasil dari RESULT_OK
        // maka foto akan ditampilkan di image view
        if (requestCode == PICK_IMAGE_REQUEST
                && resultCode == RESULT_OK
                && data != null
                && data.getData() != null) {

            // mengambil data menggunakan Uri
            filePath = data.getData();
            try {
                // mengatur gampar pada imageview menggunakan bitmap
                Bitmap bitmap = MediaStore
                        .Images
                        .Media
                        .getBitmap(
                                getContentResolver(),
                                filePath);
                imageView.setImageBitmap(bitmap);

            }

            catch (IOException e) {
                // menampilkan log yang sedang dilakukan
                e.printStackTrace();
            }
        }
    }
//mengirim data ke firebase
    void submitDataBarang(final ModelBarang barang) {
        //data yang disimpan dengan judul data_barang pada folder barang
        databaseReference.child("data_barang").push().setValue(barang)
                .addOnSuccessListener(this, V -> Toast.makeText(MainActivity.this, "Data Barang Berhasil Disimpan!", Toast.LENGTH_LONG).show());
    }
//class untuk perintah update barang
    private void dialogUpdateBarang(final ModelBarang barang) {
//        inisiasi menampilkan dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //judul ketika layout dilihat
        builder.setTitle("Edit Data ModelBarang");
        //ditampilkan pada layout_edit_barang
        View view = getLayoutInflater().inflate(R.layout.layout_edit_barang, null);
//inisiasi variabel
        editNama = view.findViewById(R.id.nama_barang);
        editMerk = view.findViewById(R.id.merk_barang);
        editHarga = view.findViewById(R.id.harga_barang);
        btnpilihfoto = view.findViewById(R.id.pilihFoto);
        imageView = view.findViewById(R.id.image);
//mengambil data sebelum diupdate
        editNama.setText(barang.getNama());
        editMerk.setText(barang.getMerk());
        editHarga.setText(barang.getHarga());
        btnpilihfoto.setOnClickListener(v -> SelectImage());
//menampilkan pada variabel view pada class ini
        builder.setView(view);
//menyimpan data yang akan diupdate yang sesuai pada kelaas updateBarang
        if (barang != null) {
            builder.setPositiveButton("SIMPAN", (dialogInterface, i) -> {
                barang.setNama(editNama.getText().toString());
                barang.setMerk(editMerk.getText().toString());
                barang.setHarga(editHarga.getText().toString());
                updateBarang(barang);
            });
        }
//membatalkan dialog update
        builder.setNegativeButton("BATAL", (dialogInterface, i) -> dialogInterface.dismiss());
        Dialog dialog = builder.create();
        dialog.show();
    }

//class untuk melaksanakan perintah update barang
    private void updateBarang(ModelBarang barang) {
        //data yang disimpan dengan judul data_barang pada folder barang
        databaseReference.child("data_barang").child(Objects.requireNonNull(barang.getKey())).setValue(barang)
                .addOnSuccessListener(this, unused -> Toast.makeText(MainActivity.this, "Data Barang Berhasil DiUpdate!", Toast.LENGTH_LONG).show());
    }

//class untuk melaksanakan perintah hapus barang
    private void hapusDataBarang(ModelBarang barang) {
        if (databaseReference != null) {
            databaseReference.child("data_barang").child(Objects.requireNonNull(barang.getKey())).removeValue()
                    .addOnSuccessListener(this, unused -> Toast.makeText(MainActivity.this, "Data Telah Berhasil Dihapus!", Toast.LENGTH_LONG).show());
        }
    }

}
