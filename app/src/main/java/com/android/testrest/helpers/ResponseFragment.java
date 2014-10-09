package com.android.testrest.helpers;


import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.android.testrest.R;
import com.android.testrest.customadapters.ResponseHeaderAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ResponseFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class ResponseFragment extends DialogFragment {

    private ListView responseHeadersList = null;
    private TextView responseContent = null;
    private ArrayList<HeaderHelper> headerHelpers = null;
    private HashMap<String, List<String>> responseHeaders = null;
    private ResponseHeaderAdapter responseHeaderAdapter = null;
    private String responseMessage;

    public static ResponseFragment newInstance(Bundle bundle) {
        ResponseFragment fragment = new ResponseFragment();
        fragment.setArguments(bundle);
        return fragment;
    }
    public ResponseFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        headerHelpers = new ArrayList<HeaderHelper>();
        Bundle bundle = getArguments();
        if (bundle != null) {
            responseMessage = bundle.getString("ResponseMessage");
            responseHeaders = (HashMap<String, List<String>>) bundle.getSerializable("HashMap");
            if(responseHeaders.size() > 0) {
                for(Map.Entry<String, List<String>> entry : responseHeaders.entrySet()) {
                    HeaderHelper headerHelper = new HeaderHelper();
                    headerHelper.setHeaderKey(entry.getKey());
                    List<String> list = entry.getValue();
                    StringBuilder builder = new StringBuilder();
                    for(int i = 0; i < list.size(); i++) {
                        builder.append(list.get(i));
                        if(i == list.size()-1) {
                            break;
                        }
                        builder.append(";");
                    }
                    headerHelper.setHeaderValue(builder.toString());
                    headerHelpers.add(headerHelper);
                }
            }
        }
        responseHeaderAdapter = new ResponseHeaderAdapter(getActivity(), R.layout.fragment_response, headerHelpers);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_response, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        responseHeadersList = (ListView) view.findViewById(R.id.response_headers);
        responseHeadersList.setAdapter(responseHeaderAdapter);

        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View footerView = inflater.inflate(R.layout.response_content_layout, null);
        responseContent = (TextView) footerView.findViewById(R.id.response_content);
        responseContent.setText(responseMessage);
        responseHeadersList.addFooterView(footerView);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setTitle("Server Response");
        return dialog;
    }


}
