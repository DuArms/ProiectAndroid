package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileUtils;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private int index;
    private String copyPath;

    private Button deleteButton = null;
    private Button renameButton = null;
    private Button copyButton = null;
    private Button pasteButton = null;

    private Button createNewFolderButton = null;
    private Button refreshButtom = null;
    private Button goBackButtom = null;

    private String rootPath = null;

    private File[] files = null;
    private List<String> filesList = null;

    private ListView listView = null;
    private LinearLayout bottomBar = null;

    private TextAdapter adapter = null;
    private TextView path = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout1);

        path = findViewById(R.id.pathOutput);

        deleteButton = findViewById(R.id.delete);
        renameButton = findViewById(R.id.rename);
        copyButton = findViewById(R.id.copy);
        pasteButton = findViewById(R.id.paste);
        refreshButtom = findViewById(R.id.refresh);
        goBackButtom = findViewById(R.id.goBack);


        createNewFolderButton = findViewById(R.id.newFolder);

        listView = findViewById(R.id.listView);
        bottomBar = findViewById(R.id.bottomBar);

        createNewFolderButton.setOnClickListener(v -> {
            final AlertDialog.Builder newFolderDialog = new AlertDialog.Builder(MainActivity.this);
            newFolderDialog.setTitle("New Folder");
            final EditText input = new EditText(MainActivity.this);

            input.setInputType(InputType.TYPE_CLASS_TEXT);

            newFolderDialog.setView(input);

            newFolderDialog.setPositiveButton("OK", (dialog, which) -> {
                final File newFolder = new File(rootPath + File.separator + input.getText().toString());
                if (!newFolder.exists()) {
                    newFolder.mkdir();
                }
                updateListOfFiles();
            });

            newFolderDialog.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
            newFolderDialog.show();
        });

        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            selection[position] = !selection[position];
            adapter.setSelection(selection);

            int count = 0;
            index = -1;
            for (int i = 0; i < selection.length; i++) {
                if (selection[i]) {
                    index = i;
                    count++;
                }
            }

            bottomBar.setVisibility(count > 0 ? View.VISIBLE : View.GONE);

            renameButton.setVisibility(count == 1 ? View.VISIBLE : View.GONE);

            return false;
        });

        listView.setOnItemClickListener((parent, view, position, id) -> {
            File selectedFile = files[position];
            if (selectedFile.isDirectory()) {
                rootPath = selectedFile.getAbsolutePath();
                path.setText(rootPath.substring(rootPath.lastIndexOf(File.separator)));
                updateListOfFiles();
            }
        });

        deleteButton.setOnClickListener(v -> {
            final AlertDialog.Builder deleteDialog = new AlertDialog.Builder(MainActivity.this);

            deleteDialog.setTitle("Delete");
            deleteDialog.setMessage("Do you really want to delete the selected files ?");

            deleteDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    for (int i = 0; i < files.length; i++) {
                        if (selection[i]) {
                            deleteFileOrFolder(files[i]);
                        }
                    }
                    updateListOfFiles();
                    bottomBar.setVisibility(View.GONE);

                }
            });

            deleteDialog.setNegativeButton("No", (dialog, which) -> dialog.cancel());

            deleteDialog.show();
        });

        refreshButtom.setOnClickListener(v -> updateListOfFiles());

        goBackButtom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int index = rootPath.lastIndexOf(File.separator);
                String st = rootPath.substring(0, index);
                String root = Environment.getExternalStorageDirectory().getAbsolutePath();
                root = root.substring(0, root.lastIndexOf(File.separator));

                if (!st.equals(root)) {
                    rootPath = st;
                    updateListOfFiles();
                    path.setText(rootPath.substring(rootPath.lastIndexOf(File.separator) + 1));
                }

            }
        });

        renameButton.setOnClickListener(v -> {
            final AlertDialog.Builder renameDialog = new AlertDialog.Builder(MainActivity.this);
            renameDialog.setTitle("Rename to:");

            final EditText input = new EditText(MainActivity.this);
            input.setInputType(InputType.TYPE_CLASS_TEXT);


            input.setText(files[index].getName());
            renameDialog.setView(input);
            int finalIndex = index;
            renameDialog.setPositiveButton("OK", (dialog, which) -> {
                String newName = input.getText().toString();

                newName = new File(rootPath).getAbsolutePath() + File.separator + newName;
                File newFile = new File(newName);

                files[finalIndex].renameTo(newFile);

                updateListOfFiles();
            });


            renameDialog.show();

        });

        copyButton.setOnClickListener(v -> {
            copyPath = files[index].getAbsolutePath();
            selection[index] = false;
            adapter.setSelection(selection);

            pasteButton.setVisibility(View.VISIBLE);
            copyButton.setVisibility(View.GONE);

        });

        pasteButton.setOnClickListener(v -> {

            copy(new File(copyPath), new File(rootPath));
            pasteButton.setVisibility(View.GONE);
            copyButton.setVisibility(View.VISIBLE);
            updateListOfFiles();
        });

    }

    private void copy(File sourceLocation, File targetLocation) {
       try{
           if (sourceLocation.isDirectory()) {
               if (!targetLocation.exists()) {
                   targetLocation.mkdir();
               }

               String[] children = sourceLocation.list();
               for (int i = 0; i < sourceLocation.listFiles().length; i++) {

                   copy(new File(sourceLocation, children[i]), new File(targetLocation, children[i]));
               }
           } else {

               InputStream in = new FileInputStream(sourceLocation);

               OutputStream out = new FileOutputStream(targetLocation);

               // Copy the bits from instream to outstream
               byte[] buf = new byte[1024];
               int len;
               while ((len = in.read(buf)) > 0) {
                   out.write(buf, 0, len);
               }
               in.close();
               out.close();
           }
       } catch (IOException e) {
          Log.e("err" ,   e.toString());
       }
    }

    private static final int REQEST_PERMISSIONS = 1234;
    private static final String[] PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private static final int PERMISSION_COUNt = PERMISSIONS.length;
    private boolean isFileManagerInitialiez = false;
    private boolean[] selection;

    private void updateListOfFiles() {
        if (filesList == null) {
            filesList = new ArrayList<>();
        }

        final File dir = new File(rootPath);
        files = dir.listFiles();

        if (files == null) {
            files = new File[0];
        }

        Arrays.sort(files);
        final TextView pathOutPut = findViewById(R.id.pathOutput);

        pathOutPut.setText(rootPath.substring(rootPath.lastIndexOf(File.separator) + 1));

        filesList.clear();

        for (File file : files) {
            String paths = file.getAbsolutePath();
            paths = paths.substring(paths.lastIndexOf(File.separator) + 1);
            // Log.d("fileNames", paths);
            filesList.add(paths);
        }

        adapter = new TextAdapter();
        listView.setAdapter(adapter);
        adapter.setData(filesList);

        selection = new boolean[files.length];

        renameButton.setVisibility(View.GONE);
        bottomBar.setVisibility(View.GONE);
    }

    class TextAdapter extends BaseAdapter {
        private List<String> data = new ArrayList<>();
        private boolean[] selection;

        public void setSelection(boolean[] selection) {
            if (selection != null) {
                this.selection = new boolean[selection.length];

                for (int i = 0; i < selection.length; i++) {
                    this.selection[i] = selection[i];
                }
                notifyDataSetChanged();
            }
        }

        public void setData(List<String> data) {
            if (data != null) {
                this.data.clear();

                if (data.size() > 0) {
                    this.data.addAll(data);
                }
                notifyDataSetChanged();
            }
        }

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public String getItem(int position) {
            return data.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater
                        .from(parent.getContext())
                        .inflate(R.layout.item, parent, false);

                convertView.setTag(new ViewHolder((TextView) convertView.findViewById(R.id.textItem)));
            }

            ViewHolder holder = (ViewHolder) convertView.getTag();
            final String item = getItem(position);
            holder.info.setText(item);

            if (selection != null) {
                if (selection[position]) {
                    holder.info.setBackgroundColor(Color.argb(100, 8, 8, 8));
                } else {
                    holder.info.setBackgroundColor(Color.WHITE);
                }
            }

            return convertView;
        }

        class ViewHolder {
            TextView info;

            ViewHolder(TextView info) {
                this.info = info;
            }
        }
    }


    private boolean arePermissionsDenied() {
        for (int p = 0; p < PERMISSION_COUNt; p++) {
            if (checkSelfPermission(PERMISSIONS[p]) != PackageManager.PERMISSION_GRANTED) {
                return true;
            }
        }
        return false;
    }

    @SuppressLint("ObsoleteSdkInt")
    @Override
    protected void onResume() {
        super.onResume();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && arePermissionsDenied()) {
            requestPermissions(PERMISSIONS, REQEST_PERMISSIONS);
            return;
        }

        if (!isFileManagerInitialiez) {
            rootPath = String.valueOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS));
            updateListOfFiles();
            isFileManagerInitialiez = true;

        }
    }

    private void deleteFileOrFolder(File fileOrFolder) {
        if (fileOrFolder.isDirectory()) {
            File[] children = fileOrFolder.listFiles();

            for (File child : children) {
                if (child.isDirectory()) {
                    deleteFileOrFolder(child);
                    continue;
                }
                child.delete();
            }
        }
        Log.d("deleteStatus", "" + fileOrFolder.getAbsolutePath());
        Log.d("deleteStatus", "" + fileOrFolder.delete());
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, final String[] permissions,
                                           final int[] grantResult) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResult);
        if (requestCode == REQEST_PERMISSIONS && grantResult.length > 0) {
            if (arePermissionsDenied()) {
                ((ActivityManager) Objects.requireNonNull(this.getSystemService(ACTIVITY_SERVICE))).clearApplicationUserData();
                recreate();
            } else {
                onResume();
            }
        }

    }

}