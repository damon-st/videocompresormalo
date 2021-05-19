package com.damon.videocompress.activitys;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;
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
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.damon.videocompress.R;
import com.damon.videocompress.utils.Util;
//import com.google.android.gms.ads.AdRequest;
//import com.google.android.gms.ads.AdSize;
//import com.google.android.gms.ads.AdView;
//import com.google.android.gms.ads.MobileAds;
//import com.google.android.gms.ads.initialization.InitializationStatus;
//import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import id.zelory.compressor.Compressor;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class CompressImageActitivy extends AppCompatActivity {


    long videoId;

    private Bitmap bitmap;


    private ImageView imagenAntes,imagenAhora;
    private TextView textoFinalizado,texto_imagen_ahora;

    String  uri,nuevo_uri;

    boolean caso;

    Uri nuevoUri,otra_uri;

    private ImageButton btn_shared;

    private Button comprimir,btn_buscar;
    private ProgressBar progressBar;
    boolean list_;
//    private AdView mAdView;

    private Dialog dialog;

    private EditText txt_height,txt_width;
    private SeekBar seekBarQuality;
    private TextView numerocalidad;
    private int width =400,height=400,quality=90;
    private Button btn_cancelar_dialog,btn_confirmar_dialog;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compress_image_actitivy);

        if (ContextCompat.checkSelfPermission(CompressImageActitivy.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(CompressImageActitivy.this, Manifest.permission.CAMERA) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(CompressImageActitivy.this, Manifest.permission.READ_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(CompressImageActitivy.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA,Manifest.permission.READ_EXTERNAL_STORAGE}, 1000);
        }

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        toolbar.setTitle("COMPRIMIR IMAGEN");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        dialog = new Dialog(this);

//        AdView adView = new AdView(this);
//        adView.setAdSize(AdSize.BANNER);
//        adView.setAdUnitId(getString(R.string.banner));
//
//        MobileAds.initialize(this, new OnInitializationCompleteListener() {
//            @Override
//            public void onInitializationComplete(InitializationStatus initializationStatus) {
//            }
//        });
//        mAdView = findViewById(R.id.adView);
//        AdRequest adRequest = new AdRequest.Builder().build();
//        mAdView.loadAd(adRequest);



        caso = getIntent().getExtras().getBoolean("caso");
        btn_buscar = findViewById(R.id.btn_buscar);
        imagenAntes = findViewById(R.id.imagen_uno);
        imagenAhora = findViewById(R.id.imagen_dos);
        textoFinalizado = findViewById(R.id.texto_finalizado);
        btn_shared  =findViewById(R.id.shared);
        comprimir = findViewById(R.id.btn_comprimir);
        texto_imagen_ahora = findViewById(R.id.texto_dos);
        progressBar = findViewById(R.id.progress_image);

        if (caso){
            String  s = getIntent().getExtras().getString("videoId");
            nuevoUri = Uri.fromFile(new File(s));
            Glide.with(this).load(nuevoUri).into(imagenAntes);
        }else {
            videoId = getIntent().getExtras().getLong("videoId");
            Uri videoUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, videoId);

            Glide.with(this).load(videoUri).into(imagenAntes);

            try {
                nuevoUri = Uri.fromFile(new File(Util.getFilePath(this,videoUri)));
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }

        imagenAntes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent  =new Intent(CompressImageActitivy.this, ImageViewActivity.class);
                intent.putExtra("url",nuevoUri.getPath());
                startActivity(intent);
            }
        });


        btn_shared.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedImage(nuevoUri.getPath());
            }
        });

        comprimir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                comprimir.setEnabled(false);
                btn_buscar.setEnabled(false);
                texto_imagen_ahora.setVisibility(View.VISIBLE);
                texto_imagen_ahora.setText("Comprimiendo Imagen Espere.....");
                compress(nuevoUri);
                Glide.with(CompressImageActitivy.this).load(nuevoUri.getPath()).into(imagenAhora);

//                CompresImage compresImage = new CompresImage();
//                compresImage.execute(nuevoUri);

