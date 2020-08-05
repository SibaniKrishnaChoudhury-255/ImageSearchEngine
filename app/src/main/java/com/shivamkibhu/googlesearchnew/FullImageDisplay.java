package com.shivamkibhu.googlesearchnew;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;

public class FullImageDisplay extends AppCompatActivity {

    private ImageViewTouch fullImage;
    private ImageView downloadImg;
    private String imageUrl = "";
    boolean connected = false;
    CoordinatorLayout coordinatorLayout;
    LinearLayout downloadLinear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_image_display);

        init();

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            //we are connected to a network
            connected = true;
        } else
            connected = false;

        if (!imageUrl.equals(""))
            Glide.with(this).load(imageUrl).into(fullImage);

        downloadLinear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!connected)
                    Toast.makeText(FullImageDisplay.this, "Please check your internet connection!!", Toast.LENGTH_SHORT).show();
                else if (!imageUrl.equals(""))
                    DownloadImage(imageUrl);
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 0) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
            }
        }
    }

    private void init() {
        fullImage = findViewById(R.id.fullImage);
        downloadImg = findViewById(R.id.downloadBtn);
        coordinatorLayout = findViewById(R.id.coordinator);
        downloadLinear = findViewById(R.id.linearDownload);

        imageUrl = getIntent().getExtras().get("url").toString();

    }

    private void DownloadImage(String ImageUrl) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE}, 123);
        } else {
            //TODO: check net connection

            Toast.makeText(this, "Downloading Imgae...", Toast.LENGTH_SHORT).show();
            //Asynctask to create a thread to downlaod image in the background
            new DownloadsImage().execute(ImageUrl);
        }
    }

    class DownloadsImage extends AsyncTask<String, Void, Void> {
        File path;
        Bitmap bm = null;
        File imageFile;

        @Override
        protected Void doInBackground(String... strings) {
            URL url = null;

            try {
                url = new URL(strings[0]);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

            try {
                bm = BitmapFactory.decodeStream(url.openConnection().getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }

            //Create Path to save Image
            path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + "/ShivamKibhu"); //Creates app specific folder

            if (!path.exists()) {
                path.mkdirs();
            }

            imageFile = new File(path, String.valueOf(System.currentTimeMillis()) + ".png"); // Imagename.png
            FileOutputStream out = null;

            try {
                out = new FileOutputStream(imageFile);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            try {
                bm.compress(Bitmap.CompressFormat.PNG, 100, out); // Compress Image
                out.flush();
                out.close();
                // Tell the media scanner about the new file so that it is
                // immediately available to the user.
                MediaScannerConnection.scanFile(FullImageDisplay.this, new String[]{imageFile.getAbsolutePath()}, null, new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                        // Log.i("ExternalStorage", "Scanned " + path + ":");
                        //    Log.i("ExternalStorage", "-> uri=" + uri);

                    }
                });
            } catch (Exception e) {
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            if (bm != null && imageFile.isFile()) {
                Snackbar snackbar = Snackbar.make(coordinatorLayout, "Downloaded successfully..", Snackbar.LENGTH_LONG)
                        .setAction("Open", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                MediaScannerConnection.scanFile(FullImageDisplay.this, new String[]{imageFile.toString()}, null,
                                        new MediaScannerConnection.OnScanCompletedListener() {
                                            @Override
                                            public void onScanCompleted(String path, Uri uri) {
                                                Intent viewImageInGallery = new Intent(Intent.ACTION_VIEW);
                                                viewImageInGallery.setDataAndType(uri, "image/*");
                                                FullImageDisplay.this.startActivityForResult(viewImageInGallery, 1);
                                            }
                                        });

                            }
                        });

                View snackView = snackbar.getView();
                TextView msg = snackView.findViewById(com.google.android.material.R.id.snackbar_text);
                msg.setTextColor(ContextCompat.getColor(FullImageDisplay.this, R.color.snackbarMssg));

                snackbar.show();

            } else
                Toast.makeText(FullImageDisplay.this, "Error while downloading", Toast.LENGTH_SHORT).show();
        }
    }
}


