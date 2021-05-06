package com.dragontwister.yondekiku.managers;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.dragontwister.yondekiku.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter {
    private final LayoutInflater inflater;
    private final List<JSONObject> messages = new ArrayList<>();

    public MessageAdapter (LayoutInflater inflater) {
        this.inflater = inflater;
    }

    private class SentMessageHolder extends RecyclerView.ViewHolder {

        CardView cardView;
        TextView messageTxt;

        public SentMessageHolder(@NonNull View itemView) {
            super(itemView);

            cardView = itemView.findViewById(R.id.sentCardView);
            messageTxt = itemView.findViewById(R.id.sentTxt);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        view = inflater.from(parent.getContext()).inflate(R.layout.item_sent_message, parent, false);
        return new SentMessageHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        JSONObject message = messages.get(position);

        try {
            SentMessageHolder messageHolder = (SentMessageHolder) holder;
            messageHolder.messageTxt.setText(message.getString("message"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public void addItem (JSONObject jsonObject) {
        messages.add(jsonObject);
        notifyDataSetChanged();
    }

    public void clearItems(){
        messages.clear();
        notifyDataSetChanged();
    }
}
