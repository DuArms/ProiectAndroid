package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.example.myapplication.Adaptors.TextAdapter;

import java.io.File;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private static final int REQEST_PERMISSIONS = 1234;
    private static final String[] PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private static final int PERMISSION_COUNt = PERMISSIONS.length;
    private boolean isFileManagerInitialiez = false;

    private Button deleteButton = null;
    private Button renameButton = null;
    private Button copyButton = null;
    private Button refreshButtom = null;
    private Button goBackButtom = null;


    private ListView listView = null;
    private LinearLayout bottomBar = null;

    private TextAdapter adapter = null;
    private TextView path = null;

    public Logic logic = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout1);

        logic = new Logic(this);

        path = findViewById(R.id.pathOutput);

        deleteButton = findViewById(R.id.delete);
        renameButton = findViewById(R.id.rename);
        copyButton = findViewById(R.id.copy);
        goBackButtom = findViewById(R.id.goBack);


        listView = findViewById(R.id.listView);
        bottomBar = findViewById(R.id.bottomBar);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            File selectedFile = logic.files[position];
            if (selectedFile.isDirectory()) {
                logic.rootPath = selectedFile.getAbsolutePath();
                path.setText(logic.rootPath.substring(logic.rootPath.lastIndexOf(File.separator)));
                logic.updateListOfFiles();
            } else {

                logic.openFile(selectedFile);
            }
        });

        listView.setOnItemLongClickListener((parent, view, position, id) -> logic.onItemNameClick(parent, view, position, id));

        deleteButton.setOnClickListener(v -> logic.delete());

        goBackButtom.setOnClickListener(v -> logic.back());

        renameButton.setOnClickListener(v -> logic.rename());

        copyButton.setOnClickListener(v -> logic.copy());


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
            logic.rootPath = String.valueOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS));
            logic.updateListOfFiles();
            isFileManagerInitialiez = true;

        }
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        return true;
    }


    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.refresh:
                logic.refresh();
                return true;
            case R.id.newFolder:
                logic.newFolder();
                return true;
            case R.id.paste:
                logic.paste();
                return true;
            case R.id.newFile:
                logic.createTextFile();

            default:
                return super.onOptionsItemSelected(item);
        }
    }


    // get

    public Button getDeleteButton() {
        return deleteButton;
    }

    public Button getRenameButton() {
        return renameButton;
    }

    public Button getCopyButton() {
        return copyButton;
    }


    public Button getRefreshButtom() {
        return refreshButtom;
    }

    public Button getGoBackButtom() {
        return goBackButtom;
    }


    public ListView getListView() {
        return listView;
    }

    public LinearLayout getBottomBar() {
        return bottomBar;
    }

    public TextAdapter getAdapter() {
        return adapter;
    }

    public TextView getPath() {
        return path;
    }


    public static int getReqestPermissions() {
        return REQEST_PERMISSIONS;
    }

    public static String[] getPERMISSIONS() {
        return PERMISSIONS;
    }

    public static int getPERMISSION_COUNt() {
        return PERMISSION_COUNt;
    }

    public boolean isFileManagerInitialiez() {
        return isFileManagerInitialiez;
    }


    //set
    public void setDeleteButton(Button deleteButton) {
        this.deleteButton = deleteButton;
    }

    public void setRenameButton(Button renameButton) {
        this.renameButton = renameButton;
    }

    public void setCopyButton(Button copyButton) {
        this.copyButton = copyButton;
    }


    public void setRefreshButtom(Button refreshButtom) {
        this.refreshButtom = refreshButtom;
    }

    public void setGoBackButtom(Button goBackButtom) {
        this.goBackButtom = goBackButtom;
    }


    public void setListView(ListView listView) {
        this.listView = listView;
    }

    public void setBottomBar(LinearLayout bottomBar) {
        this.bottomBar = bottomBar;
    }

    public void setAdapter(TextAdapter adapter) {
        this.adapter = adapter;
    }

    public void setPath(TextView path) {
        this.path = path;
    }


    public void setFileManagerInitialiez(boolean fileManagerInitialiez) {
        isFileManagerInitialiez = fileManagerInitialiez;
    }


}