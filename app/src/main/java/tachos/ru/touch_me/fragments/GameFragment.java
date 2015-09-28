package tachos.ru.touch_me.fragments;


import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import tachos.ru.touch_me.MainActivity;
import tachos.ru.touch_me.R;

public class GameFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_game, container, false);
        root.findViewById(R.id.bt_game_exit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) getActivity()).startFragmentUsers();
            }
        });
        return root;
    }
}
