package com.example.booksmart.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;
import com.example.booksmart.BookingConfirmationActivity;
import com.example.booksmart.R;
import com.example.booksmart.models.BookingTemplate;
import java.util.List;

/**
 * RecyclerView adapter for displaying booking templates.
 * Handles the display and interaction of saved booking templates.
 */
public class TemplatesAdapter extends RecyclerView.Adapter<TemplatesAdapter.TemplateViewHolder> {
    private List<BookingTemplate> templates;
    private Context context;

    public TemplatesAdapter(Context context, List<BookingTemplate> templates) {
        this.context = context;
        this.templates = templates;
    }

    @NonNull
    @Override
    public TemplateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_template, parent, false);
        return new TemplateViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TemplateViewHolder holder, int position) {
        BookingTemplate template = templates.get(position);
        holder.templateName.setText(template.getTemplateName());
        holder.spaceType.setText(template.getSpaceType());
        holder.timeRange.setText(String.format("%s - %s",
                template.getStartTime(), template.getEndTime()));

        holder.templateCard.setOnClickListener(v -> {
            Intent intent = new Intent(context, BookingConfirmationActivity.class);
            intent.putExtra("templateId", template.getTemplateId());
            intent.putExtra("spaceType", template.getSpaceType());
            intent.putExtra("templateName", template.getTemplateName());
            intent.putExtra("startTime", template.getStartTime());
            intent.putExtra("endTime", template.getEndTime());
            intent.putExtra("fromTemplate", true);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return templates.size();
    }

    static class TemplateViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView templateCard;
        TextView templateName, spaceType, timeRange;

        TemplateViewHolder(@NonNull View itemView) {
            super(itemView);
            templateCard = itemView.findViewById(R.id.templateCard);
            templateName = itemView.findViewById(R.id.templateName);
            spaceType = itemView.findViewById(R.id.spaceType);
            timeRange = itemView.findViewById(R.id.timeRange);
        }
    }
}