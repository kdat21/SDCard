package com.example.sdcard;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
    private ArrayList<String> fileList;
    private ArrayAdapter<String> adapter;
    private String currentPath;

    private String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf(".");
        if (dotIndex > 0) {
            return fileName.substring(dotIndex + 1);
        } else {
            return "";
        }
    }

    private String readFile(File file) {
        StringBuilder text = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return text.toString();
    }

    private void showTextFileContent(File file) {
        TextView textView = new TextView(this);
        textView.setText(readFile(file));
        new AlertDialog.Builder(this)
                .setTitle(file.getName())
                .setView(textView)
                .setPositiveButton("OK", null)
                .show();
    }

    private void showImageFileContent(File file) {
        ImageView imageView = new ImageView(this);
        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
        imageView.setImageBitmap(bitmap);
        new AlertDialog.Builder(this)
                .setTitle(file.getName())
                .setView(imageView)
                .setPositiveButton("OK", null)
                .show();
    }

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListView listView = findViewById(R.id.list_view);
        currentPath = Environment.getExternalStorageDirectory().getAbsolutePath();

        fileList = new ArrayList<>();
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath());
        File[] files = file.listFiles();

        for (File f : files) {
            fileList.add(f.getName());
        }

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, fileList);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String fileName = fileList.get(position);
                File clickedFile = new File(currentPath + "/" + fileName);
                Log.d("currentPath", currentPath + "/" + fileName);
                if (clickedFile.isDirectory()) {
                    currentPath = clickedFile.getAbsolutePath();
                    fileList = new ArrayList<String>(Arrays.asList(clickedFile.list()));
                    adapter.clear();
                    adapter.addAll(fileList);
                } else {
                    String fileExtension = getFileExtension(fileName);
                    Log.d("fileExtension", fileExtension);
                    if (fileExtension.equals("txt")) {
                        showTextFileContent(clickedFile);
                    } else if (fileExtension.equals("jpg") || fileExtension.equals("bmp") || fileExtension.equals("png")) {
                        showImageFileContent(clickedFile);
                    } else {
                        Toast.makeText(getApplicationContext(), "Unsupported file format", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_options, menu);
        return true;
    }
}