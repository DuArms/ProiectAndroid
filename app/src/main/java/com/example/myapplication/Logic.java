package com.example.myapplication;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Scroller;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.example.myapplication.Adaptors.TextAdapter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Logic {
    private int index;
    private String copyPath;

    public String rootPath = null;

    public File[] files = null;
    public List<String> filesList = null;

    private boolean[] selection;

    private final MainActivity parent;

    public Logic(MainActivity parent) {
        this.parent = parent;
    }

    public void createTextFile() {
        final AlertDialog.Builder textEditDialog = new AlertDialog.Builder(parent);

        View viewBugReport = LayoutInflater.from(textEditDialog.getContext()).inflate(R.layout.dialogedittxt,null);

        textEditDialog.setTitle("Text Edit");

        final EditText name = viewBugReport.findViewById(R.id.fileName);
        final EditText content = viewBugReport.findViewById(R.id.fileContent);

        name.setText("");
        name.setHint("file.txt");

        textEditDialog.setView(viewBugReport);

        textEditDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if (name.getText().length() < 3) {
                    Toast.makeText(parent.getApplicationContext(), "Error , name too short", Toast.LENGTH_SHORT)
                            .show();
                    return;
                }

                File root = new File(rootPath);
                String fileName = name.getText().toString();
                File gpxfile = new File(root, fileName + (fileName.endsWith(".txt") ? "" : ".txt"));

                try {
                    FileWriter writer = new FileWriter(gpxfile);
                    writer.append(content.getText().toString().trim());
                    writer.flush();
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                refresh();
            }
        });

        textEditDialog.setNegativeButton("No", (dialog, which) -> dialog.cancel());

        textEditDialog.show();

    }

    public void editTextFile(File file) {
        final AlertDialog.Builder textEditDialog = new AlertDialog.Builder(parent);

        View viewBugReport = LayoutInflater.from(textEditDialog.getContext()).inflate(R.layout.dialogedittxt,null);

        textEditDialog.setTitle("Text Edit");

        final EditText name = viewBugReport.findViewById(R.id.fileName);
        final EditText content = viewBugReport.findViewById(R.id.fileContent);

        name.setInputType(InputType.TYPE_CLASS_TEXT);
        name.setText(file.getName());

        textEditDialog.setView(viewBugReport);

        try {
            Scanner myReader = new Scanner(file);
            StringBuilder allData = new StringBuilder();

            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                allData.append(data + '\n');
            }
            myReader.close();
            content.setText(allData.toString());

        } catch (FileNotFoundException e) {
            Log.e("MiRo" ,   e.toString()) ;


        }



        textEditDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if (name.getText().length() < 3) {
                    Toast.makeText(parent.getApplicationContext(), "Error , name too short", Toast.LENGTH_SHORT)
                            .show();
                    return;
                }

                File root = new File(rootPath);
                String fileName = name.getText().toString();



                try {
                    FileWriter writer = new FileWriter(file);
                    writer.append(content.getText().toString());
                    writer.flush();
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }


                file.renameTo(new File(root, fileName + (fileName.endsWith(".txt") ? "" : ".txt")));
                refresh();
            }
        });

        textEditDialog.setNegativeButton("No", (dialog, which) -> dialog.cancel());

        textEditDialog.show();

    }


    public boolean onItemNameClick(AdapterView<?> parent, View view, int position, long id) {
        selection[position] = !selection[position];
        this.parent.getAdapter().setSelection(selection);

        int count = 0;
        index = -1;
        for (int i = 0; i < selection.length; i++) {
            if (selection[i]) {
                index = i;
                count++;
            }
        }

        this.parent.getBottomBar().setVisibility(count > 0 ? View.VISIBLE : View.GONE);

        this.parent.getRenameButton().setVisibility(count == 1 ? View.VISIBLE : View.GONE);

        return false;
    }

    public void copy(File sourceLocation, File targetLocation) {

        if (targetLocation.toString().contains(sourceLocation.toString())) {
            Log.d("MiRo", "Eroare incerci ca copiezi directorul parinte in directorul copil!");
            return;
        }

        try {
            if (sourceLocation.isDirectory()) {
                if (!targetLocation.exists()) {
                    targetLocation.mkdir();
                }

                String name = targetLocation.toString();

                if (!targetLocation.getName().equals(sourceLocation.getName())) {
                    name = name + File.separator + sourceLocation.getName();
                }

                File directory = new File(name);

                if (!directory.exists()) {
                    directory.mkdir();
                }


                String[] children = sourceLocation.list();
                for (int i = 0; i < sourceLocation.listFiles().length; i++) {

                    copy(new File(sourceLocation, children[i]),
                            new File(directory, children[i]));

                    Log.d("MiRo", directory.toString());
                }

            } else {

                InputStream in = new FileInputStream(sourceLocation);

                OutputStream out = new FileOutputStream(new File(targetLocation + File.separator + sourceLocation.getName()));

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
            Log.e("MiRo", e.toString());
        }
    }

    public void updateListOfFiles() {
        if (filesList == null) {
            filesList = new ArrayList<>();
        }

        final File dir = new File(rootPath);
        files = dir.listFiles();

        if (files == null) {
            files = new File[0];
        }

        Arrays.sort(files);
        final TextView pathOutPut = parent.findViewById(R.id.pathOutput);

        pathOutPut.setText(rootPath.substring(rootPath.lastIndexOf(File.separator) + 1));

        filesList.clear();

        for (File file : files) {
            String paths = file.getAbsolutePath();
            paths = paths.substring(paths.lastIndexOf(File.separator) + 1);
            // Log.d("fileNames", paths);
            filesList.add(paths);
        }

        parent.setAdapter(new TextAdapter());
        parent.getListView()
                .setAdapter(parent.getAdapter());
        parent.getAdapter()
                .setData(filesList);
        selection = new boolean[files.length];


        parent.getRenameButton().setVisibility(View.GONE);
        parent.getBottomBar().setVisibility(View.GONE);
    }

    public void deleteFileOrFolder(File fileOrFolder) {
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
        Log.d("MiRo", "" + fileOrFolder.getAbsolutePath());
        Log.d("MiRo", "" + fileOrFolder.delete());
    }

    void newFolder() {
        final AlertDialog.Builder newFolderDialog = new AlertDialog.Builder(parent);
        newFolderDialog.setTitle("New Folder");
        final EditText input = new EditText(parent);

        input.setInputType(InputType.TYPE_CLASS_TEXT);

        newFolderDialog.setView(input);

        newFolderDialog.setPositiveButton("OK", (dialog, which) -> {
            final File newFolder = new File(rootPath + File.separator + input.getText().toString());
            if (!newFolder.exists()) {
                newFolder.mkdir();
            }
            parent.logic.updateListOfFiles();
        });

        newFolderDialog.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        newFolderDialog.show();
    }

    void refresh() {
        updateListOfFiles();
    }

    void delete() {
        final AlertDialog.Builder deleteDialog = new AlertDialog.Builder(parent);

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
                parent.getBottomBar().setVisibility(View.GONE);

            }
        });

        deleteDialog.setNegativeButton("No", (dialog, which) -> dialog.cancel());

        deleteDialog.show();
    }

    void back() {
        int index = rootPath.lastIndexOf(File.separator);
        String st = rootPath.substring(0, index);
        String root = Environment.getExternalStorageDirectory().getAbsolutePath();
        root = root.substring(0, root.lastIndexOf(File.separator));

        if (!st.equals(root)) {
            rootPath = st;
            updateListOfFiles();
            parent.getPath().setText(rootPath.substring(rootPath.lastIndexOf(File.separator) + 1));
        }
    }

    void rename() {
        final AlertDialog.Builder renameDialog = new AlertDialog.Builder(parent);
        renameDialog.setTitle("Rename to:");

        final EditText input = new EditText(parent);
        input.setInputType(InputType.TYPE_CLASS_TEXT);


        input.setText(files[index].getName());
        renameDialog.setView(input);
        int finalIndex = index;
        renameDialog.setPositiveButton("OK", (dialog, which) -> {
            String newName = input.getText().toString();

            newName = new File(rootPath).getAbsolutePath() + File.separator + newName;
            File newFile = new File(newName);

            boolean r = files[finalIndex].renameTo(newFile);
            Log.d("MiRo", r ? "da" : "nu");
            updateListOfFiles();
        });
        renameDialog.show();
    }


    void copy() {
        copyPath = files[index].getAbsolutePath();
        selection[index] = false;
        parent.getAdapter().setSelection(selection);

        parent.getCopyButton().setVisibility(View.GONE);
    }

    void paste() {
        if (copyPath == null) {
            Toast.makeText(parent.getApplicationContext(), "Error , no file selected to be copied!", Toast.LENGTH_SHORT)
                    .show();

            return;
        }

        copy(new File(copyPath), new File(rootPath));
        parent.getCopyButton().setVisibility(View.VISIBLE);
        updateListOfFiles();
    }


    public void openFile(File file) {
        Uri uri = FileProvider.getUriForFile(parent, parent.getApplicationContext().getPackageName() + ".provider", file);

        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);


        intent.addCategory(Intent.CATEGORY_OPENABLE);
        // intent.setAction(Intent.ACTION_VIEW);

        final String name = file.getName();

        if (name.contains(".txt")) {
            editTextFile(file);
            return;
        }

        if (name.contains(".doc") || name.contains(".docx")) {
            // Word document
            intent.setDataAndType(uri, "application/msword");
        } else if (name.contains(".pdf")) {
            // PDF file
            intent.setDataAndType(uri, "application/pdf");
        } else if (name.contains(".ppt") || name.contains(".pptx")) {
            // Powerpoint file
            intent.setDataAndType(uri, "application/vnd.ms-powerpoint");
        } else if (name.contains(".xls") || name.contains(".xlsx")) {
            // Excel file
            intent.setDataAndType(uri, "application/vnd.ms-excel");
        } else if (name.contains(".zip") || name.contains(".rar")) {
            // ZIP audio file
            intent.setDataAndType(uri, "application/zip");
        } else if (name.contains(".rtf")) {
            // RTF file
            intent.setDataAndType(uri, "application/rtf");
        } else if (name.contains(".wav") || name.contains(".mp3")) {
            // WAV audio file
            intent.setDataAndType(uri, "audio/x-wav");
        } else if (name.contains(".gif")) {
            // GIF file
            intent.setDataAndType(uri, "image/gif");
        } else if (name.contains(".jpg") || name.contains(".jpeg") || name.contains(".png")) {
            // JPG file
            intent.setDataAndType(uri, "image/jpeg");
        } else if (name.contains(".txt")) {
            // Text file
            intent.setDataAndType(uri, "text/plain");
        } else if (name.contains(".3gp") || name.contains(".mpg") || name.contains(".mpeg") || name.contains(".mpe") || name.contains(".mp4") || name.contains(".avi")) {
            // Video files
            intent.setDataAndType(uri, "video/*");
        } else {
            // Other files
            intent.setDataAndType(uri, "*/*");
        }
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        parent.startActivity(intent);
    }
}

