package com.giselletavares.unotes.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.giselletavares.unotes.R;
import com.giselletavares.unotes.activities.AudioActivity;
import com.giselletavares.unotes.models.Attachment;
import com.giselletavares.unotes.utils.Formatting;

import java.util.List;

public class AudioAdapter extends RecyclerView.Adapter<AudioAdapter.AudiosViewHolder> {

    private List<Attachment> mAttachmentList;
    private Context mContext;
    private Formatting formatting;

    public AudioAdapter(List<Attachment> attachmentList, Context context) {
        this.mAttachmentList = attachmentList;
        this.mContext = context;
    }

    @NonNull
    @Override
    public AudiosViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        final Context mContext = viewGroup.getContext();

        View mView;
        mView = LayoutInflater.from(mContext).inflate(R.layout.item_audio_list, viewGroup, false);
        final AudiosViewHolder audiosViewHolder = new AudiosViewHolder(mView);

        audiosViewHolder.mLinearLayout_audio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, AudioActivity.class);
                intent.putExtra("audioId", mAttachmentList.get(audiosViewHolder.getAdapterPosition()).get_id());
                mContext.startActivity(intent);
            }
        });

        return audiosViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull AudioAdapter.AudiosViewHolder audiosViewHolder, int position) {
        Attachment currentAttachment = mAttachmentList.get(position);

        formatting = new Formatting();
        audiosViewHolder.lblCreatedDate.setText(formatting.getDateLongFormatter(currentAttachment.getCreatedDate()));
    }

    @Override
    public int getItemCount() {
        return mAttachmentList.size();
    }

    public static class AudiosViewHolder extends RecyclerView.ViewHolder {

        private LinearLayout mLinearLayout_audio;
        private TextView lblCreatedDate;

        public AudiosViewHolder(@NonNull View itemView) {
            super(itemView);

            this.mLinearLayout_audio = itemView.findViewById(R.id.audioId);
            this.lblCreatedDate = itemView.findViewById(R.id.lblCreatedDate);
        }

    }


}
