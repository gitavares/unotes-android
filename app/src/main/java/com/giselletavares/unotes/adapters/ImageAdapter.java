package com.giselletavares.unotes.adapters;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.giselletavares.unotes.R;
import com.giselletavares.unotes.activities.ImageActivity;
import com.giselletavares.unotes.models.AppDatabase;
import com.giselletavares.unotes.models.Attachment;

import java.io.File;
import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImagesViewHolder> {

    public static AppDatabase sAppDatabase;
    private List<Attachment> mAttachmentList;
    private Context mContext;

    public ImageAdapter(List<Attachment> attachmentList, Context context) {
        this.mAttachmentList = attachmentList;
        this.mContext = context;
    }

    @NonNull
    @Override
    public ImagesViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        final Context mContext = viewGroup.getContext();

        View mView;
        mView = LayoutInflater.from(mContext).inflate(R.layout.item_image_list, viewGroup, false);
        final ImagesViewHolder imagesViewHolder = new ImagesViewHolder(mView);

        imagesViewHolder.mLinearLayout_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            Intent intent = new Intent(mContext, ImageActivity.class);
            intent.putExtra("imageId", mAttachmentList.get(imagesViewHolder.getAdapterPosition()).get_id());
            mContext.startActivity(intent);
            }
        });

        imagesViewHolder.mLinearLayout_image.setOnLongClickListener(new View.OnLongClickListener() {
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

                        Attachment currentAttachment = mAttachmentList.get(imagesViewHolder.getAdapterPosition());
                        sAppDatabase.mAttachmentDAO().deleteAttachment(currentAttachment);
                        alertDialog.dismiss();
                        mAttachmentList.remove(imagesViewHolder.getAdapterPosition());
                        notifyDataSetChanged();

                        sAppDatabase.close();
                    }
                });
                alertDialog.show();
                return false;
            }
        });

        return imagesViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ImagesViewHolder imagesViewHolder, int position) {
        Attachment currentAttachment = mAttachmentList.get(position);

        File imgFile = new  File(currentAttachment.getFilename());
        imagesViewHolder.ivImage.setImageBitmap(BitmapFactory.decodeFile(imgFile.getAbsolutePath()));
    }

    @Override
    public int getItemCount() {
        return mAttachmentList.size();
    }

    public static class ImagesViewHolder extends RecyclerView.ViewHolder {

        private LinearLayout mLinearLayout_image;
        private ImageView ivImage;

        public ImagesViewHolder(@NonNull View itemView) {
            super(itemView);

            this.mLinearLayout_image = itemView.findViewById(R.id.imageId);
            this.ivImage = itemView.findViewById(R.id.ivImage);
        }

    }
}
