package com.audiorecorder.voicerecorderhd.editor.editRecording;

import android.app.Dialog;
import android.content.Context;
import android.os.Message;
import android.view.View;
import android.widget.Button;

import com.audiorecorder.voicerecorderhd.editor.R;

public class AfterSaveActionDialog extends Dialog {
    private Message mResponse;

    public AfterSaveActionDialog(Context context, Message response) {
        super(context);

        // Inflate our UI from its XML layout description.
        setContentView(R.layout.after_save_action);

        setTitle(R.string.alert_title_success);

        ((Button)findViewById(R.id.button_make_default))
                .setOnClickListener(new View.OnClickListener() {
                    public void onClick(View view) {
                        closeAndSendResult(R.id.button_make_default);
                    }
                });
        ((Button)findViewById(R.id.button_choose_contact))
                .setOnClickListener(new View.OnClickListener() {
                    public void onClick(View view) {
                        closeAndSendResult(R.id.button_choose_contact);
                    }
                });
        ((Button)findViewById(R.id.button_do_nothing))
                .setOnClickListener(new View.OnClickListener() {
                    public void onClick(View view) {
                        closeAndSendResult(R.id.button_do_nothing);
                    }
                });

        mResponse = response;
    }

    private void closeAndSendResult(int clickedButtonId) {
        mResponse.arg1 = clickedButtonId;
        mResponse.sendToTarget();
        dismiss();
    }
}
