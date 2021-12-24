package com.marichtech.artyy.fragments.dashboard;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.marichtech.artyy.R;
import com.marichtech.artyy.posts.adapters.MainItemsAdapter;
import com.marichtech.artyy.posts.models.MainPost;
import com.marichtech.artyy.posts.models.User;

import jp.wasabeef.recyclerview.animators.SlideInLeftAnimator;

import java.util.ArrayList;
import java.util.List;


public class DashboardFragment extends Fragment {

    private RecyclerView postListView;
    public List<MainPost> postList;
    public List<User> userList;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private MainItemsAdapter postRecyclerAdapter;

    private DocumentSnapshot lastVisible;
    private Boolean firstLoad = true;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        postList = new ArrayList<>();
        userList = new ArrayList<>();
        postListView = view.findViewById(R.id.main_listView);

        mAuth = FirebaseAuth.getInstance();

        postRecyclerAdapter = new MainItemsAdapter(postList, userList);

        postListView.setItemAnimator(new SlideInLeftAnimator());
        postListView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        postListView.setLayoutManager(new LinearLayoutManager(getActivity()));
        postListView.setHasFixedSize(true);
        postListView.setAdapter(postRecyclerAdapter);





        allDataShow();



        // Inflate the layout for this fragment
        return view;
    }


    public void loadPost() {
        if (mAuth.getCurrentUser() != null) {
            // Sort results by descending (latest posts first)
            Query nextQuery = db.collection("Arty/Posts/Data")
                    .startAfter(lastVisible)
                    .limit(10);

            // Set real time database listener
            nextQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot documentSnapshots, @Nullable FirebaseFirestoreException e) {
                    if (documentSnapshots != null) {
                        if (!documentSnapshots.isEmpty()) {
                            lastVisible = documentSnapshots.getDocuments().get(documentSnapshots.size() - 1);
                            for (DocumentChange doc: documentSnapshots.getDocumentChanges()) {
                                if (doc.getType() == DocumentChange.Type.ADDED) {
                                    String newPostId = doc.getDocument().getId();
                                    final MainPost newPost = doc.getDocument().toObject(MainPost.class).withId(newPostId);

                                    String postUserId = doc.getDocument().getString("user_id");

                                    db.collection("Arty/User/Artists").document(postUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            if (task.isSuccessful()) {

                                                User user = task.getResult().toObject(User.class);

                                                userList.add(user);
                                                postList.add(newPost);

                                            }

                                            postRecyclerAdapter.notifyDataSetChanged();
                                        }
                                    });
                                }
                            }
                        }
                    }
                }
            });
        }
    }



//show all the data

    public void allDataShow(){

        if (mAuth.getCurrentUser() != null) {

            db = FirebaseFirestore.getInstance();

            postListView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    Boolean reachedBottom = !recyclerView.canScrollVertically(1);

                    if (reachedBottom) {
                         Toast.makeText(getContext(), "Reached bottom", Toast.LENGTH_SHORT).show();
                        loadPost();
                    }
                }
            });

            // Sort results by descending (latest posts first)
            Query firstQuery = db.collection("Arty/Posts/Data")
                    //.whereEqualTo("on sale", true);
                    .limit(10);

            // Set real time database listener
            firstQuery.addSnapshotListener( new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot documentSnapshots, @Nullable FirebaseFirestoreException e) {
                    if (documentSnapshots != null) {
                        if (!documentSnapshots.isEmpty()) {

                            if (firstLoad) {
                                lastVisible = documentSnapshots.getDocuments().get(documentSnapshots.size() - 1);
                                postList.clear();
                                userList.clear();
                            }

                            //clear userList and postList to avoid duplication

                            userList.clear();
                            postList.clear();

                            ///////////////////////

                            for (DocumentChange doc: documentSnapshots.getDocumentChanges()) {
                                if (doc.getType() == DocumentChange.Type.ADDED) {

                                    String newPostId = doc.getDocument().getId();
                                    final MainPost newPost = doc.getDocument().toObject(MainPost.class).withId(newPostId);

                                    String postUserId = doc.getDocument().getString("user_id");

                                    db.collection("Arty/User/Artists").document(postUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            if (task.isSuccessful()) {

                                                User user = task.getResult().toObject(User.class);

                                                if (firstLoad) {
                                                    userList.add(user);
                                                    postList.add(newPost);
                                                } else {
                                                    userList.add(0, user);
                                                    postList.add(0, newPost);
                                                }

                                            }

                                            postRecyclerAdapter.notifyDataSetChanged();
                                        }
                                    });


                                }
                            }

                            firstLoad = false;
                        }
                    }
                }
            });
        }

    }

}
