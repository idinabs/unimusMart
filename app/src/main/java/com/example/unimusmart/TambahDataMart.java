package com.example.unimusmart;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class TambahDataMart extends AppCompatActivity {

    Button btn_tambah_produk;
    EditText namaBarang, deskripsiBarang, jumlahBarang, hargaBarang;
    ShapeableImageView img_sampul_product;
    FirebaseStorage storage;
    FirebaseFirestore firestore;
    CollectionReference reference;
    Uri imageUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tambah_data_mart);

        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        reference = firestore.collection("unimus_mart_produk");

        instance_input_tools_product();
        input_product_add();

    }

    private void instance_input_tools_product() {

        img_sampul_product = findViewById(R.id.img_sampul);
        namaBarang = findViewById(R.id.namaBarang);
        deskripsiBarang = findViewById(R.id.deskripsiBarang);
        jumlahBarang = findViewById(R.id.jumlahBarang);
        hargaBarang = findViewById(R.id.hargaBarang);
        btn_tambah_produk = findViewById(R.id.btn_tambah_produk);

    }

    private void input_product_add() {

        img_sampul_product.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SampulpickImage();
            }
        });

        btn_tambah_produk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                uploadImages(view);
            }
        });
    }

//
//    private void verifyPermissionAndPickImageSampul() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
//                SampulpickImage();
//            } else {
//                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
//            }
//        } else {
//
//        }
//    }

    private void SampulpickImage() {
        Intent openGalleryIntent = new Intent(Intent.ACTION_PICK);
        startActivityForResult(openGalleryIntent,1);
    }

    private void uploadImages(View view) {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);
        progressDialog.show();

        saveImageDataToFirestore(progressDialog);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1:
                if(resultCode == Activity.RESULT_OK){
                    imageUri = data.getData();
                    //profileImage.setImageURI(imageUri);
                    Picasso.get().load(imageUri).into(img_sampul_product);
                }
        }
    }


    private void saveImageDataToFirestore(final ProgressDialog progressDialog) {
        progressDialog.setMessage("Saving Data...");
        final StorageReference fileRef  = FirebaseStorage.getInstance().getReference("unimus_mart_produk").child("Image " + System.currentTimeMillis());


        fileRef.putFile(imageUri)
            .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                uri = uri;

                                String nama = namaBarang.getText().toString().trim();
                                String deskripsi = deskripsiBarang.getText().toString().trim();
                                String jumlah = jumlahBarang.getText().toString().trim();
                                String harga = hargaBarang.getText().toString().trim();

                                Map<String, Object> dataMap = new HashMap<>();
                                dataMap.put("nama", nama);
                                dataMap.put("deskripsi", deskripsi);
                                dataMap.put("jumlah", jumlah);
                                dataMap.put("harga", harga);
                                dataMap.put("uri", uri);

                                reference.add(dataMap).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                    @Override
                                    public void onSuccess(DocumentReference documentReference) {
                                        progressDialog.dismiss();
                                        Toast.makeText(TambahDataMart.this, "Data saved successfully!", Toast.LENGTH_SHORT).show();

                                        Intent intent = new Intent(TambahDataMart.this, MainActivity.class);
                                        startActivity(intent);
                                        finish();

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        progressDialog.dismiss();
                                        Toast.makeText(TambahDataMart.this, "Data uploaded but we couldn't save them to database.", Toast.LENGTH_SHORT).show();
                                        Log.e("MainActivity:SaveData", e.getMessage());

                                        Intent intent = new Intent(TambahDataMart.this, MainActivity.class);
                                        startActivity(intent);
                                    }
                                });

                            }
                        });
                    }
                });
    }


}