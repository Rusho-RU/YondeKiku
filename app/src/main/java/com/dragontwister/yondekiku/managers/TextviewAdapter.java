package com.dragontwister.yondekiku.managers;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.dragontwister.yondekiku.R;

import java.util.ArrayList;

public class TextviewAdapter extends BaseAdapter {
    private ArrayList<String> listViewItems;

    public LayoutInflater mInflater;

    public TextviewAdapter(Activity act) {
        this.listViewItems = new ArrayList<>();
        mInflater = (LayoutInflater) act.getApplicationContext().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null)
            convertView = mInflater.inflate(R.layout.listview_item, null);
        TextView text_of_item = (TextView) convertView.findViewById(R.id.textView_item);
        text_of_item.setText(listViewItems.get(position));

        return convertView;
    }

    @Override
    public int getItemViewType(int position) {
        //you can achive this part via using integer IDs. Just change your ArrayList<String> to ArrayList<int>

//        if (listViewItems.get(position).equals("item")) {//I suppose you give unique names to your subitems
//            return TYPE_ITEM;
//        } else if (listViewItems.get(position).equals("subitem")) {//I suppose you give unique names to your subitems
//            return TYPE_SUBITEM;
//        }

        return position;
    }

    @Override
    public int getViewTypeCount () {
        return 1;
    }


    @Override
    public int getCount () {
        return listViewItems.size();
    }

    @Override
    public String getItem ( int position){
        return listViewItems.get(position);
    }

    @Override
    public long getItemId ( int position){
        return position;
    }

    public void addItem (String item){
        listViewItems.add(item);
        notifyDataSetChanged();
    }

    public void removeItem ( int position){
        listViewItems.remove(position);
        notifyDataSetChanged();
    }
}