//                compimirDos();
            }
        });

        imagenAhora.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CompressImageActitivy.this,ImageViewActivity.class);
                intent.putExtra("url",uri);
                startActivity(intent);
            }
        });

        btn_buscar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity(otra_uri).start(CompressImageActitivy.this);
            }
        });
    }
    boolean saved;
    private void  compress(final Uri imageUris){
        new Thread(){
            @Override
            public void run() {
                super.run();
                try {
                    System.out.println(width);
                    System.out.println(height);
                    String file_name = UUID.randomUUID() + ".jpeg";
                    OutputStream outputStream;
                    File folder;

                    bitmap = MediaStore.Images.Media.getBitmap(CompressImageActitivy.this.getContentResolver(),imageUris);
                    final File tumb_filePath = new File(imageUris.getPath());
                    bitmap = BitmapFactory.decodeFile(imageUris.getPath());

//            int with = bitmap.getWidth();
//            int height = bitmap.getHeight();
//            System.out.println(with);
//            System.out.println(height);
                    try {
                        bitmap = new Compressor(CompressImageActitivy.this)
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
                        sendPictureToGallery(imageUri);
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (saved) {
                                texto_imagen_ahora.setText("!EXITO IMAGEN COMPRIMIDA");
                                comprimir.setEnabled(true);
                                btn_buscar.setEnabled(true);
                                uri = String.valueOf(imageUri.getPath());
                                btn_shared.setVisibility(View.VISIBLE);
                                textoFinalizado.setVisibility(View.VISIBLE);
                                textoFinalizado.setText("COMPRESSION COMPLETA");
                            }else{
                                Toast.makeText(getApplicationContext(),"no se puedo  comprimir", Toast.LENGTH_SHORT).show();
                                textoFinalizado.setVisibility(View.VISIBLE);
                                textoFinalizado.setText("NOSE PUDO COMPRIMIR");
                            }
                        }
                    });


                    outputStream.flush();
                    outputStream.close();
                    //imageUris = null;
                    bitmap= null;
                    image = null;
//            tumb_filePath = null;

                } catch (Exception e) {
                    Log.e("ERROR", e.getMessage());
                }
            }
        }.start();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK && data !=null){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            nuevoUri = result.getUri();
            Glide.with(this).load(nuevoUri).into(imagenAntes);
        }
    }

    private void sendPictureToGallery(Uri imageuri){
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        intent.setData(imageuri);
        sendBroadcast(intent);
    }

    protected void SharedImage(String  absolutePath){
        Intent compartirAudio = new Intent(android.content.Intent.ACTION_SEND);
        compartirAudio.setType("image/*");
        compartirAudio.putExtra(Intent.EXTRA_STREAM,
                Uri.parse(absolutePath)); //En getPackageName() da error por no estar definido
        startActivity(Intent.createChooser(compartirAudio, "Compartir vía"));
    }
    Uri urisend;
    class CompresImage extends AsyncTask<Uri,Integer,Uri> {
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

                    bitmap = MediaStore.Images.Media.getBitmap(CompressImageActitivy.this.getContentResolver(),imageUris[0]);
                    File tumb_filePath = new File(imageUris[0].getPath());
                    bitmap = BitmapFactory.decodeFile(imageUris[0].getPath());

                    int with = bitmap.getWidth();
                    int height = bitmap.getHeight();
                    System.out.println(with);
                    System.out.println(height);
                    try {
                        bitmap = new Compressor(CompressImageActitivy.this)
                                .setMaxWidth(350)
                                .setMaxHeight(350)
                                .setQuality(80)
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
                    saved =  bitmap.compress(Bitmap.CompressFormat.JPEG,80,outputStream);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        ContentResolver resolver = getContentResolver();
                        ContentValues contentValues = new ContentValues();
                        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME,file_name);
                        contentValues.put(MediaStore.MediaColumns.MIME_TYPE,"image/jpeg");
                        contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH,Environment.DIRECTORY_PICTURES + File.separator + getString(R.string.app_name));
                        Uri uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,contentValues);
                        outputStream = resolver.openOutputStream(uri);
                        saved = bitmap.compress(Bitmap.CompressFormat.JPEG,80,outputStream);

                    }else{
                        sendPictureToGallery(imageUri);
                    }
                    System.out.println(urisend);
                    if (saved) {
                        texto_imagen_ahora.setVisibility(View.VISIBLE);
                        comprimir.setEnabled(true);
                        urisend = imageUri;
                        uri = String.valueOf(imageUri.getPath());
                        btn_shared.setVisibility(View.VISIBLE);
                        textoFinalizado.setVisibility(View.VISIBLE);
                        textoFinalizado.setText("COMPRESSION COMPLETA");
                        Glide.with(CompressImageActitivy.this).load(bitmap).into(imagenAhora);
                    }else{
                        Toast.makeText(CompressImageActitivy.this,"no se puedo  comprimir", Toast.LENGTH_SHORT).show();
                        textoFinalizado.setVisibility(View.VISIBLE);
                        textoFinalizado.setText("NOSE PUDO COMPRIMIR");
                    }
                    outputStream.flush();
                    outputStream.close();
                    publishProgress(i * 100);
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
             progressBar.setProgress(p);
         }

        @Override
        protected void onPostExecute(Uri uris) {
            //super.onPostExecute(uris);
            System.out.println(uris);
            if (uris !=null){
                uri  = uris.getPath();
                Glide.with(CompressImageActitivy.this).load(uris).into(imagenAhora);
            }
        }

    }



    private void compimirDos(){
        Observable.create(new ObservableOnSubscribe<Uri>() {
            @Override
            public void subscribe(ObservableEmitter<Uri> e) throws Exception {
                e.onNext(nuevoUri);
                e.onComplete();
            }
        }).subscribeOn(Schedulers.newThread())
          .observeOn(AndroidSchedulers.mainThread())
          .subscribe(new Observer<Uri>() {
              @Override
              public void onSubscribe(Disposable d) {
                  System.out.println("Disposable "+d);
              }

              @Override
              public void onNext(Uri imageUris) {
                  try {
                      System.out.println(width);
                      System.out.println(height);
                      String file_name = UUID.randomUUID() + ".jpeg";
                      OutputStream outputStream;
                      boolean saved;
                      File folder;

                      bitmap = MediaStore.Images.Media.getBitmap(CompressImageActitivy.this.getContentResolver(),imageUris);
                      File tumb_filePath = new File(imageUris.getPath());
                      bitmap = BitmapFactory.decodeFile(imageUris.getPath());

//            int with = bitmap.getWidth();
//            int height = bitmap.getHeight();
//            System.out.println(with);
//            System.out.println(height);
                      try {
                          bitmap = new Compressor(CompressImageActitivy.this)
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
                          sendPictureToGallery(imageUri);
                      }
                      if (saved) {
                          texto_imagen_ahora.setVisibility(View.VISIBLE);
                          comprimir.setEnabled(true);
                          btn_buscar.setEnabled(true);
                          uri = String.valueOf(imageUri.getPath());
                          btn_shared.setVisibility(View.VISIBLE);
                          textoFinalizado.setVisibility(View.VISIBLE);
                          textoFinalizado.setText("COMPRESSION COMPLETA");
                          Glide.with(CompressImageActitivy.this).load(bitmap).into(imagenAhora);
                      }else{
                          Toast.makeText(CompressImageActitivy.this,"no se puedo  comprimir", Toast.LENGTH_SHORT).show();
                          textoFinalizado.setVisibility(View.VISIBLE);
                          textoFinalizado.setText("NOSE PUDO COMPRIMIR");
                      }
                      outputStream.flush();
                      outputStream.close();
                      imageUris = null;
                      bitmap= null;
                      image = null;
                      tumb_filePath = null;

                  } catch (Exception e) {
                      Log.e("ERROR", e.getMessage());
                  }
              }

              @Override
              public void onError(Throwable e) {
                  System.out.println("Disposable "+e.getMessage());
              }

              @Override
              public void onComplete() {
                  System.out.println("Complete ");
              }
          });
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            finish();
        else if (item.getItemId() == R.id.menu_settings)
            showDialogCusttom();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.image_menu,menu);
        return true;
    }

    private void showDialogCusttom() {
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
                    Toast.makeText(CompressImageActitivy.this, "BIEN AHORA DALE CLICK EN EL BOTON COMPRIMIR", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
            }
        });


        dialog.show();
    }

    private String compressImage(Uri imageUri, File destDirectory) {

        String filePath = imageUri.getPath();
        Bitmap scaledBitmap = null;

        BitmapFactory.Options options = new BitmapFactory.Options();

//      by setting this field as true, the actual bitmap pixels are not loaded in the memory. Just the bounds are loaded. If
//      you try the use the bitmap here, you will get null.
        options.inJustDecodeBounds = true;
        Bitmap bmp = BitmapFactory.decodeFile(filePath, options);

        int actualHeight = options.outHeight;
        int actualWidth = options.outWidth;

//      max Height and width values of the compressed image is taken as 816x612

        float maxHeight = 816.0f;
        float maxWidth = 612.0f;
        float imgRatio = actualWidth / actualHeight;
        float maxRatio = maxWidth / maxHeight;

//      width and height values are set maintaining the aspect ratio of the image

        if (actualHeight > maxHeight || actualWidth > maxWidth) {
            if (imgRatio < maxRatio) {
                imgRatio = maxHeight / actualHeight;
                actualWidth = (int) (imgRatio * actualWidth);
                actualHeight = (int) maxHeight;
            } else if (imgRatio > maxRatio) {
                imgRatio = maxWidth / actualWidth;
                actualHeight = (int) (imgRatio * actualHeight);
                actualWidth = (int) maxWidth;
            } else {
                actualHeight = (int) maxHeight;
                actualWidth = (int) maxWidth;

            }
        }

//      setting inSampleSize value allows to load a scaled down version of the original image

        options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight);

