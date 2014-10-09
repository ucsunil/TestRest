package com.android.testrest.customadapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;

import com.android.testrest.R;
import com.android.testrest.helpers.HeaderHelper;

import java.util.ArrayList;

/**
 * Created by umonssu on 10/7/14.
 */
public class RequestHeaderAdapter extends ArrayAdapter<HeaderHelper> {

    private ArrayList<HeaderHelper> headers;
    private Context context;

    public RequestHeaderAdapter(Context context, int textViewResourceId, ArrayList<HeaderHelper> headers) {
        super(context, textViewResourceId, headers);

        this.context = context;
        this.headers = headers;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        if(rowView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.request_headers_layout, parent, false);
        }
        HeaderHelper header = headers.get(position);
        EditText key = (EditText) rowView.findViewById(R.id.header_key);
        key.setText(header.getHeaderKey());
        EditText value = (EditText) rowView.findViewById(R.id.header_value);
        value.setText(header.getHeaderValue());
        return rowView;
    }
}
