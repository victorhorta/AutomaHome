package com.automahome.canbus;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.automahome.canbus.mvc.CanNode;



public class CanNodeListAdapter extends ArrayAdapter<CanNode> {
    
    public CanNodeListAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
    }

    public CanNodeListAdapter(Context context, int resource, List<CanNode> items) {
        super(context, resource, items);
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;
        
        if (v == null) {

            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.adapter_list_item, null);

        }

        CanNode p = getItem(position);

        if (p != null) {
            ImageView icon = (ImageView) v.findViewById(R.id.list_icon);
            TextView nodeTitle = (TextView) v.findViewById(R.id.list_title);
            TextView nodeStatus = (TextView) v.findViewById(R.id.list_status);
            //TextView tt3 = (TextView) v.findViewById(R.id.description);
            
            if (icon != null) {
                icon.setImageResource(p.getMyType().getIconResource());
            }

            if (nodeTitle != null) {
                nodeTitle.setText(p.getMyType().getTypeName());
            }
            if (nodeStatus != null) {

                nodeStatus.setText(p.getStatusText());
            }
//            if (tt3 != null) {
//
//                tt3.setText(p.getDescription());
//            }
        }

        return v;
    }

}
