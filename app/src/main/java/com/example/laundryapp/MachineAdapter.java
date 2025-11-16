package com.example.laundryapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MachineAdapter extends RecyclerView.Adapter<MachineAdapter.MachineViewHolder> {

    private final List<MachineItem> machines;

    public MachineAdapter(List<MachineItem> machines) {
        this.machines = machines;
    }

    @NonNull
    @Override
    public MachineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_machine_card, parent, false);
        return new MachineViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MachineViewHolder holder, int position) {
        MachineItem m = machines.get(position);

        holder.machineName.setText(m.name);
        // Handle null or empty status - default to "idle" if not set
        String status = (m.status != null && !m.status.isEmpty()) ? m.status : "idle";
        holder.machineStatus.setText(status);

        // icon logic based on machine type
        if (m.iconResId != 0) {
            holder.machineIcon.setImageResource(m.iconResId);
        } else {
            // fallback icons if you haven't made proper washer/dryer icons yet
            if ("washer".equalsIgnoreCase(m.type)) {
                holder.machineIcon.setImageResource(android.R.drawable.ic_menu_rotate);
            } else if ("dryer".equalsIgnoreCase(m.type)) {
                holder.machineIcon.setImageResource(android.R.drawable.ic_menu_week);
            } else {
                holder.machineIcon.setImageResource(android.R.drawable.ic_menu_manage);
            }
        }

        //make it possible to open the machine detail
        holder.itemView.setOnClickListener(v->
        {
            android.content.Intent intent = new android.content.Intent(v.getContext(), MachineDetails.class);

            intent.putExtra("machineName", m.name);
            intent.putExtra("cycleTime", m.cycleTime);
            intent.putExtra("telemetry", m.telemetry);

            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return machines.size();
    }

    static class MachineViewHolder extends RecyclerView.ViewHolder {
        ImageView machineIcon;
        TextView machineName;
        TextView machineStatus;
        MachineViewHolder(@NonNull View itemView) {
            super(itemView);
            machineIcon = itemView.findViewById(R.id.machineIcon);
            machineName = itemView.findViewById(R.id.machineName);
            machineStatus = itemView.findViewById(R.id.machineStatus);
        }
    }
}
