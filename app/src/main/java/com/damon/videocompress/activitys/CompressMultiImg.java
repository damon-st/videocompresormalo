package com.damon.videocompress.activitys;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;


import com.damon.videocompress.R;
import com.damon.videocompress.adapters.ImagesMultiAdapter;
import com.damon.videocompress.utils.Util;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import id.zelory.compressor.Compressor;

public class CompressMultiImg extends AppCompatActivity {

    private ArrayList<String> data;
    private ArrayList<Uri> nuevo;
    private ArrayList<Long> id;
    private long ids;
    private Uri nuevoUri;
    private ProgressBar progressBar;
    TextView titulo,txv_porc,txt_msg_multi;
    private Button btn_compress,btn_shared_multi;
    private Bitmap bitmap;
    public Uri urisend;

    boolean saved;
    public List<Uri> datas;
    int pors=0;
    boolean completado;
    Button btn_ver;
    private RecyclerView recyclerView;
    private Dialog dialog;

    private EditText txt_height,txt_width;
    private SeekBar seekBarQuality;
    private TextView numerocalidad;
    private int width =400,height=400,quality=90;
    private Button btn_cancelar_dialog,btn_confirmar_dialog;
    private int cantidad;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compress_multi_img);

        dialog = new Dialog(this);

        datas = new ArrayList<>();
        nuevo = new ArrayList<Uri>();
        txt_msg_multi = findViewById(R.id.texto_shared_multi);
        btn_shared_multi = findViewById(R.id.btn_shared_multi);
        btn_ver = findViewById(R.id.btn_ver);
        progressBar = findViewById(R.id.progress_multi);
        progressBar.setMax(100);
        txv_porc = findViewById(R.id.porcentaje);
        titulo = findViewById(R.id.titulo);
        btn_compress = findViewById(R.id.btn_compress_multi);
        recyclerView = findViewById(R.id.recycler_img_multi);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(CompressMultiImg.this,LinearLayoutManager.HORIZONTAL,false));
        data = getIntent().getStringArrayListExtra("data");
        id = new ArrayList<>();
        initToolbar();
        if (data!=null){
            for (String s : data){
                id.add(Long.valueOf(s));
            }
            titulo.setText("Comprimir " + id.size() + " Imagenes");


            btn_compress.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    btn_compress.setEnabled(false);
                    progressBar.setProgress(0);
                    txv_porc.setText("Comprimiendo...");
//                    for (long videoId : id){
                      //  Uri uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,videoId);


                    // nuevoUri = Uri.fromFile(new File(Util.getFilePath(CompressMultiImg.this,uri)));
//                  new CompresImage().doInBackground(nuevoUri);
                    compress(id);

//                    }

                }
            });

            System.out.println(completado);
        }
            ImagesMultiAdapter imagesMultiAdapter = new ImagesMultiAdapter(this,datas);
            btn_ver.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    recyclerView.setAdapter(imagesMultiAdapter);
//                    Toast.makeText(CompressMultiImg.this, "holaaaaaaaa", Toast.LENGTH_SHORT).show();
                }
            });

            btn_shared_multi.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
                    intent.setType("image/*");
                    intent.putExtra(Intent.EXTRA_STREAM, nuevo);
                    startActivity(Intent.createChooser(intent,"COMPARTIR VIA"));
                }
            });

    }

    class MostarDIalog extends Thread{
        @Override
        public void run() {
            super.run();
            txv_porc.setText("Comprimiendo...");
        }
    }

    private void  compress(final ArrayList<Long> imageUriss){
        new Thread(){
            @Override
            public void run() {
                super.run();
                for(long videoId : imageUriss){
                    Uri imageUris = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,videoId);
                    try {
                        imageUris = Uri.fromFile(new File(Util.getFilePath(CompressMultiImg.this,imageUris)));
                        pors+=20;
                        String file_name = UUID.randomUUID() + ".jpeg";
                        OutputStream outputStream;
                        File folder;

                        bitmap = MediaStore.Images.Media.getBitmap(CompressMultiImg.this.getContentResolver(),imageUris);
                        final File tumb_filePath = new File(imageUris.getPath());
                        bitmap = BitmapFactory.decodeFile(imageUris.getPath());

//            int with = bitmap.getWidth();
//            int height = bitmap.getHeight();
//            System.out.println(with);
//            System.out.println(height);
                        try {
                            bitmap = new Compressor(CompressMultiImg.this)
                                    .setMaxWidth(400)
                                    .setMaxHeight(400)
                                    .setQuality(90)
                                    .compressToBitmap(tumb_filePath);
                        }catch (IOException e){
                            e.printStackTrace();
                        }

                        if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.Q) {
                            folder = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES)+File.separator+getString(R.string.app_name));
                        }else {
                            folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)+File.separator+getString(R.string.app_name));
                        }


                        if (!folder.exists()) {
                            folder.mkdirs();
                        }

                        File image = new File(folder+File.separator+file_name);
                        Uri imageUri = Uri.fromFile(image);
                        outputStream = new FileOutputStream(image);
                        saved =  bitmap.compress(Bitmap.CompressFormat.JPEG,90,outputStream);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            ContentResolver resolver = getContentResolver();
                            ContentValues contentValues = new ContentValues();
                            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME,file_name);
                            contentValues.put(MediaStore.MediaColumns.MIME_TYPE,"image/jpeg");
                            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH,Environment.DIRECTORY_PICTURES + File.separator + getString(R.string.app_name));
                            Uri uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,contentValues);
                            outputStream = resolver.openOutputStream(uri);
                            saved = bitmap.compress(Bitmap.CompressFormat.JPEG,90,outputStream);

                        }else{
//                        sendPictureToGallery(imageUri);
                        }

                        cantidad++;

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (saved) {
                                    progressBar.setProgress(pors);
                                    urisend = imageUri;
                                    datas.add(imageUri);
                                    nuevo.add(Uri.parse(imageUri.getPath()));
                                    txv_porc.setText("Comprimiendo "+cantidad +" de "+ imageUriss.size());
                                }else{
                                    Toast.makeText(getApplicationContext(),"no se puedo  comprimir", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });


                        outputStream.flush();
                        outputStream.close();
                        //imageUris = null;
                        //  bitmap= null;
                        image = null;
