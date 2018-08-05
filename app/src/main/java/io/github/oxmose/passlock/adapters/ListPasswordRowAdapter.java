package io.github.oxmose.passlock.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import io.github.oxmose.passlock.R;
import io.github.oxmose.passlock.model.ListPasswordRowItem;

public class ListPasswordRowAdapter extends ArrayAdapter<ListPasswordRowItem> implements View.OnClickListener{

    private List<ListPasswordRowItem> dataSet;
    Context mContext;

    private static class ViewHolder {
        TextView title;
        TextView value;
        ImageView icon;
    }

    public ListPasswordRowAdapter(List<ListPasswordRowItem> data, Context context) {
        super(context, R.layout.password_item_row, data);
        this.dataSet = data;
        this.mContext = context;
    }

    @Override
    public void onClick(View v) {

        int position = (Integer) v.getTag();
        Object object= getItem(position);
        ListPasswordRowItem dataModel = (ListPasswordRowItem)object;

    }

    private int lastPosition = -1;

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        ListPasswordRowItem dataModel = getItem(position);

        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder; // view lookup cache stored in tag

        final View result;

        if (convertView == null) {

            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.password_item_row, parent, false);
            viewHolder.title = convertView.findViewById(R.id.password_item_row_title);
            viewHolder.value = convertView.findViewById(R.id.password_item_row_value);
            viewHolder.icon = convertView.findViewById(R.id.password_item_row_icon);

            result = convertView;

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            result = convertView;
        }

        lastPosition = position;

        viewHolder.title.setText(dataModel.getTitle());
        viewHolder.value.setText(dataModel.getValue());

        int id;
        switch(dataModel.getType()) {
            case PASSWORD:
                id = mContext.getResources()
                        .getIdentifier("io.github.oxmose.passlock:drawable/ic_lock_outline",
                                null, null);
                break;
            case PIN:
                id = mContext.getResources()
                        .getIdentifier("io.github.oxmose.passlock:drawable/ic_credit_card",
                                null, null);
                break;
            case DIGICODE:
                id = mContext.getResources()
                        .getIdentifier("io.github.oxmose.passlock:drawable/ic_dialpad",
                                null, null);
                break;
            default:
                id = mContext.getResources()
                        .getIdentifier("io.github.oxmose.passlock:drawable/ic_lock_outline",
                                null, null);
                break;
        }
        viewHolder.icon.setImageResource(id);

        return convertView;
    }
}
