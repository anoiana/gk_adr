package com.example.ad_gk.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.ad_gk.R;
import com.example.ad_gk.adapter.UserAdapter;
import com.example.ad_gk.model.User;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class ListUserFragment extends Fragment {

    private RecyclerView recyclerViewUser;
    private UserAdapter userAdapter;
    private List<User> userList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list_user, container, false);
        recyclerViewUser = view.findViewById(R.id.recyclerViewUser);

        userList = new ArrayList<>();
        userList.add(new User("John Doe", "john@example.com"));
        userList.add(new User("Jane Smith", "jane@example.com"));
        userList.add(new User("Alice Brown", "alice@example.com"));

        userAdapter = new UserAdapter(userList);
        recyclerViewUser.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewUser.setAdapter(userAdapter);

        return view;
    }
}