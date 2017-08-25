package com.sbc.sk.schedulehelper;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class FirstExcute3Fragment extends Fragment {
    public FirstExcute3Fragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_first_excute3, container, false);

        View view = inflater.inflate(R.layout.fragment_first_excute3, container, false);
        Button btn_firstexcute_finish = (Button) view.findViewById(R.id.btn_firstexcute_finish);
        btn_firstexcute_finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });
        return view;
    }
}
