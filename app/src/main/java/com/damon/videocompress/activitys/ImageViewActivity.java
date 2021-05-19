package com.damon.videocompress.activitys;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.damon.videocompress.R;
//import com.google.android.gms.ads.AdListener;
//import com.google.android.gms.ads.AdRequest;
//import com.google.android.gms.ads.InterstitialAd;
//import com.google.android.gms.ads.MobileAds;
//import com.google.android.gms.ads.initialization.InitializationStatus;
//import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

import uk.co.senab.photoview.PhotoViewAttacher;

public class ImageViewActivity extends AppCompatActivity {


    private ImageView imageView;
    private String  data;
    private ImageView shared_image;

//    private InterstitialAd mInterstitialAd;

    private PhotoViewAttacher photoViewAttacher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view);

//        MobileAds.initialize(this, new OnInitializationCompleteListener() {
//            @Override
//            public void onInitializationComplete(InitializationStatus initializationStatus) {}
//        });
//        mInterstitialAd = new InterstitialAd(this);
//        mInterstitialAd.setAdUnitId(getResources().getString(R.string.interstal));
//        mInterstitialAd.loadAd(new AdRequest.Builder().build());


        imageView =findViewById(R.id.imagen_view);
        shared_image = findViewById(R.id.shared_btn);

        data = getIntent().getExtras().getString("url");

        if (data!=null){

            try {
                photoViewAttacher = new PhotoViewAttacher(imageView);
                Glide.with(this).load(data).into(imageView);
                shared_image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        shared(data);
                    }
                });
            }catch (Exception e){
                e.printStackTrace();
            }

        }else {
            Toast.makeText(this, "Error al Mostrar la imagen", Toast.LENGTH_SHORT).show();
        }


//        mInterstitialAd.setAdListener(new AdListener(){
//            @Override
//            public void onAdLoaded() {
//                super.onAdLoaded();
//                if (mInterstitialAd.isLoaded()) {
//                    mInterstitialAd.show();
//                } else {
//                    Log.d("TAG", "The interstitial wasn't loaded yet.");
//                }
//            }
//        });
    }

    private void shared(String data) {
        Intent intent  =new Intent(Intent.ACTION_SEND);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(data));
        startActivity(Intent.createChooser(intent,"Compartir Via"));
    }
}