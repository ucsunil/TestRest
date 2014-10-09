package com.android.testrest.customadapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.android.testrest.R;
import com.android.testrest.helpers.HeaderHelper;

import java.util.ArrayList;

/**
 * Created by umonssu on 10/8/14.
 */
public class ResponseHeaderAdapter extends ArrayAdapter<HeaderHelper> {

    private ArrayList<HeaderHelper> headers;
    private Context context;

    public ResponseHeaderAdapter(Context context, int textViewResourceId, ArrayList<HeaderHelper> headers) {
        super(context, textViewResourceId, headers);

        this.context = context;
        this.headers = headers;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        if(rowView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.response_headers_layout, parent, false);
        }
        HeaderHelper header = headers.get(position);
        TextView key = (TextView) rowView.findViewById(R.id.header_key);
        if(position == 0) {
            key.setText("Status");
        } else {
            key.setText(header.getHeaderKey());
        }
        TextView value = (TextView) rowView.findViewById(R.id.header_value);
        value.setText(header.getHeaderValue());
        return rowView;
    }
}
