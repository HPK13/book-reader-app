package com.example.e_book1;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class EbookAdapter extends ArrayAdapter<Ebook> {
    private List<Ebook> ebooks;
    private Context context;

    public EbookAdapter(Context context, List<Ebook> ebooks) {
        super(context, 0, ebooks);
        this.context = context;
        this.ebooks = ebooks;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_ebook, parent, false);
        }

        Ebook ebook = getItem(position);
        TextView titleTextView = convertView.findViewById(R.id.titleTextView);
        TextView authorTextView = convertView.findViewById(R.id.authorTextView);

        if (ebook != null) {
            titleTextView.setText(ebook.getTitle() != null ? ebook.getTitle() : "Unknown Title");
            authorTextView.setText(ebook.getAuthor() != null ? ebook.getAuthor() : "Unknown Author");
        }

        return convertView;
    }


    public void addAll(List<Ebook> ebooks) {
        this.ebooks.clear();
        this.ebooks.addAll(ebooks);
        notifyDataSetChanged();
    }
}
