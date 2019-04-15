package com.giselletavares.unotes.adapters;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.giselletavares.unotes.R;
import com.giselletavares.unotes.activities.AudioActivity;
import com.giselletavares.unotes.models.AppDatabase;
import com.giselletavares.unotes.models.Attachment;
import com.giselletavares.unotes.models.Note;
import com.giselletavares.unotes.utils.Formatting;

import java.util.List;

public class AudioAdapter extends RecyclerView.Adapter<AudioAdapter.AudiosViewHolder> {

    public static AppDatabase sAppDatabase;
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

        audiosViewHolder.mLinearLayout_audio.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                final AlertDialog alertDialog =new AlertDialog.Builder(mContext).create();
                alertDialog.setTitle("Are you want to delete this");
                alertDialog.setCancelable(false);
                alertDialog.setMessage("By deleting this, item will permanently be deleted. Are you still want to delete this?");
                alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

                alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        // DATABASE
                        sAppDatabase = Room.databaseBuilder(mContext, AppDatabase.class, "unotes")
                                .allowMainThreadQueries() // it will allow the database works on the main thread
                                .fallbackToDestructiveMigration() // because i wont implement now migrations
                                .build();

                        Attachment currentAttachment = mAttachmentList.get(audiosViewHolder.getAdapterPosition());
                        sAppDatabase.mAttachmentDAO().deleteAttachment(currentAttachment);
                        alertDialog.dismiss();
                        mAttachmentList.remove(audiosViewHolder.getAdapterPosition());
                        notifyDataSetChanged();

                        sAppDatabase.close();
                    }
                });
                alertDialog.show();
                return false;
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
