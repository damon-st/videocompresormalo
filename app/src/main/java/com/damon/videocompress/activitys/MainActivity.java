package com.damon.videocompress.activitys;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.damon.videocompress.R;
import com.damon.videocompress.adapters.TabsAccessorAdapter;
import com.damon.videocompress.fragments.VideoList;
import com.damon.videocompress.utils.VideoCompressSilliCompressor;
//import com.google.android.gms.ads.AdRequest;
//import com.google.android.gms.ads.AdSize;
//import com.google.android.gms.ads.AdView;
//import com.google.android.gms.ads.MobileAds;
//import com.google.android.gms.ads.initialization.InitializationStatus;
//import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.material.tabs.TabLayout;
import com.theartofdev.edmodo.cropper.CropImage;



import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class MainActivity extends AppCompatActivity {

    private Button buscar,buscarvideo;
    private Uri imageUris;
    public static int REQUEST_PERMISSIONS = 1;
    boolean boolean_permission;
    Bitmap bitmap;


    String mCurrentPhotoPath;
    Uri capturedUri = null;
    final private int SELECT_VIDEO_REQUEST_CODE = 129;
    private static final int TYPE_VIDEO = 2;
    private static final int TYPE_IMAGE = 1;
    VideoCompressSilliCompressor videoCompress;
    public static final String FILE_PROVIDER_AUTHORITY = ".provider";

    private TabLayout myTabLayout;
    //cremaos un objeto de la clase tabsaccesadapter que tiene los items y title de los fragments
    private TabsAccessorAdapter myTabsAccessorAdapter;
    private ViewPager myViewPager;


//    private AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, 1000);
        }
        fn_permission();
//        MobileAds.initialize(this,
//                getResources().getString(R.string.app_id_admob));
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



        buscar = findViewById(R.id.buscar);
        buscarvideo = findViewById(R.id.buscarvideo);

        buscar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity(imageUris)
                        .start(MainActivity.this);
            }
        });

        buscarvideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent();
//                intent.setAction(Intent.ACTION_GET_CONTENT);
//                intent.addCategory(Intent.CATEGORY_OPENABLE);
//                intent.setType("video/mp4");
//                startActivityForResult(intent.createChooser(intent, "Select PDF File"), 438);

                new  Thread(new Runnable() {
                    @Override
                    public void run() {
//                        new VideoPicker.Builder(MainActivity.this)
//                                .mode(VideoPicker.Mode.CAMERA_AND_GALLERY)
//                                .directory(VideoPicker.Directory.DEFAULT)
//                                .extension(VideoPicker.Extension.MP4)
//                                .enableDebuggingMode(true)
//                                .build();
//
//                Intent intent = new Intent();
//                intent.setAction(Intent.ACTION_GET_CONTENT);
//                intent.addCategory(Intent.CATEGORY_OPENABLE);
//                intent.setType("video/mp4");
//                startActivityForResult(intent.createChooser(intent, "Select PDF File"), 438);
                        Intent intent1 = new Intent(MainActivity.this, VideoList.class);
                        startActivity(intent1);
                    }
                }).start();



