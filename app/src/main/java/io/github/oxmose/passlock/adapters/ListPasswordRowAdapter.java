package io.github.oxmose.passlock.adapters;

import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;
import java.util.Objects;

import io.github.oxmose.passlock.R;
import io.github.oxmose.passlock.database.DatabaseSingleton;
import io.github.oxmose.passlock.database.Password;
import io.github.oxmose.passlock.fragments.SearchFragment;
import io.github.oxmose.passlock.model.ListPasswordRowItem;

public class ListPasswordRowAdapter extends ArrayAdapter<ListPasswordRowItem>
                                    implements View.OnClickListener{

    /* List context */
    private SearchFragment mContext;


    private static class ViewHolder {
        TextView title;
        TextView value;
        ImageView icon;
        ImageView faveIcon;
    }

    public ListPasswordRowAdapter(List<ListPasswordRowItem> data, SearchFragment context) {
        super(Objects.requireNonNull(context.getContext()), R.layout.password_item_row, data);
        this.mContext = context;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        /* Get the data item for this position */
        final ListPasswordRowItem dataModel = getItem(position);

        /* Check if an existing view is being reused, otherwise inflate the view */
        final ViewHolder viewHolder;

        /* If we can't reuse, create a new view */
        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.password_item_row, parent, false);
            viewHolder.title = convertView.findViewById(R.id.password_item_row_title);
            viewHolder.value = convertView.findViewById(R.id.password_item_row_value);
            viewHolder.icon = convertView.findViewById(R.id.password_item_row_icon);
            viewHolder.faveIcon = convertView.findViewById(R.id.password_item_row_faveicon);

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

            if(dataModel.isFavorite()) {
                viewHolder.faveIcon.setImageResource(mContext.getResources()
                        .getIdentifier("io.github.oxmose.passlock:drawable/ic_favorite",
                                null, null));
            }
            else {
                viewHolder.faveIcon.setImageResource(mContext.getResources()
                        .getIdentifier("io.github.oxmose.passlock:drawable/ic_favorite_border",
                                null, null));
            }



            viewHolder.faveIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(dataModel.isFavorite()) {
                        dataModel.setFavorite(false);
                        setFavorite(dataModel.getId(), false);
                        viewHolder.faveIcon.setImageResource(mContext.getResources()
                                .getIdentifier("io.github.oxmose.passlock:drawable/ic_favorite_border",
                                        null, null));
                    }
                    else {
                        dataModel.setFavorite(true);
                        setFavorite(dataModel.getId(), true);
                        viewHolder.faveIcon.setImageResource(mContext.getResources()
                                .getIdentifier("io.github.oxmose.passlock:drawable/ic_favorite",
                                        null, null));
                    }

                    mContext.updateList();
                }
            });
        }

        return convertView;
    }

    @Override
    public void onClick(View v) {
        int position = (Integer)v.getTag();
        Object object = getItem(position);
        ListPasswordRowItem dataModel = (ListPasswordRowItem)object;

        Log.i("COUCOU", "COUCOU HO");

        if(dataModel == null)
            return;

        Log.i("COUCOU", "COUCOU");

        switch (v.getId())
        {
            case R.id.password_item_row_faveicon:
                ImageView image = (ImageView)v;

                if(dataModel.isFavorite()) {
                    dataModel.setFavorite(false);
                    setFavorite(dataModel.getId(), false);
                    image.setImageResource(mContext.getResources()
                            .getIdentifier("io.github.oxmose.passlock:drawable/ic_favorite_border",
                                    null, null));
                }
                else {
                    dataModel.setFavorite(true);
                    setFavorite(dataModel.getId(), true);
                    image.setImageResource(mContext.getResources()
                            .getIdentifier("io.github.oxmose.passlock:drawable/ic_favorite",
                                    null, null));
                }
                break;
        }
    }

    private void setFavorite(int passwordId, boolean favorite) {
        DatabaseSingleton db = DatabaseSingleton.getInstance();
        Password pass = db.getPasswordById(passwordId);
        pass.setFavorite(favorite);
        db.editPassword(pass);
    }
}
