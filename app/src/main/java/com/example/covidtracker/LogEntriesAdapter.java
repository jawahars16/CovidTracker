package com.example.covidtracker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class LogEntriesAdapter extends RecyclerView.Adapter<LogEntriesAdapter.LogViewHolder> {
    private static DecimalFormat df2 = new DecimalFormat("#.#");
    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");

    class LogViewHolder extends RecyclerView.ViewHolder {
        private final TextView rssi;
        private final TextView address;
        private final TextView timestamp;
        private final TextView distance;

        private LogViewHolder(View itemView) {
            super(itemView);
            rssi = itemView.findViewById(R.id.rssi);
            address = itemView.findViewById(R.id.address);
            timestamp = itemView.findViewById(R.id.time);
            distance = itemView.findViewById(R.id.distance);
        }
    }

    private final LayoutInflater mInflater;
    private List<LogEntry> entries; // Cached copy of words

    LogEntriesAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public LogViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.recyclerview_item, parent, false);
        return new LogViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(LogViewHolder holder, int position) {
        if (entries != null) {
            LogEntry current = entries.get(position);
            String name = current.getDeviceName();
            holder.rssi.setText("Signal strength: " + current.getRssi());
            holder.address.setText("Device ID: " + current.getDeviceAddress());
            holder.distance.setText(df2.format(current.getDistance()) + " meters away");
            holder.timestamp.setText("Time: " + simpleDateFormat.format(new Date(current.getCreatedAt())));
        } else {
            holder.address.setText("No Word");
        }
    }

    void setData(List<LogEntry> words) {
        entries = words;
        notifyDataSetChanged();
    }

    // getItemCount() is called many times, and when it is first called,
    // mWords has not been updated (means initially, it's null, and we can't return null).
    @Override
    public int getItemCount() {
        if (entries != null)
            return entries.size();
        else return 0;
    }
}
