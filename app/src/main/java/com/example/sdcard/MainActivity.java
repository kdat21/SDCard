package com.example.sdcard;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
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
    private ArrayList<String> fileListName;
    private ArrayList<File> fileList;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private String currentPath;

    private void getAllFiles() {
        fileListName = new ArrayList<>();
        fileList = new ArrayList<>();
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath());
        File[] files = file.listFiles();

        for (File f : files) {
            fileListName.add(f.getName());
            fileList.add(f);
        }
    }

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

    private void showRenameDialog(final File file) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Đổi tên");
        builder.setMessage("Nhập tên mới cho tệp tin hoặc thư mục:");

        final EditText input = new EditText(this);
        input.setText(file.getName());
        builder.setView(input);

        builder.setPositiveButton("Lưu", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newName = input.getText().toString().trim();

                if (newName.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Tên không được để trống", Toast.LENGTH_SHORT).show();
                    return;
                }

                File newFile = new File(file.getParent(), newName);

                if (newFile.exists()) {
                    Toast.makeText(MainActivity.this, "Đã có tệp tin hoặc thư mục có tên này", Toast.LENGTH_SHORT).show();
                    return;
                }

                boolean success = file.renameTo(newFile);

                if (!success) {
                    Toast.makeText(MainActivity.this, "Không thể đổi tên", Toast.LENGTH_SHORT).show();
                }

                fileList.clear();
                getAllFiles();
                adapter.notifyDataSetChanged();
            }
        });

        builder.setNegativeButton("Hủy", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void showDeleteDialog(final File file) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Xóa");
        builder.setMessage("Bạn có chắc chắn muốn xóa \"" + file.getName() + "\" không?");
        builder.setPositiveButton("Có", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (!file.isDirectory()) {
                    file.delete();
                }
                fileList.clear();
                getAllFiles();
                adapter.notifyDataSetChanged();
            }
        });
        builder.setNegativeButton("Không", null);
        builder.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = findViewById(R.id.list_view);
        currentPath = Environment.getExternalStorageDirectory().getAbsolutePath();

        getAllFiles();

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, fileListName);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String fileName = fileListName.get(position);
                File clickedFile = new File(currentPath + "/" + fileName);
                if (clickedFile.isDirectory()) {
                    currentPath = clickedFile.getAbsolutePath();
                    fileListName = new ArrayList<String>(Arrays.asList(clickedFile.list()));
                    adapter.clear();
                    adapter.addAll(fileListName);
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

        registerForContextMenu(listView);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        String itemName = fileList.get(info.position).getName();
        menu.setHeaderTitle(itemName);

        if(!fileList.get(info.position).isDirectory())
            getMenuInflater().inflate(R.menu.context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int menuItemIndex = item.getItemId();
        Log.d("menuIndex", String.valueOf(menuItemIndex));
        if (!fileList.get(info.position).isDirectory()) {
            if (menuItemIndex == R.id.context_menu_rename) {
                showRenameDialog(fileList.get(info.position));
            } else if (menuItemIndex == R.id.context_menu_delete) {
                showDeleteDialog(fileList.get(info.position));
            } else if (menuItemIndex == R.id.context_menu_copy) {
//                showCopyFileDialog(fileList.get(info.position));
            }
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.new_folder:
                showNewFolderDialog();
                return true;
            case R.id.new_file:
                showNewFileDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showNewFolderDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Tạo thư mục mới");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("Tạo", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String folderName = input.getText().toString();
                if (!TextUtils.isEmpty(folderName)) {
                    File newFolder = new File(Environment.getExternalStorageDirectory(), folderName);
                    if (newFolder.mkdirs()) {
                        Toast.makeText(MainActivity.this, "Tạo thư mục mới thành công", Toast.LENGTH_SHORT).show();
                        fileList.clear();
                        getAllFiles();
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(MainActivity.this, "Tạo thư mục mới thất bại", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        builder.setNegativeButton("Hủy", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    // Hiển thị dialog để người dùng nhập tên file mới và tạo file mới
    private void showNewFileDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Tạo file mới");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("Tạo", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String fileName = input.getText().toString();
                if (!TextUtils.isEmpty(fileName)) {
                    File newFile = new File(Environment.getExternalStorageDirectory(), fileName + ".txt");
                    try {
                        if (newFile.createNewFile()) {
                            Toast.makeText(MainActivity.this, "Tạo file mới thành công", Toast.LENGTH_SHORT).show();
                            fileList.clear();
                            getAllFiles();
                            adapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(MainActivity.this, "Tạo file mới thất bại", Toast.LENGTH_SHORT).show();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        builder.setNegativeButton("Hủy", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }
}