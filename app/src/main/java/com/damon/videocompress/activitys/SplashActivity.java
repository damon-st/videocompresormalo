package com.damon.videocompress.activitys;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.widget.Toast;

import com.damon.videocompress.R;

public class SplashActivity extends AppCompatActivity {


    private final int DURACION_SPLASH = 1000;

    boolean boolean_permission;
    public static int REQUEST_PERMISSIONS = 1;
    private boolean saltar = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //el splsh_activity ay que remplazar con la actividad de esta clase
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if (ContextCompat.checkSelfPermission(SplashActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(SplashActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(SplashActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, REQUEST_PERMISSIONS);
            }
            fn_permission();
        }
        SharedPreferences preferences = getSharedPreferences("permisos", Context.MODE_PRIVATE);
        final String user = preferences.getString("permisos","no existe");
        if (user.equals("si")){
            new Handler().postDelayed(new Runnable(){
                public void run(){             //remplazamos con la actividad  y remplazamos con la actividad ala que queremos ir
                    Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();

                };
            }, DURACION_SPLASH);
        }else {
            Toast.makeText(this, "Por favor consede los permisos necesarios", Toast.LENGTH_SHORT).show();
        }


    }

    @TargetApi(Build.VERSION_CODES.O)
    private void fn_permission() {
        if ((ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) &&
                (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) && (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
                && ActivityCompat.checkSelfPermission(SplashActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)  {

            if ((ActivityCompat.shouldShowRequestPermissionRationale(SplashActivity.this, android.Manifest.permission.READ_EXTERNAL_STORAGE))) {
            } else {
                ActivityCompat.requestPermissions(SplashActivity.this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_PERMISSIONS);

            }

            if ((ActivityCompat.shouldShowRequestPermissionRationale(SplashActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE))) {
            } else {
                ActivityCompat.requestPermissions(SplashActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_PERMISSIONS);

            }

            if ((ActivityCompat.shouldShowRequestPermissionRationale(SplashActivity.this, Manifest.permission.CAMERA))) {
            } else {
                ActivityCompat.requestPermissions(SplashActivity.this, new String[]{Manifest.permission.CAMERA},
                        REQUEST_PERMISSIONS);

                if (REQUEST_PERMISSIONS == ContextCompat.checkSelfPermission(SplashActivity.this,
                        Manifest.permission.CAMERA));
            }
        } else {
            coloSeleccionadoGuaradado("si");
            saltar  =true;
            boolean_permission = true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && requestCode == REQUEST_PERMISSIONS ){
            saltar = true;
            coloSeleccionadoGuaradado("si");
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public void coloSeleccionadoGuaradado(String colorSeleccionado ){
        SharedPreferences color  = getSharedPreferences("permisos", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = color.edit();
        editor.putString("permisos",colorSeleccionado);
        editor.apply();
    }
}