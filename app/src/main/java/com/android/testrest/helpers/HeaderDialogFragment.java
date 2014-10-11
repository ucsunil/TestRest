package com.android.testrest.helpers;


import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.android.testrest.R;

public class HeaderDialogFragment extends DialogFragment implements View.OnClickListener {

    private EditText headerKey, headerValue;
    private Button save, cancel;
    private final int HEADER_CODE = 1;

    public static HeaderDialogFragment getInstance() {
        HeaderDialogFragment fragment = new HeaderDialogFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_header, null);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        headerKey = (EditText) view.findViewById(R.id.header_key);
        headerValue = (EditText) view.findViewById(R.id.header_value);
        save = (Button) view.findViewById(R.id.save);
        save.setOnClickListener(this);
        cancel = (Button) view.findViewById(R.id.cancel);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setTitle("Add Header");
        return dialog;
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.save) {
            Intent intent = new Intent();
            intent.putExtra("HEADER_KEY", headerKey.getText().toString());
            intent.putExtra("HEADER_VALUE", headerValue.getText().toString());
            sendResult(intent, HEADER_CODE);
            this.dismiss();
        } else if(view.getId() == R.id.cancel) {
            this.dismiss();
        }
    }

    private void sendResult(Intent intent, int code) {
        getTargetFragment().onActivityResult(getTargetRequestCode(), code, intent);
    }
}
