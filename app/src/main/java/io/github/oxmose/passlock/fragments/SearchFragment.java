package io.github.oxmose.passlock.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.mancj.materialsearchbar.MaterialSearchBar;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import io.github.oxmose.passlock.PasswordViewActivity;
import io.github.oxmose.passlock.R;
import io.github.oxmose.passlock.adapters.ListPasswordRowAdapter;
import io.github.oxmose.passlock.data.Session;
import io.github.oxmose.passlock.database.DatabaseSingleton;
import io.github.oxmose.passlock.database.Password;
import io.github.oxmose.passlock.database.User;
import io.github.oxmose.passlock.model.ListPasswordRowItem;
import io.github.oxmose.passlock.tools.ApplicationContextProvider;

public class SearchFragment extends Fragment {

    private TextView infoTextView;
    private ListView searchPasswordListview;
    private MaterialSearchBar searchBar;

    private List<ListPasswordRowItem> listItems = null;
    private ListPasswordRowAdapter listAdapter;

    public SearchFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        /* Get components */
        searchPasswordListview = view.findViewById(R.id.fragment_search_llistview);
        searchBar = view.findViewById(R.id.fragment_search_searchbar);
        infoTextView = view.findViewById(R.id.fragment_search_searchinfo);

        /* Init compoments */
        initUI();
    }

    private void initUI() {
        if(listItems == null) {
            listItems = new ArrayList<>();
        }

        listAdapter = new ListPasswordRowAdapter(listItems, ApplicationContextProvider.getContext());

        searchPasswordListview.setAdapter(listAdapter);
        searchPasswordListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ListPasswordRowItem item = (ListPasswordRowItem)parent.getItemAtPosition(position);

                /* Launch password view activity */
                Intent i = new Intent(getContext(), PasswordViewActivity.class);
                i.putExtra("passwordId", item.getId());
                startActivity(i);
            }
        });

        searchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
            @Override
            public void onSearchStateChanged(boolean enabled) {

            }

            @Override
            public void onSearchConfirmed(CharSequence text) {
                listItems.clear();
                listAdapter.notifyDataSetChanged();

                infoTextView.setText(R.string.searching);
                infoTextView.setVisibility(View.VISIBLE);

                new StartSearchAsync(SearchFragment.this).execute();
            }

            @Override
            public void onButtonClicked(int buttonCode) {

            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private static class StartSearchAsync extends AsyncTask<Void, Void, Void> {
        private WeakReference<SearchFragment> fragmentRef;

        StartSearchAsync(SearchFragment fragmentRef) {
            this.fragmentRef = new WeakReference<>(fragmentRef);
        }

        @Override
        protected Void doInBackground(Void... params) {

            // get a reference to the activity if it is still there
            SearchFragment fragment = fragmentRef.get();
            if (fragment == null || fragment.isDetached()) return null;

            fragment.listItems.clear();

            DatabaseSingleton db = DatabaseSingleton.getInstance();
            User user = Session.getInstance().getCurrentUser();

            List<Password> passwordsList = db.getUserPasswordsNonAsync(user, fragment.searchBar.getText());

            for(Password password: passwordsList) {
                String title = password.getName();
                String value = "Tap to decrypt password...";

                ListPasswordRowItem.ITEM_TYPE type;
                if(password.isPassword()) {
                    type = ListPasswordRowItem.ITEM_TYPE.PASSWORD;
                }
                else if(password.isPin()) {
                    type = ListPasswordRowItem.ITEM_TYPE.PIN;
                }
                else if(password.isDigicode()) {
                    type = ListPasswordRowItem.ITEM_TYPE.DIGICODE;
                }
                else {
                    type = ListPasswordRowItem.ITEM_TYPE.PASSWORD;
                }

                ListPasswordRowItem newItem = new ListPasswordRowItem(title, value, type, password.getId());
                fragment.listItems.add(newItem);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            // get a reference to the activity if it is still there
            SearchFragment fragment = fragmentRef.get();
            if (fragment == null || fragment.isDetached()) return;

            if(fragment.listItems.isEmpty()) {
                fragment.infoTextView.setText(R.string.no_result);
                fragment.infoTextView.setVisibility(View.VISIBLE);
            }
            else {
                fragment.listAdapter.notifyDataSetChanged();
                fragment.infoTextView.setVisibility(View.INVISIBLE);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        searchBar.setText("");
        listItems.clear();
        listAdapter.notifyDataSetChanged();

        infoTextView.setText(R.string.searching);
        infoTextView.setVisibility(View.VISIBLE);

        new StartSearchAsync(this).execute();
    }
}