//            tumb_filePath = null;

                    } catch (Exception e) {
                        Log.e("ERROR", e.getMessage());
                    }
                }
                completado  = true;
                System.out.println(completado);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        btn_compress.setEnabled(true);
                        btn_ver.setVisibility(View.VISIBLE);
                        txt_msg_multi.setVisibility(View.VISIBLE);
                        btn_shared_multi.setVisibility(View.VISIBLE);
                        txv_porc.setText("Se ha comprimido "+cantidad+" Imagenes...");
                    }
                });


            }
        }.start();

    }


    private class CompresImage extends AsyncTask<Uri,Integer,Uri> {

        @Override
        protected void onPreExecute() {
            progressBar.setProgress(0);
            btn_compress.setEnabled(false);
            super.onPreExecute();
        }

        @SuppressLint("WrongThread")
        @Override
        protected Uri doInBackground(Uri... imageUris) {
            try {

                int count = imageUris.length;

                for (int i =0; i <count ; i++){
                    String file_name = UUID.randomUUID() + ".jpeg";
                    OutputStream outputStream;
                    boolean saved;
                    File folder;

                    bitmap = MediaStore.Images.Media.getBitmap(CompressMultiImg.this.getContentResolver(),imageUris[0]);
                    File tumb_filePath = new File(imageUris[0].getPath());
                    bitmap = BitmapFactory.decodeFile(imageUris[0].getPath());

                    int with = bitmap.getWidth();
                    int heights = bitmap.getHeight();
                    System.out.println(with);
                    System.out.println(heights);
                    try {
                        bitmap = new Compressor(CompressMultiImg.this)
                                .setMaxWidth(width)
                                .setMaxHeight(height)
                                .setQuality(quality)
                                .compressToBitmap(tumb_filePath);
                    }catch (IOException e){
                        e.printStackTrace();
                    }

                    if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.Q) {
                        folder = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES)+File.separator+getString(R.string.app_name));
                    }else {
                        folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)+File.separator+getString(R.string.app_name));
                    }


                    if (!folder.exists()) {
                        folder.mkdirs();
                    }

                    File image = new File(folder+File.separator+file_name);
                    Uri imageUri = Uri.fromFile(image);
                    outputStream = new FileOutputStream(image);
                    saved =  bitmap.compress(Bitmap.CompressFormat.JPEG,quality,outputStream);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        ContentResolver resolver = getContentResolver();
                        ContentValues contentValues = new ContentValues();
                        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME,file_name);
                        contentValues.put(MediaStore.MediaColumns.MIME_TYPE,"image/jpeg");
                        contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH,Environment.DIRECTORY_PICTURES + File.separator + getString(R.string.app_name));
                        Uri uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,contentValues);
                        outputStream = resolver.openOutputStream(uri);
                        saved = bitmap.compress(Bitmap.CompressFormat.JPEG,quality,outputStream);

                    }else{
//                        sendPictureToGallery(imageUri);
                    }
                    System.out.println(urisend);
                    if (saved) {
                        urisend = imageUri;
                        datas.add(imageUri);
                    }else{
                        Toast.makeText(CompressMultiImg.this,"no se puedo  comprimir", Toast.LENGTH_SHORT).show();
                    }
                    outputStream.flush();
                    outputStream.close();
                    publishProgress(i+1 * 100);
                    if (isCancelled()) break;
                    System.out.println(urisend);
                }
                return urisend;
            } catch (Exception e) {
                Log.e("ERROR", e.getMessage());
                return null;
            }

        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            int p = values[0];
            System.out.println(p);
            progressBar.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(Uri uris) {
            //super.onPostExecute(uris);
            System.out.println(uris);
            if (uris !=null){
                System.out.println(uris);
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.image_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.menu_settings:
                showDialogCustom();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    void initToolbar(){
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        toolbar.setTitle("COMPRIMIR IMAGENES");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    void showDialogCustom(){
        dialog.setContentView(R.layout.custom_dialog_image);
        txt_height = dialog.findViewById(R.id.alto_dialog);
        txt_width = dialog.findViewById(R.id.ancho_dialog);
        numerocalidad = dialog.findViewById(R.id.calidad_select);
        seekBarQuality = dialog.findViewById(R.id.select_calidad);
        btn_cancelar_dialog = dialog.findViewById(R.id.btn_cancelar_dialog);
        btn_confirmar_dialog = dialog.findViewById(R.id.btn_confirmar_dialog);

        btn_cancelar_dialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        numerocalidad.setText(""+quality+" %");
        seekBarQuality.setProgress(quality);
        seekBarQuality.setMax(100);
        seekBarQuality.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                numerocalidad.setText(""+progress +" %");
                quality = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        btn_confirmar_dialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int lheight = Integer.parseInt(txt_height.getText().toString());
                int lwidth = Integer.parseInt(txt_width.getText().toString());
                if (lheight >0 && lwidth >0 & quality >0) {
                    height = lheight;
                    width = lwidth;
                    Toast.makeText(CompressMultiImg.this, "BIEN AHORA DALE CLICK EN EL BOTON COMPRIMIR", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
            }
        });


        dialog.show();
    }
}