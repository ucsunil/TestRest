package com.android.testrest.requestfragments;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.android.testrest.R;
import com.android.testrest.RestTestApplication;
import com.android.testrest.customadapters.RequestHeaderAdapter;
import com.android.testrest.helpers.HeaderHelper;
import com.android.testrest.helpers.ResponseFragment;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

public class GetFragment extends Fragment implements View.OnClickListener {

    ArrayList<HeaderHelper> headers = null;
    RequestHeaderAdapter requestHeaderAdapter = null;
    ListView headersList = null;
    EditText urlContent = null;
    LinearLayout resultArea = null;
    TextView resultMessage = null;
    Button addHeader = null, reset = null, get = null, viewResponse = null;
    Map<String, List<String>> resultHeaders = null;
    String responseMessage = null;

    public static GetFragment newInstance() {
        GetFragment fragment = new GetFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        headers = new ArrayList<HeaderHelper>();
        requestHeaderAdapter = new RequestHeaderAdapter(getActivity(), R.layout.fragment_get, headers);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_get, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        addHeader = (Button) view.findViewById(R.id.add_header);
        addHeader.setOnClickListener(this);
        headersList = (ListView) view.findViewById(R.id.headers);
        headersList.setAdapter(requestHeaderAdapter);
        urlContent = (EditText) view.findViewById(R.id.url);
        reset = (Button) view.findViewById(R.id.reset);
        reset.setOnClickListener(this);
        get = (Button) view.findViewById(R.id.get_request);
        get.setOnClickListener(this);
        viewResponse = (Button) view.findViewById(R.id.view_response);
        viewResponse.setOnClickListener(this);
        resultArea = (LinearLayout) view.findViewById(R.id.result_area);
        resultMessage = (TextView) view.findViewById(R.id.result_message);
    }


    @Override
    public void onClick(View view) {
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
            if(headers.size() > 0) {
                headers.clear();
                requestHeaderAdapter.notifyDataSetChanged();
            }
            resultArea.setBackgroundColor(Color.WHITE);
            resultMessage.setText("");
            reset.setEnabled(false);
            viewResponse.setEnabled(false);
        }else if(view.getId() == R.id.get_request) {
            int code = 1;
            if(headers.size() > 0) {
                code = saveLastHeader();
            }
            if(code == 1) {
                executeGet();
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
        if(!TextUtils.isEmpty(key) && !TextUtils.isEmpty(value)) {
            header.setHeaderKey(key);
            header.setHeaderValue(value);
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

    private void executeGet() {
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
           new GetHttpTask().execute(url);
        } else if(parts[0].equals("https")) {
           new GetHttpsTask().execute(url);
        } else {
           // TO DO...
        }
    }

    /**
     * Method to display the popup message. I am using popups to display the more sever error
     * messages
     * @param errorMessage The message that the opup should display
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
        HashMap<String, List<String>> hashmap = new HashMap<String, List<String>>();
        for(Map.Entry<String, List<String>> entry : resultHeaders.entrySet()) {
            hashmap.put(entry.getKey(), entry.getValue());
        }
        Bundle bundle = new Bundle();
        bundle.putSerializable("HashMap", hashmap);

        bundle.putString("ResponseMessage", responseMessage);
        ResponseFragment responseFragment = ResponseFragment.newInstance(bundle);
        responseFragment.show(getActivity().getFragmentManager(), "dialog");
    }

    /**
     * The actual Http Get request is performed in the background by using the GetHttpTask
     */
    private class GetHttpTask extends AsyncTask<URL, Void, Integer> {

        @Override
        protected Integer doInBackground(URL... urls) {
            URL url = urls[0];
            int code = -1;
            HttpURLConnection connection = null;
            try {
                connection = (HttpURLConnection) url.openConnection();
                if(headers.size() > 0) {
                    for(HeaderHelper header : headers) {
                        if(!TextUtils.isEmpty(header.getHeaderKey()) && !TextUtils.isEmpty(header.getHeaderValue())) {
                            connection.setRequestProperty(header.getHeaderKey(), header.getHeaderValue());
                        }
                    }
                }
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
     * The actual Https Get request is performed in the background by using the GetHttpTask
     */
    private class GetHttpsTask extends AsyncTask<URL, Void, Integer> {

        @Override
        protected Integer doInBackground(URL... urls) {
            URL url = urls[0];
            int code = -1;
            HttpsURLConnection connection = null;
            try {
                connection = (HttpsURLConnection) url.openConnection();
                if(headers.size() > 0) {
                    for(HeaderHelper header : headers) {
                        if(!TextUtils.isEmpty(header.getHeaderKey()) && !TextUtils.isEmpty(header.getHeaderValue())) {
                            connection.setRequestProperty(header.getHeaderKey(), header.getHeaderValue());
                        }
                    }
                }
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
}