package com.audiorecorder.voicerecorderhd.editor.activity;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.ActionMode;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.audiorecorder.voicerecorderhd.editor.MainActivity;
import com.audiorecorder.voicerecorderhd.editor.R;
import com.audiorecorder.voicerecorderhd.editor.adapter.LibraryAdapter;
import com.audiorecorder.voicerecorderhd.editor.data.DBQuerys;
import com.audiorecorder.voicerecorderhd.editor.interfaces.LongClickItemLibrary;
import com.audiorecorder.voicerecorderhd.editor.interfaces.OnclickItemLibrary;
import com.audiorecorder.voicerecorderhd.editor.model.Audio;
import com.audiorecorder.voicerecorderhd.editor.utils.CommonUtils;
import com.audiorecorder.voicerecorderhd.editor.utils.Constants;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class LibraryActivity extends AppCompatActivity implements View.OnClickListener, ActionMode.Callback {

    private RecyclerView rvLibrary;
    private LibraryAdapter adapter;
    private LinearLayoutManager layoutManager;
    private ArrayList<Audio> audioList = new ArrayList<>();
    private String formatDuration = "";
    private TextView tvEmpty;
    private LinearLayout viewProgres;
    private static final String TAG = "library";
    private ImageView ivBottomLibrary;
    private ImageView ivBottomRecoder;
    private ImageView ivBottomSettings;
    private ActionMode actionMode;
    private boolean isMultiSelect = false;
    private List<String> selectedIds = new ArrayList<>();
    private AlertDialog dialog;
    private DBQuerys dbQuerys;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_library);
        setTitle("Library");
        mapping();
        new queryFile().execute();
    }

    @SuppressLint("WrongConstant")
    public void setTitle(String title) {
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        TextView textView = new TextView(this);
        textView.setText(title);
        textView.setTextSize(25);
        textView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        textView.setGravity(Gravity.CENTER);
        textView.setTextColor(getResources().getColor(R.color.all_color_black));
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(textView);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_bottom_recoder:
                startActivity(new Intent(LibraryActivity.this, MainActivity.class));

                break;
            case R.id.iv_bottom_settings:
                startActivity(new Intent(LibraryActivity.this, SettingsActivity.class));

                break;
        }
    }

    private void multiSelect(int position) {
        Audio data = adapter.getItem(position);
        if (data != null) {
            if (actionMode != null) {
                if (selectedIds.contains(data.getPath()))
                    selectedIds.remove(data.getPath());
                else
                    selectedIds.add(data.getPath());
                Log.e("size", selectedIds + "" + position);

                if (selectedIds.size() > 0)
                    actionMode.setTitle(String.valueOf(selectedIds.size())); //show selected item count on action mode.
                else {
                    actionMode.setTitle("");
                    actionMode.finish(); //hide action mode.

                }
                adapter.setSelectedIds(selectedIds);

            }

        }
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        MenuInflater inflater = mode.getMenuInflater();
        inflater.inflate(R.menu.menu_select, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.menu_delete_item_library:
                deleteAudio();
                break;
            case R.id.menu_share_item_library:
                shareAudio();

                break;
        }
        return false;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        actionMode = null;
        isMultiSelect = false;
        selectedIds = new ArrayList<>();
        adapter.setSelectedIds(new ArrayList<String>());
    }

    private class queryFile extends AsyncTask<String, String, ArrayList<Audio>> {

        @Override
        protected ArrayList<Audio> doInBackground(String... strings) {
            dbQuerys = new DBQuerys(LibraryActivity.this);
            audioList = dbQuerys.getallNguoiDung();
            return audioList;
        }

        @Override
        protected void onPostExecute(ArrayList<Audio> list) {
            super.onPostExecute(list);
            viewProgres.setVisibility(View.GONE);
            rvLibrary.setVisibility(View.VISIBLE);
            setDataAdapter(list);
        }
    }

    private void setDataAdapter(final ArrayList<Audio> audioList) {
        layoutManager = new LinearLayoutManager(this);
        rvLibrary.setLayoutManager(layoutManager);
        if (audioList.size() == 0) {
            tvEmpty.setVisibility(View.VISIBLE);
            rvLibrary.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            rvLibrary.setVisibility(View.VISIBLE);
        }
        adapter = new LibraryAdapter(LibraryActivity.this, audioList);
        rvLibrary.setAdapter(adapter);

        adapter.setOnclickItem(new OnclickItemLibrary() {
            @Override
            public void onClick(int i) {
                if (isMultiSelect) {
                    multiSelect(i);
                    return;
                }
                startActivity(new Intent(LibraryActivity.this, DetailActivity.class)
                        .putExtra("position", i)
                        .putParcelableArrayListExtra("list", audioList));
            }
        });

        adapter.setLongClickItemLibrary(new LongClickItemLibrary() {
            @Override
            public void longClick(int position) {
                if (!isMultiSelect) {
                    selectedIds = new ArrayList<>();
                    isMultiSelect = true;

                    if (actionMode == null) {
                        actionMode = startActionMode(LibraryActivity.this); //show ActionMode.

                    } else {
                    }
                }

                multiSelect(position);
            }
        });
    }

    private void mapping() {
        ivBottomLibrary = findViewById(R.id.iv_bottom_library);
        ivBottomRecoder = findViewById(R.id.iv_bottom_recoder);
        ivBottomSettings = findViewById(R.id.iv_bottom_settings);

        ivBottomRecoder.setOnClickListener(this);
        ivBottomSettings.setOnClickListener(this);

        ivBottomLibrary.setImageDrawable(getResources().getDrawable(R.drawable.ic_library_pr));
        tvEmpty = findViewById(R.id.tv_library_empty);
        rvLibrary = findViewById(R.id.rv_library);
        viewProgres = findViewById(R.id.prb_library);
        rvLibrary.setHasFixedSize(true);
    }


    private void deleteAudio() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.question_delete)
                .setPositiveButton(R.string.dialog_no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int j) {
                        dialog.dismiss();
                    }
                }).setNegativeButton(R.string.dialog_yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int j) {

                for (int i = 0; i < selectedIds.size(); i++) {
                    new File(selectedIds.get(i)).delete();
                    Log.e("path", selectedIds.get(i));
                }
                actionMode.finish();
                audioList.clear();
                adapter.updateList(dbQuerys.getallNguoiDung());

                showToast(getResources().getString(R.string.success_delete));
            }
        });
        builder.create();
        dialog = builder.show();
    }

    private void showToast(String s) {
        Toast.makeText(LibraryActivity.this, s, Toast.LENGTH_SHORT).show();
    }

    private void shareAudio() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND_MULTIPLE);
        intent.putExtra(Intent.EXTRA_SUBJECT, "Here are some files.");
        intent.setType("audio/*");

        ArrayList<Uri> files = new ArrayList<>();

        for (String path : selectedIds) {
            File file = new File(path);
            files.add(Uri.fromFile((file)));
        }

        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, files);
        startActivity(Intent.createChooser(intent, "share"));
    }


}
