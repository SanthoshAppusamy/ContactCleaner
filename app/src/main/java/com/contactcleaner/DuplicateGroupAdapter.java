package com.contactcleaner;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class DuplicateGroupAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_CONTACT = 1;
    private List<Object> flatItems;

    public DuplicateGroupAdapter(List<Object> flatItems) { this.flatItems = flatItems; }

    @Override
    public int getItemViewType(int position) {
        return (flatItems.get(position) instanceof DuplicateGroup) ? TYPE_HEADER : TYPE_CONTACT;
    }

    @NonNull @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inf = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_HEADER) {
            return new GroupHeaderVH(inf.inflate(R.layout.item_group_header, parent, false));
        }
        return new ContactVH(inf.inflate(R.layout.item_contact, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof GroupHeaderVH) {
            ((GroupHeaderVH) holder).bind((DuplicateGroup) flatItems.get(position));
        } else {
            ((ContactVH) holder).bind((ContactItem) flatItems.get(position));
        }
    }

    @Override public int getItemCount() { return flatItems.size(); }

    static class GroupHeaderVH extends RecyclerView.ViewHolder {
        TextView tvGroupLabel, tvGroupCount;
        GroupHeaderVH(View v) {
            super(v);
            tvGroupLabel = v.findViewById(R.id.tvGroupLabel);
            tvGroupCount = v.findViewById(R.id.tvGroupCount);
        }
        void bind(DuplicateGroup g) {
            tvGroupLabel.setText("Number: " + g.getNormalizedNumber());
            tvGroupCount.setText(g.getContacts().size() + " duplicate entries");
        }
    }

    static class ContactVH extends RecyclerView.ViewHolder {
        TextView tvName, tvNumber;
        CheckBox cbSelect;
        ContactVH(View v) {
            super(v);
            tvName = v.findViewById(R.id.tvContactName);
            tvNumber = v.findViewById(R.id.tvContactNumber);
            cbSelect = v.findViewById(R.id.cbSelect);
        }
        void bind(ContactItem c) {
            tvName.setText(c.getName());
            tvNumber.setText(c.getRawNumber());
            cbSelect.setOnCheckedChangeListener(null);
            cbSelect.setChecked(c.isSelected());
            cbSelect.setOnCheckedChangeListener((btn, checked) -> c.setSelected(checked));
            itemView.setOnClickListener(v -> { c.setSelected(!c.isSelected()); cbSelect.setChecked(c.isSelected()); });
        }
    }
}
