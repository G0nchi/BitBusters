package com.example.bitbusters.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bitbusters.R;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class SuperadminNotificationsAdapter
        extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface OnItemClickListener  { void onClick(Item item); }
    public interface OnDeleteReadListener { void onDeleteRead(); }

    // ── Entry types ─────────────────────────────────────────────────────────

    public abstract static class Entry {
        static final int TYPE_HEADER = 0;
        static final int TYPE_ITEM   = 1;
        abstract int type();
    }

    public static class Header extends Entry {
        public final String title;
        public final boolean showDelete;
        public Header(String title, boolean showDelete) {
            this.title      = title;
            this.showDelete = showDelete;
        }
        @Override int type() { return TYPE_HEADER; }
    }

    public static class NotifEntry extends Entry {
        public final Item item;
        public NotifEntry(Item item) { this.item = item; }
        @Override int type() { return TYPE_ITEM; }
    }

    public static class Item {
        public final String id;
        public final int iconRes, iconTint, iconBg;
        public final String titulo, descripcion, tiempo;
        public final Class<?> destination;
        public final boolean read;

        public Item(String id, int iconRes, int iconTint, int iconBg,
                    String titulo, String descripcion, String tiempo,
                    Class<?> destination, boolean read) {
            this.id          = id;
            this.iconRes     = iconRes;
            this.iconTint    = iconTint;
            this.iconBg      = iconBg;
            this.titulo      = titulo;
            this.descripcion = descripcion;
            this.tiempo      = tiempo;
            this.destination = destination;
            this.read        = read;
        }
    }

    // ── Adapter ─────────────────────────────────────────────────────────────

    private final List<Entry> entries;
    private final OnItemClickListener  clickListener;
    private final OnDeleteReadListener deleteReadListener;

    public SuperadminNotificationsAdapter(List<Entry> entries,
                                          OnItemClickListener clickListener,
                                          OnDeleteReadListener deleteReadListener) {
        this.entries            = entries;
        this.clickListener      = clickListener;
        this.deleteReadListener = deleteReadListener;
    }

    public void removeEntryAt(int position) {
        entries.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public int getItemViewType(int position) {
        return entries.get(position).type();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inf = LayoutInflater.from(parent.getContext());
        if (viewType == Entry.TYPE_HEADER) {
            return new HeaderVH(inf.inflate(R.layout.item_superadmin_notif_header, parent, false));
        }
        return new ItemVH(inf.inflate(R.layout.item_superadmin_notification, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderVH) {
            bindHeader((HeaderVH) holder, (Header) entries.get(position));
        } else {
            bindItem((ItemVH) holder, ((NotifEntry) entries.get(position)).item);
        }
    }

    private void bindHeader(HeaderVH h, Header header) {
        h.tvTitle.setText(header.title);
        if (header.showDelete) {
            h.tvDelete.setVisibility(View.VISIBLE);
            h.tvDelete.setOnClickListener(v -> deleteReadListener.onDeleteRead());
        } else {
            h.tvDelete.setVisibility(View.GONE);
        }
    }

    private void bindItem(ItemVH h, Item item) {
        Context ctx = h.itemView.getContext();

        h.cvIcon.setCardBackgroundColor(ctx.getColor(item.iconBg));
        h.ivIcon.setImageResource(item.iconRes);
        h.ivIcon.setColorFilter(ctx.getColor(item.iconTint));
        h.tvTitulo.setText(item.titulo);
        h.tvDescripcion.setText(item.descripcion);
        h.tvTiempo.setText(item.tiempo);

        if (item.read) {
            h.dotUnread.setVisibility(View.GONE);
            h.tvTitulo.setTypeface(null, Typeface.NORMAL);
            h.itemView.setAlpha(0.55f);
        } else {
            h.dotUnread.setVisibility(View.VISIBLE);
            h.tvTitulo.setTypeface(null, Typeface.BOLD);
            h.itemView.setAlpha(1f);
        }

        h.itemView.setOnClickListener(v -> clickListener.onClick(item));
    }

    @Override
    public int getItemCount() { return entries.size(); }

    // ── ViewHolders ─────────────────────────────────────────────────────────

    public static class HeaderVH extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDelete;
        HeaderVH(@NonNull View v) {
            super(v);
            tvTitle  = v.findViewById(R.id.tvSectionTitle);
            tvDelete = v.findViewById(R.id.tvDeleteRead);
        }
    }

    public static class ItemVH extends RecyclerView.ViewHolder {
        View dotUnread;
        MaterialCardView cvIcon;
        ImageView ivIcon;
        TextView tvTitulo, tvDescripcion, tvTiempo;
        ItemVH(@NonNull View v) {
            super(v);
            dotUnread     = v.findViewById(R.id.dotUnread);
            cvIcon        = v.findViewById(R.id.cvIcon);
            ivIcon        = v.findViewById(R.id.ivIcon);
            tvTitulo      = v.findViewById(R.id.tvTitulo);
            tvDescripcion = v.findViewById(R.id.tvDescripcion);
            tvTiempo      = v.findViewById(R.id.tvTiempo);
        }
    }
}
