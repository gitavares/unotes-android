package com.giselletavares.unotes.adapters;

import android.arch.persistence.room.Room;
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
import com.giselletavares.unotes.activities.NotesActivity;
import com.giselletavares.unotes.models.AppDatabase;
import com.giselletavares.unotes.models.Category;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoriesViewHolder> {

    public static AppDatabase sAppDatabase;
    private List<Category> mCategoryList;
    private Context mContext;

    public CategoryAdapter(List<Category> categoryList, Context context) {
        this.mCategoryList = categoryList;
        this.mContext = context;
    }


    @NonNull
    @Override
    public CategoriesViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        final Context mContext = viewGroup.getContext();

        View mView;
        mView = LayoutInflater.from(mContext).inflate(R.layout.item_category_list, viewGroup, false);
        final CategoriesViewHolder categoriesViewHolder = new CategoriesViewHolder(mView);

        categoriesViewHolder.mLinearLayout_category.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            Intent intent = new Intent(mContext, NotesActivity.class);
            intent.putExtra("categoryId", mCategoryList.get(categoriesViewHolder.getAdapterPosition()).get_id());
            mContext.startActivity(intent);
            }
        });

        return categoriesViewHolder;

    }

    @Override
    public void onBindViewHolder(@NonNull CategoriesViewHolder categoriesViewHolder, int position) {

        // DATABASE
        sAppDatabase = Room.databaseBuilder(mContext, AppDatabase.class, "unotes")
                .allowMainThreadQueries() // it will allow the database works on the main thread
                .fallbackToDestructiveMigration() // because i wont implement now migrations
                .build();

        Category currentCategory = mCategoryList.get(position);
        int count = sAppDatabase.mNoteDAO().getNumOfNotesByCategory(currentCategory.get_id());

        categoriesViewHolder.lblCategoryName.setText(currentCategory.getName());
        categoriesViewHolder.lblNumOfNotes.setText(count + " notes");

        sAppDatabase.close();
    }

    @Override
    public int getItemCount() {
        return mCategoryList.size();
    }

    public static class CategoriesViewHolder extends RecyclerView.ViewHolder {

        private LinearLayout mLinearLayout_category;
        private TextView lblCategoryName;
        private TextView lblNumOfNotes;

        public CategoriesViewHolder(@NonNull View itemView) {
            super(itemView);

            this.mLinearLayout_category = itemView.findViewById(R.id.categoryId);
            this.lblCategoryName = itemView.findViewById(R.id.lblCategoryName);
            this.lblNumOfNotes = itemView.findViewById(R.id.lblNumOfNotes);
        }

    }


}