//      inJustDecodeBounds set to false to load the actual bitmap
        options.inJustDecodeBounds = false;

//      this options allow android to claim the bitmap memory if it runs low on memory
        options.inPurgeable = true;
        options.inInputShareable = true;
        options.inTempStorage = new byte[16 * 1024];

        try {
//          load the bitmap from its path
            bmp = BitmapFactory.decodeFile(filePath, options);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();

        }
        try {
            scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();
        }

        float ratioX = actualWidth / (float) options.outWidth;
        float ratioY = actualHeight / (float) options.outHeight;
        float middleX = actualWidth / 2.0f;
        float middleY = actualHeight / 2.0f;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bmp, middleX - bmp.getWidth() / 2, middleY - bmp.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));

//      check the rotation of the image and display it properly
        ExifInterface exif;
        try {
            exif = new ExifInterface(filePath);

            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, 0);
            Log.d("EXIF", "Exif: " + orientation);
            Matrix matrix = new Matrix();
            if (orientation == 6) {
                matrix.postRotate(90);
                Log.d("EXIF", "Exif: " + orientation);
            } else if (orientation == 3) {
                matrix.postRotate(180);
                Log.d("EXIF", "Exif: " + orientation);
            } else if (orientation == 8) {
                matrix.postRotate(270);
                Log.d("EXIF", "Exif: " + orientation);
            }
            scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0,
                    scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix,
                    true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        FileOutputStream out = null;
        String filename = getFilename(imageUri.getPath(), destDirectory);
        try {
            out = new FileOutputStream(filename);

//          write the compressed bitmap at the destination specified by filename.
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return filename;

    }

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        final float totalPixels = width * height;
        final float totalReqPixelsCap = reqWidth * reqHeight * 2;
        while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
            inSampleSize++;
        }

        return inSampleSize;
    }

    private String getFilename(String filename, File file) {
        if (!file.exists()) {
            file.mkdirs();
        }
        String ext = ".jpg";
        //get extension
        /*if (Pattern.matches("^[.][p][n][g]", filename)){
            ext = ".png";
        }*/

        return (file.getAbsolutePath() + "/IMG_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ext);

    }
}