package io.github.oxmose.passlock.adapters;

import android.content.Context;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import io.github.oxmose.passlock.R;
import io.github.oxmose.passlock.model.ListPasswordRowItem;

public class ListPasswordRowAdapter extends ArrayAdapter<ListPasswordRowItem>
                                    implements View.OnClickListener{

    /* List context */
    private Context mContext;

    @Override
    public void onClick(View view) {

    }

    private static class ViewHolder {
        TextView title;
        TextView value;
        ImageView icon;
    }

    public ListPasswordRowAdapter(List<ListPasswordRowItem> data, Context context) {
        super(context, R.layout.password_item_row, data);
        this.mContext = context;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        /* Get the data item for this position */
        ListPasswordRowItem dataModel = getItem(position);

        /* Check if an existing view is being reused, otherwise inflate the view */
        ViewHolder viewHolder;

        /* If we can't reuse, create a new view */
        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.password_item_row, parent, false);
            viewHolder.title = convertView.findViewById(R.id.password_item_row_title);
            viewHolder.value = convertView.findViewById(R.id.password_item_row_value);
            viewHolder.icon = convertView.findViewById(R.id.password_item_row_icon);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        if(dataModel != null) {

            /* Set the title and value */
            viewHolder.title.setText(dataModel.getTitle());
            viewHolder.value.setText(dataModel.getValue());

            int id;

            /* Choose the icon to display */
            switch (dataModel.getType()) {
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
        }

        return convertView;
    }
}
