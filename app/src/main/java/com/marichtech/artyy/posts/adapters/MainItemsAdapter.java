package com.marichtech.artyy.posts.adapters;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
import com.marichtech.artyy.R;
import com.marichtech.artyy.posts.activities.CommentsActivity;
import com.marichtech.artyy.posts.activities.ItemDetailActivity;
import com.marichtech.artyy.posts.activities.ItemPostDetailsActivity;
import com.marichtech.artyy.posts.activities.ItemPostsActivity;
import com.marichtech.artyy.posts.models.MainPost;
import com.marichtech.artyy.posts.models.User;


import de.hdodenhof.circleimageview.CircleImageView;

public class MainItemsAdapter extends RecyclerView.Adapter<MainItemsAdapter.ViewHolder> {

    public List<MainPost> postList;
    public List<User> userList;
    public Context context;
    public MainItemsAdapter adapter;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;


    public MainItemsAdapter(List<MainPost> postList, List<User> userList) {
        this.postList = postList;
        this.userList = userList;
        this.adapter = this;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.main_items, parent, false);
        context = parent.getContext();

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        return new ViewHolder(view);


    }


    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, @SuppressLint("RecyclerView") final int position) {


        String postId = postList.get(position).PostId;
        String  currentUserId = mAuth.getCurrentUser().getUid();

        String number = userList.get(position).getPhone();


        String image_url = postList.get(position).getImage_url();
        String thumb_url = postList.get(position).getThumb_url();
            holder.setPostImage(image_url, thumb_url);

        String title = postList.get(position).getTitle();
            holder.setTitleText(title);

        String user_id = postList.get(position).getUser_id();

        String desc = postList.get(position).getDesc();


        String price = postList.get(position).getPrice();
            holder.setPriceText(price);

        String post_user_id = postList.get(position).getUser_id();

            if (post_user_id.equals(currentUserId)) {
                holder.postDeleteBtn.setEnabled(true);
                holder.postDeleteBtn.setVisibility(View.VISIBLE);

            } else {
                holder.postDeleteBtn.setEnabled(false);
                holder.postDeleteBtn.setVisibility(View.INVISIBLE);


            }

        String  userName = userList.get(position).getName();
        String  userImageUrl = userList.get(position).getImage_url();
        String  userThumbUrl = userList.get(position).getThumb_url();

        String  contact = userList.get(position).getPhone();

           holder.setUserData(userName, userImageUrl, userThumbUrl);

            try {
                long dateInMs = postList.get(position).getCreated_on().getTime();
                String postDate = DateFormat.format("dd MMM yyyy", new Date(dateInMs)).toString();
                holder.setDate(postDate);
            } catch (Exception e) {
                // Toast.makeText(context, "Exception : " + e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("Exception: ", e.getMessage());
            }



        holder.postCallBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phone = contact;
                Intent phoneIntent = new Intent(Intent.ACTION_DIAL, Uri.fromParts(
                        "tel", phone, null));

                context.startActivity(phoneIntent);

            }
        });




        // Get Likes Count
        db.collection("ArtyPosts/" + postId + "/Likes").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot documentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (documentSnapshots != null) {
                    if (!documentSnapshots.isEmpty()) {
                        int count = documentSnapshots.size();
                        holder.updateLikesCount(count);
                    } else {
                        holder.updateLikesCount(0);
                    }
                }
            }
        });

        // Get LikesuserId
        db.collection("ArtyPosts/" + postId + "/Likes").document(currentUserId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (documentSnapshot != null) {
                    if (documentSnapshot.exists()) {
                        holder.postLikeBtn.setImageResource(R.drawable.ic_favorite_24);
                    } else {
                        holder.postLikeBtn.setImageResource(R.drawable.ic_baseline_favorite_border_24);
                    }
                }
            }
        });


        // Like button press handler
        holder.postLikeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                db.collection("ArtyPosts/" + postId + "/Likes").document(currentUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (!task.getResult().exists()) {
                            Map<String, Object> likesMap = new HashMap<>();
                            likesMap.put("timestamp", FieldValue.serverTimestamp());

                            db.collection("ArtyPosts/" + postId + "/Likes").document(currentUserId).set(likesMap);
                        } else {
                            db.collection("ArtyPosts/" + postId + "/Likes").document(currentUserId).delete();
                        }
                    }
                });
            }
        });

        holder.postCommentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent commentIntent = new Intent(context, CommentsActivity.class);
                commentIntent.putExtra("post_id", postId);
                context.startActivity(commentIntent);
            }
        });



        holder.postCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if (post_user_id.equals(currentUserId)) {

                    Intent postDetailIntent = new Intent(context, ItemDetailActivity.class);
                    postDetailIntent.putExtra("ArtistsNumber", number);
                    postDetailIntent.putExtra("Title", title);
                    postDetailIntent.putExtra("Desc", desc);
                    postDetailIntent.putExtra("Price", price);
                    postDetailIntent.putExtra("UserId", user_id);
                    postDetailIntent.putExtra("ImageUrl", image_url);
                    postDetailIntent.putExtra("ThumbUrl", thumb_url);
                    context.startActivity(postDetailIntent);


                } else {
                    Intent postDetailIntent = new Intent(context, ItemPostDetailsActivity.class);
                    postDetailIntent.putExtra("ArtistsNumber", number);
                    postDetailIntent.putExtra("Title", title);
                    postDetailIntent.putExtra("Desc", desc);
                    postDetailIntent.putExtra("Price", price);
                    postDetailIntent.putExtra("UserId", user_id);
                    postDetailIntent.putExtra("ImageUrl", image_url);
                    postDetailIntent.putExtra("ThumbUrl", thumb_url);
                    context.startActivity(postDetailIntent);


                }

            }
        });

        holder.postDeleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(context)
                        .setTitle("Delete Post")
                        .setMessage("You are about to delete this post, are you sure?")
                        .setIcon(R.drawable.ic_baseline_delete)
                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                db.collection("Arty/Posts/Data").document(postId).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        postList.remove(position);
                                        userList.remove(position);
                                        adapter.notifyDataSetChanged();
                                        Toast.makeText(context, "Post deleted", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }).setNegativeButton("Cancel", null).show();
            }
        });


    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private View mView;
        private ImageView postImageView, postCallBtn,postCommentBtn, postLikeBtn;
        private TextView titleView, priceView, postDateView, postUserName,
                postLikeCount;

        private CircleImageView postUserImageView;
        private ImageButton postDeleteBtn;
        private CardView postCardView;


        public ViewHolder(View itemView) {
            super(itemView);
            mView = itemView;

            postCardView = mView.findViewById(R.id.main_post);
            postDeleteBtn = mView.findViewById(R.id.post_delete_btn);
            postCallBtn = mView.findViewById(R.id.post_call_btn);
            postLikeBtn = mView.findViewById(R.id.post_like_btn);
            postCommentBtn = mView.findViewById(R.id.post_comment_btn);
        }


        public void setTitleText(String titleText) {
            titleView = mView.findViewById(R.id.post_title);
            titleView.setText(titleText);
        }





        public void setPriceText(String priceText) {
            priceView = mView.findViewById(R.id.post_price);
            priceView.setText("Ksh "+priceText);
        }

        public void setPostImage(String downloadUri, String downloadThumbUri) {
            postImageView = mView.findViewById(R.id.post_image);

            RequestOptions placeholderOptions = new RequestOptions();
            placeholderOptions.placeholder(R.drawable.default_image);

            Glide.with(context)
                    .applyDefaultRequestOptions(placeholderOptions)
                    .load(downloadUri)
                    .thumbnail(Glide.with(context)
                            .load(downloadThumbUri))
                    .into(postImageView);
        }

        public void setDate(String date) {
            postDateView = mView.findViewById(R.id.post_date);
            postDateView.setText(date);
        }

        public void updateLikesCount(int count) {
            postLikeCount = mView.findViewById(R.id.post_like_count);
            postLikeCount.setText(count + " Likes");
        }

        public void setUserData(String name, String imageUrl,  String imageThumb) {
            postUserName = mView.findViewById(R.id.post_username);
            postUserImageView = mView.findViewById(R.id.post_user_image);

            postUserName.setText(name);

            RequestOptions placeholderOptions = new RequestOptions();
            placeholderOptions.placeholder(R.drawable.default_user_art_g_2);

            Glide.with(context)
                    .applyDefaultRequestOptions(placeholderOptions)
                    .load(imageUrl)
                    .thumbnail(Glide.with(context)
                            .load(imageThumb))
                    .into(postUserImageView);

        }

    }
}
