package com.android.testrest.requestfragments;

import android.app.Fragment;
import android.content.Context;
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
import android.view.WindowManager;
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
import com.android.testrest.helpers.HeaderHelper;
import com.android.testrest.helpers.ResponseFragment;

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

/**
 * Created by umonssu on 10/8/14.
 */
public class PostFragment extends Fragment implements View.OnClickListener {

    private final int fragmentFlag = 2;
    ArrayList<HeaderHelper> headers = null;
    RequestHeaderAdapter requestHeaderAdapter = null;
    Button addHeader = null;
    ListView headersList = null;
    Spinner contentType = null;
    EditText urlContent = null, postBody = null;
    LinearLayout resultArea = null;
    TextView resultMessage = null;
    Button reset = null, post = null, viewResponse = null;
    Map<String, List<String>> resultHeaders = null;
    String responseMessage = null;
    boolean isLastHeaderSaved = false;

    public static PostFragment newInstance() {
        PostFragment fragment = new PostFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        headers = new ArrayList<HeaderHelper>();
        requestHeaderAdapter = new RequestHeaderAdapter(getActivity(), R.layout.fragment_get, headers);
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
                saveLastHeader();
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
        post = (Button) view.findViewById(R.id.request);
        post.setText(R.string.post);
        post.setOnClickListener(this);
        viewResponse = (Button) view.findViewById(R.id.view_response);
        viewResponse.setOnClickListener(this);
        resultArea = (LinearLayout) view.findViewById(R.id.result_area);
        resultMessage = (TextView) view.findViewById(R.id.result_message);
    }

    @Override
    public void onClick(View view) {
        isLastHeaderSaved = false;
        if(view.getId() == R.id.add_header) {
            if(headersList.getCount() == 0) {
                HeaderHelper header = new HeaderHelper();
                headers.add(header);
                requestHeaderAdapter.notifyDataSetChanged();
                return;
            }
            int position = headers.size() - 1;
            EditText headerKey = (EditText) headersList.getChildAt(position).findViewById(R.id.header_key);
            String key = headerKey.getText().toString();
            EditText headerValue = (EditText) headersList.getChildAt(position).findViewById(R.id.header_value);
            String value = headerValue.getText().toString();

            HeaderHelper header = headers.get(position);
            if(!TextUtils.isEmpty(key) && !TextUtils.isEmpty(value)) {
                header.setHeaderKey(key);
                header.setHeaderValue(value);
                headers.add(new HeaderHelper());
                requestHeaderAdapter.notifyDataSetChanged();
            } else if(!TextUtils.isEmpty(key) && TextUtils.isEmpty(value)) {
                Toast.makeText(getActivity(), RestTestApplication.ADD_HEADER_MISSING_VALUE, Toast.LENGTH_LONG).show();
            } else if(TextUtils.isEmpty(key) && !TextUtils.isEmpty(value)) {
                Toast.makeText(getActivity(), RestTestApplication.ADD_HEADER_MISSING_KEY, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getActivity(), RestTestApplication.ADD_HEADER_MISSING, Toast.LENGTH_LONG).show();
            }
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
            test();
            int code = 1;
            if(headers.size() > 0 && !isLastHeaderSaved) {
                code = saveLastHeader();
            }
            if(code == 1) {
                executePost();
            }
        } else if(view.getId() == R.id.view_response) {
            showResponseDialogFragment();
        }
    }

