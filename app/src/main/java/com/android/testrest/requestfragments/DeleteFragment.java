package com.android.testrest.requestfragments;


import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.SparseBooleanArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.testrest.R;
import com.android.testrest.RestTestApplication;
import com.android.testrest.customadapters.RequestHeaderAdapter;
import com.android.testrest.helpers.ActionModeListener;
import com.android.testrest.helpers.HeaderDialogFragment;
import com.android.testrest.helpers.HeaderHelper;
import com.android.testrest.helpers.ResponseDialogFragment;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

public class DeleteFragment extends Fragment implements View.OnClickListener {

    private final int fragmentFlag = 4;
    ArrayList<HeaderHelper> headers = null;
    RequestHeaderAdapter requestHeaderAdapter = null;
    Button addHeader = null;
    ListView headersList = null;
    Spinner contentType = null;
    EditText urlContent = null, postBody = null;
    LinearLayout resultArea = null;
    TextView resultMessage = null;
    Button reset = null, delete = null, viewResponse = null;
    Map<String, List<String>> resultHeaders = null;
    String responseMessage = null;
    private final int HEADER_CODE = 1;
    private final String HEADER_KEY = "HEADER_KEY";
    private final String HEADER_VALUE = "HEADER_VALUE";

    public static DeleteFragment newInstance() {
        DeleteFragment fragment = new DeleteFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        headers = new ArrayList<HeaderHelper>();
        requestHeaderAdapter = new RequestHeaderAdapter(getActivity(), R.layout.fragment_header, headers);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_post_put_delete, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        addHeader = (Button) view.findViewById(R.id.add_header);
        addHeader.setOnClickListener(this);
        headersList = (ListView) view.findViewById(R.id.headers);
        headersList.setAdapter(requestHeaderAdapter);
        headersList.setMultiChoiceModeListener(new ActionModeListener(getActivity(), this, headersList, fragmentFlag));
        headersList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        headersList.setOnItemLongClickListener(new ListView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
                headersList.setItemChecked(position, true);
                return true;
            }
        });
        urlContent = (EditText) view.findViewById(R.id.url);
        contentType = (Spinner) view.findViewById(R.id.content_type);
        contentType.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                TextView textView = (TextView) view;
                if(textView.getText().toString().equals("Custom")) {
                    Toast.makeText(getActivity(),RestTestApplication.SUPPLY_HEADER, Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // Do nothing
            }
        });
        postBody = (EditText) view.findViewById(R.id.post_body);
        reset = (Button) view.findViewById(R.id.reset);
        reset.setOnClickListener(this);
        delete = (Button) view.findViewById(R.id.request);
        delete.setText(R.string.delete);
        delete.setOnClickListener(this);
        viewResponse = (Button) view.findViewById(R.id.view_response);
        viewResponse.setOnClickListener(this);
        resultArea = (LinearLayout) view.findViewById(R.id.result_area);
        resultMessage = (TextView) view.findViewById(R.id.result_message);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == requestCode) {
            HeaderHelper headerHelper = new HeaderHelper();
            headerHelper.setHeaderKey(data.getStringExtra(HEADER_KEY));
            headerHelper.setHeaderValue(data.getStringExtra(HEADER_VALUE));
            headers.add(headerHelper);
            requestHeaderAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.add_header) {
            showHeaderDialogFragment();
        } else if(view.getId() == R.id.reset) {
            urlContent.setText("");
            contentType.setSelection(0);
            if(headers.size() > 0) {
                headers.clear();
                requestHeaderAdapter.notifyDataSetChanged();
            }
            postBody.setText("");
            resultArea.setBackgroundColor(Color.WHITE);
            resultMessage.setText("");
            reset.setEnabled(false);
            viewResponse.setEnabled(false);
        }else if(view.getId() == R.id.request) {
            executePost();
        } else if(view.getId() == R.id.view_response) {
            showResponseDialogFragment();
        }
    }

    private void executePost() {
        URL url = null;
        if(TextUtils.isEmpty(urlContent.getText().toString())) {
            showErrorPopup(RestTestApplication.EMPTY_URL);
            return;
        }
        try {
            url = new URL(urlContent.getText().toString());
        } catch (MalformedURLException e) {
            showErrorPopup(RestTestApplication.MALFORMED_URL);
            e.printStackTrace();
            return;
        }
        String[] parts = urlContent.getText().toString().split("://");
        if(parts[0].equals("http")) {
            new DeleteHttpTask().execute(url);
        } else if(parts[0].equals("https")) {
            new DeleteHttpsTask().execute(url);
        } else {
            // TO DO...
        }
    }

    /**
     * Method to display the popup message. I am using popups to display the more severe errors
     * messages
     * @param errorMessage The message that the popup should display
     */
    private void showErrorPopup(String errorMessage) {
        LayoutInflater layoutInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View popupView = layoutInflater.inflate(R.layout.failure_popup, null);
        final PopupWindow popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        TextView textView = (TextView) popupView.findViewById(R.id.error_text);
        textView.setText(errorMessage);
        Button ok = (Button) popupView.findViewById(R.id.ok);
        ok.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupWindow.dismiss();
            }
        });
        popupWindow.showAtLocation(getActivity().findViewById(android.R.id.content), Gravity.CENTER, 0, 0);
    }

    private void showHeaderDialogFragment() {
        HeaderDialogFragment headerDialogFragment = HeaderDialogFragment.getInstance();
        headerDialogFragment.setTargetFragment(this, HEADER_CODE);
        headerDialogFragment.show(getActivity().getFragmentManager(), "dialogHeader");
    }

    /**
     * Method to display the ResponseFragment. Call this method to see the returned response headers
     * and the response body in a dialog fragment
     */
    private void showResponseDialogFragment() {
        HashMap<String, List<String>> hashmap = null;
        if(resultHeaders != null) {
            hashmap = new HashMap<String, List<String>>();
            for(Map.Entry<String, List<String>> entry : resultHeaders.entrySet()) {
                hashmap.put(entry.getKey(), entry.getValue());
            }
        }
        Bundle bundle = new Bundle();
        bundle.putSerializable("HashMap", hashmap);

        bundle.putString("ResponseMessage", responseMessage);
        ResponseDialogFragment responseDialogFragment = ResponseDialogFragment.newInstance(bundle);
        responseDialogFragment.show(getActivity().getFragmentManager(), "dialog");
    }

    /**
     * The actual Http Delete request is performed in the background by using the PostHttpTask
     */
    private class DeleteHttpTask extends AsyncTask<URL, Void, Integer> {

        @Override
        protected Integer doInBackground(URL... urls) {
            URL url = urls[0];
            int code = -1;
            String payload = postBody.getText().toString();
            String payloadType = contentType.getSelectedItem().toString();
            HttpURLConnection connection = null;
            try {
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("DELETE");
                connection.setDoOutput(true);
                connection.setDoInput(true);
                boolean contentTypeInHeaderFlag = false;
                if(headers.size() > 0) {
                    for(HeaderHelper header : headers) {
                        if(!TextUtils.isEmpty(header.getHeaderKey()) && !TextUtils.isEmpty(header.getHeaderValue())) {
                            connection.setRequestProperty(header.getHeaderKey(), header.getHeaderValue());
                            if(header.getHeaderKey().equals("Content-Type")) {
                                contentTypeInHeaderFlag = true;
                            }
                        }
                    }
                }
                if(!contentTypeInHeaderFlag && !payloadType.equals("Custom")) {
                    connection.addRequestProperty("Content-Type", payloadType);
                }
                OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
                writer.write(payload);
                writer.flush();
                writer.close();
                code = connection.getResponseCode();
                InputStream stream;
                if(code < 400) {
                    stream = connection.getInputStream();
                } else {
                    stream = connection.getErrorStream();
                }
                BufferedReader reader = null;
                StringBuilder builder = new StringBuilder();
                String line;
                try {
                    reader = new BufferedReader(new InputStreamReader(stream));
                    while((line = reader.readLine()) != null) {
                        builder.append(line);
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                } finally {
                    if(reader != null) {
                        try {
                            reader.close();
                            stream.close();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
                responseMessage = builder.toString();
                resultHeaders = connection.getHeaderFields();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return code;
        }

        @Override
        protected void onPostExecute(Integer code) {
            if(code == -1) {
                // Massive application level failure!!
                showErrorPopup(RestTestApplication.APP_FAILURE);
                return;
            }
            if(code >= 200 && code <= 299) {
                resultArea.setBackgroundColor(Color.GREEN);
                resultMessage.setText(RestTestApplication.QUERY_SUCCESS + code);
                viewResponse.setEnabled(true);
                reset.setEnabled(true);
            } else if(code >= 300 && code <= 399) {
                resultArea.setBackgroundColor(Color.YELLOW);
                resultMessage.setText(RestTestApplication.QUERY_REDIRECT + code);
                viewResponse.setEnabled(true);
                reset.setEnabled(true);
            } else if(code >= 400 && code <= 499) {
                resultArea.setBackgroundColor(Color.RED);
                resultMessage.setText(RestTestApplication.QUERY_FAILURE + code);
                viewResponse.setEnabled(true);
                reset.setEnabled(true);
            } else if(code >= 500 && code <= 599) {
                resultArea.setBackgroundColor(Color.BLUE);
                resultMessage.setTextColor(Color.RED);
                resultMessage.setText(RestTestApplication.SERVER_ERROR + code);
                viewResponse.setEnabled(true);
                reset.setEnabled(true);
            }
        }
    }

    /**
     * The actual Https Delete request is performed in the background by using the PostHttpTask
     */
    private class DeleteHttpsTask extends AsyncTask<URL, Void, Integer> {

        @Override
        protected Integer doInBackground(URL... urls) {
            URL url = urls[0];
            int code = -1;
            String payload = postBody.getText().toString();
            String payloadType = contentType.getSelectedItem().toString();
            HttpsURLConnection connection = null;
            try {
                connection = (HttpsURLConnection) url.openConnection();
                connection.setRequestMethod("DELETE");
                connection.setDoOutput(true);
                connection.setDoInput(true);
                boolean contentTypeInHeaderFlag = false;
                if(headers.size() > 0) {
                    for(HeaderHelper header : headers) {
                        if(!TextUtils.isEmpty(header.getHeaderKey()) && !TextUtils.isEmpty(header.getHeaderValue())) {
                            connection.setRequestProperty(header.getHeaderKey(), header.getHeaderValue());
                            if(header.getHeaderKey().equals("Content-Type")) {
                                contentTypeInHeaderFlag = true;
                            }
                        }
                    }
                }
                if(!contentTypeInHeaderFlag && !payloadType.equals("Custom")) {
                    connection.addRequestProperty("Content-Type", payloadType);
                }
                OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
                writer.write(payload);
                writer.flush();
                writer.close();
                code = connection.getResponseCode();
                InputStream stream;
                if(code < 400) {
                    stream = connection.getInputStream();
                } else {
                    stream = connection.getErrorStream();
                }
                BufferedReader reader = null;
                StringBuilder builder = new StringBuilder();
                String line;
                try {
                    reader = new BufferedReader(new InputStreamReader(stream));
                    while((line = reader.readLine()) != null) {
                        builder.append(line);
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                } finally {
                    if(reader != null) {
                        try {
                            reader.close();
                            stream.close();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
                responseMessage = builder.toString();
                resultHeaders = connection.getHeaderFields();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return code;
        }

        @Override
        protected void onPostExecute(Integer code) {
            if(code == -1) {
                // Massive application level failure!!
                showErrorPopup(RestTestApplication.APP_FAILURE);
                return;
            }
            if(code >= 200 && code <= 299) {
                resultArea.setBackgroundColor(Color.GREEN);
                resultMessage.setText(RestTestApplication.QUERY_SUCCESS + code);
                viewResponse.setEnabled(true);
                reset.setEnabled(true);
            } else if(code >= 300 && code <= 399) {
                resultArea.setBackgroundColor(Color.YELLOW);
                resultMessage.setText(RestTestApplication.QUERY_REDIRECT + code);
                viewResponse.setEnabled(true);
                reset.setEnabled(true);
            } else if(code >= 400 && code <= 499) {
                resultArea.setBackgroundColor(Color.RED);
                resultMessage.setText(RestTestApplication.QUERY_FAILURE + code);
                viewResponse.setEnabled(true);
                reset.setEnabled(true);
            } else if(code >= 500 && code <= 599) {
                resultArea.setBackgroundColor(Color.BLUE);
                resultMessage.setTextColor(Color.RED);
                resultMessage.setText(RestTestApplication.SERVER_ERROR + code);
                viewResponse.setEnabled(true);
                reset.setEnabled(true);
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        urlContent.setText("");
        postBody.setText("");
        contentType.setSelection(0);
    }

    public boolean performActions(MenuItem item) {
        SparseBooleanArray checked = headersList.getCheckedItemPositions();

        switch (item.getItemId()) {
            case R.id.delete:
                int count = 0;
                for (int i = 0; i < checked.size(); i++) {
                    int position = checked.keyAt(i);
                    headers.remove(position - count);
                    requestHeaderAdapter.notifyDataSetChanged();
                    count++;
                }
                return true;
        }
        return false;
    }
}