//                Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
//        takeVideoIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//        if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
//            try {
//                takeVideoIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
//                takeVideoIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT,15);
//                capturedUri = FileProvider.getUriForFile(MainActivity.this,
//                        getPackageName() + FILE_PROVIDER_AUTHORITY,
//                        createMediaFile(TYPE_VIDEO));
//
//                takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, capturedUri);
//                startActivityForResult(takeVideoIntent, SELECT_VIDEO_REQUEST_CODE);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//        }


            }
        });

        myViewPager = findViewById(R.id.main_tabs_pager);//inicializamos el viewpager
        myTabsAccessorAdapter = new TabsAccessorAdapter(getSupportFragmentManager(),this);
        myViewPager.setAdapter(myTabsAccessorAdapter);//aqui pasamos el parametro de la calse adapter


        myTabLayout = findViewById(R.id.main_tabs);//esta tabla crea los 3 bloques contacto chats etc
        myTabLayout.setupWithViewPager(myViewPager);//aqui pasamos la vista que tendra cada tabla




    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        if (resultCode != RESULT_CANCELED){
//            if (requestCode == 438&& resultCode == RESULT_OK && data !=null ){
//
//                File f = new File( Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES) + "/VideoCompress/videos");
//                Uri uri = data.getData();
//                Cursor returnCursor =
//                        getContentResolver().query(uri, null, null, null, null);
//
//                int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
//                int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
//                returnCursor.moveToFirst();
//                System.out.println(sizeIndex);
//                System.out.println(getPath(uri));
//                File tumb_filePath = new File(uri.getPath());
//                System.out.println(tumb_filePath.getAbsolutePath());
//                returnCursor.close();
//
////                try {
////                    videoCompress = new VideoCompressSilliCompressor(MainActivity.this, new AsyncResponse() {
////                        @Override
////                        public void processFinish(String output) {
////                            System.out.println(output);
////                        }
////                    });
////                    videoCompress.execute(tumb_filePath.getAbsolutePath(),f.getPath());
////                } catch (Exception e) {
////                    e.printStackTrace();
////                }
////            List<String> mPaths = data.getStringArrayListExtra(VideoPicker.EXTRA_VIDEO_PATH);
////            File f = new File( Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES) + "/PetBacker/videos");
////            for (int i =0 ; i< mPaths.size() ; i++){
////                Uri uri = Uri.parse(mPaths.get(i));
////                try {
////                    videoCompress = new VideoCompressSilliCompressor(MainActivity.this, new AsyncResponse() {
////                        @Override
////                        public void processFinish(String output) {
////                            System.out.println(output);
////                        }
////                    });
////                    videoCompress.execute(uri.getPath(),f.getPath());
////                } catch (Exception e) {
////                    e.printStackTrace();
////                }
////
////            }
//
//            }else
//                if (requestCode ==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE&&resultCode ==RESULT_OK&&data!=null){
//                CropImage.ActivityResult result = CropImage.getActivityResult(data);
//                imageUris = result.getUri();//aqui recueramos
//                try {
//                    String file_name = UUID.randomUUID() + ".jpeg";
//                    OutputStream outputStream;
//                    boolean saved;
//                    File folder;
//
//                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(),imageUris);
//                    File tumb_filePath = new File(imageUris.getPath());
//                    bitmap = BitmapFactory.decodeFile(imageUris.getPath());
//
//                    int with = bitmap.getWidth();
//                    int height = bitmap.getHeight();
//                    System.out.println(with);
//                    System.out.println(height);
//                    try {
//                        bitmap = new Compressor(this)
//                                .setMaxWidth(350)
//                                .setMaxHeight(350)
//                                .setQuality(80)
//                                .compressToBitmap(tumb_filePath);
//                    }catch (IOException e){
//                        e.printStackTrace();
//                    }
//
//                    if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.Q) {
//                        folder = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES)+File.separator+getString(R.string.app_name));
//                    }else {
//                        folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)+File.separator+getString(R.string.app_name));
//                    }
//
//
//                    if (!folder.exists()) {
//                        folder.mkdirs();
//                    }
//
//                    File image = new File(folder+File.separator+file_name);
//                    Uri imageUri = Uri.fromFile(image);
//                    outputStream = new FileOutputStream(image);
//                    saved =  bitmap.compress(Bitmap.CompressFormat.JPEG,80,outputStream);
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//                        ContentResolver resolver = getContentResolver();
//                        ContentValues contentValues = new ContentValues();
//                        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME,file_name);
//                        contentValues.put(MediaStore.MediaColumns.MIME_TYPE,"image/jpeg");
//                        contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH,Environment.DIRECTORY_PICTURES + File.separator + getString(R.string.app_name));
//                        Uri uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,contentValues);
//                        outputStream = resolver.openOutputStream(uri);
//                        saved = bitmap.compress(Bitmap.CompressFormat.JPEG,80,outputStream);
//
//                    }else{
//                        sendPictureToGallery(imageUri);
//                    }
//                    if (saved) {
//                        Toast.makeText(this,"picture save", Toast.LENGTH_SHORT).show();
//
//                    }else{
//                        Toast.makeText(this,"picture not  save", Toast.LENGTH_SHORT).show();
//
//                    }
//
//                    outputStream.flush();
//                    outputStream.close();
//                    imageUris = null;
//                    bitmap= null;
//                    image = null;
//                    tumb_filePath = null;
//
//                } catch (Exception e) {
//                    Log.e("ERROR", e.getMessage());
//                }
//            }
//        }
//
//    }

    @TargetApi(Build.VERSION_CODES.O)
    private void fn_permission() {
        if ((ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) ||
                (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) || (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) )  {

            if ((ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, android.Manifest.permission.READ_EXTERNAL_STORAGE))) {
            } else {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_PERMISSIONS);

            }

            if ((ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE))) {
            } else {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_PERMISSIONS);

            }

            if ((ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.CAMERA))) {
            } else {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA},
                        REQUEST_PERMISSIONS);

                if (REQUEST_PERMISSIONS == ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.CAMERA));
            }
        } else {
            boolean_permission = true;


        }
    }


    public String getPath(Uri uri) {
        String[] projection = { MediaStore.Video.Media.DATA };
        getContentResolver();
        Cursor cursor = getApplicationContext().getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } else
            return null;
    }

    private void sendPictureToGallery(Uri imageuri){
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        intent.setData(imageuri);
        sendBroadcast(intent);
    }

    private File createMediaFile(int type) throws IOException {

        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String fileName = (type == TYPE_IMAGE) ? "JPEG_" + timeStamp + "_" : "VID_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                type == TYPE_IMAGE ? Environment.DIRECTORY_PICTURES : Environment.DIRECTORY_MOVIES);
        File file = File.createTempFile(
                fileName,  /* prefix */
                type == TYPE_IMAGE ? ".jpg" : ".mp4",         /* suffix */
                storageDir      /* directory */
        );

        // Get the path of the file created
        mCurrentPhotoPath = file.getAbsolutePath();
        Log.d( "mCurrentPhotoPath: ", mCurrentPhotoPath);
        return file;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Glide.get(getApplicationContext()).clearMemory();
//        Glide.get(getApplicationContext()).clearDiskCache();
        clearApplicationData();
        getCacheDir().deleteOnExit();
        deleteCache(this);
//        if (Build.VERSION_CODES.KITKAT <= Build.VERSION.SDK_INT) {
//            ((ActivityManager) getSystemService(ACTIVITY_SERVICE))
//                    .clearApplicationUserData();
//            return;
//        }
    }

    public static void deleteCache(Context context) {
        try {
            File dir = context.getCacheDir();
            deleteDirs(dir);
        } catch (Exception e) {}
    }
    public static boolean deleteDirs(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
            return dir.delete();
        } else if(dir!= null && dir.isFile()) {
            return dir.delete();
        } else {
            return false;
        }
    }


    public void clearApplicationData() {
        File cache = getCacheDir();
        File appDir = new File(cache.getParent());
        if (appDir.exists()) {
            String[] children = appDir.list();
            for (String s : children) {
                if (!s.equals("lib")) {
                    deleteDir(new File(appDir, s));
                    Log.i("EEEEEERRRRRROOOOOOORRRR", "**************** File /data/data/APP_PACKAGE/" + s + " DELETED *******************");
                }
            }
        }
    }

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            int i = 0;
            while (i < children.length) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
                i++;
            }
        }

        assert dir != null;
        return dir.delete();
    }

//    public static void deleteCache(Context context) {
//        try {
//            File dir = context.getCacheDir();
//            deleteDir(dir);
//        } catch (Exception e) { e.printStackTrace();}
//    }
//
//    public static boolean deleteDir(File dir) {
//        if (dir != null && dir.isDirectory()) {
//            String[] children = dir.list();
//            for (int i = 0; i < children.length; i++) {
//                boolean success = deleteDir(new File(dir, children[i]));
//                if (!success) {
//                    return false;
//                }
//            }
//            return dir.delete();
//        } else if(dir!= null && dir.isFile()) {
//            return dir.delete();
//        } else {
//            return false;
//        }
//    }
}