    /**
     * This method saves the last header that was entered. Up until now only the (n-1)th header is
     * getting saved every time we add a new header.
     */
    private int saveLastHeader() {
        int position = headers.size() - 1;
        EditText headerKey = (EditText) headersList.getChildAt(position).findViewById(R.id.header_key);
        String key = headerKey.getText().toString();
        EditText headerValue = (EditText) headersList.getChildAt(position).findViewById(R.id.header_value);
        String value = headerValue.getText().toString();

        HeaderHelper header = headers.get(position);
        if(!TextUtils.isEmpty(key) && !TextUtils.isEmpty(value) && !isLastHeaderSaved) {
            header.setHeaderKey(key);
            header.setHeaderValue(value);
            isLastHeaderSaved = true;
            return 1;
        } else if(!TextUtils.isEmpty(key) && TextUtils.isEmpty(value)) {
            Toast.makeText(getActivity(), RestTestApplication.HEADER_MISSING_VALUE, Toast.LENGTH_LONG).show();
        } else if(TextUtils.isEmpty(key) && !TextUtils.isEmpty(value)) {
            Toast.makeText(getActivity(), RestTestApplication.HEADER_MISSING_KEY, Toast.LENGTH_LONG).show();
        } else {
            // Do nothing...
            // Ignore this header and proceed with the request
            return 1;
        }
        return -1;
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
            new PostHttpTask().execute(url);
        } else if(parts[0].equals("https")) {
            new PostHttpsTask().execute(url);
        } else {
            // TO DO...
        }
    }

    /**
     * Method to display the popup message. I am using popups to display the more sever error
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
        ResponseFragment responseFragment = ResponseFragment.newInstance(bundle);
        responseFragment.show(getActivity().getFragmentManager(), "dialog");
    }

    /**
     * The actual Http Post request is performed in the background by using the PostHttpTask
     */
    private class PostHttpTask extends AsyncTask<URL, Void, Integer> {

        @Override
        protected Integer doInBackground(URL... urls) {
            URL url = urls[0];
            int code = -1;
            String payload = postBody.getText().toString();
            String payloadType = contentType.getSelectedItem().toString();
            HttpURLConnection connection = null;
            try {
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
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
                InputStream stream = connection.getInputStream();
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
            viewResponse.setEnabled(true);
            reset.setEnabled(true);
            if(code == -1) {
                // Massive application level failure!!
                showErrorPopup(RestTestApplication.APP_FAILURE);
                return;
            }
            if(code >= 200 && code <= 299) {
                resultArea.setBackgroundColor(Color.GREEN);
                resultMessage.setText(RestTestApplication.QUERY_SUCCESS + code);
            } else if(code >= 300 && code <= 399) {
                resultArea.setBackgroundColor(Color.YELLOW);
                resultMessage.setText(RestTestApplication.QUERY_REDIRECT + code);
            } else if(code >= 400 && code <= 499) {
                resultArea.setBackgroundColor(Color.RED);
                resultMessage.setText(RestTestApplication.QUERY_FAILURE + code);
            } else if(code >= 500 && code <= 599) {
                resultArea.setBackgroundColor(Color.BLUE);
                resultMessage.setTextColor(Color.RED);
                resultMessage.setText(RestTestApplication.SERVER_ERROR + code);
            }
        }
    }

    /**
     * The actual Https Post request is performed in the background by using the PostHttpTask
     */
    private class PostHttpsTask extends AsyncTask<URL, Void, Integer> {

        @Override
        protected Integer doInBackground(URL... urls) {
            URL url = urls[0];
            int code = -1;
            String payload = postBody.getText().toString();
            String payloadType = contentType.getSelectedItem().toString();
            HttpsURLConnection connection = null;
            try {
                connection = (HttpsURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
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
                InputStream stream = connection.getInputStream();
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
            viewResponse.setEnabled(true);
            reset.setEnabled(true);
            if(code == -1) {
                // Massive application level failure!!
                showErrorPopup(RestTestApplication.APP_FAILURE);
                return;
            }
            if(code >= 200 && code <= 299) {
                resultArea.setBackgroundColor(Color.GREEN);
                resultMessage.setText(RestTestApplication.QUERY_SUCCESS + code);
            } else if(code >= 300 && code <= 399) {
                resultArea.setBackgroundColor(Color.YELLOW);
                resultMessage.setText(RestTestApplication.QUERY_REDIRECT + code);
            } else if(code >= 400 && code <= 499) {
                resultArea.setBackgroundColor(Color.RED);
                resultMessage.setText(RestTestApplication.QUERY_FAILURE + code);
            } else if(code >= 500 && code <= 599) {
                resultArea.setBackgroundColor(Color.BLUE);
                resultMessage.setTextColor(Color.RED);
                resultMessage.setText(RestTestApplication.SERVER_ERROR + code);
            }
        }
    }

    private void test() {
        urlContent.setText("http://paperapi-qa.stg-openclass.com/nextext-api/api/account/login?withIdpResponse=true");
        postBody.setText("{" +
                "\"userName\":\"p.siddhartha1\"," +
                "\"password\":\"india1234\"," +
                "\"appKey\":\"NEXT_TEXT_01\"" +
                "}");
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
