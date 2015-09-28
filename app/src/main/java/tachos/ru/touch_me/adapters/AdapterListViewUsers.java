package tachos.ru.touch_me.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.Date;
import java.util.List;

import tachos.ru.touch_me.R;
import tachos.ru.touch_me.data.Users;

public class AdapterListViewUsers extends BaseAdapter {
    private List<Users> users;
    private LayoutInflater inflater;

    public AdapterListViewUsers(List<Users> users, LayoutInflater inflater) {
        this.users = users;
        this.inflater = inflater;
    }

    @Override
    public int getCount() {
        return users.size();
    }

    @Override
    public Users getItem(int position) {
        return users.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View rowView;
        TextView tvName;
        TextView tvLastActivity;
        rowView = inflater.inflate(R.layout.item_users, null);
        tvName = (TextView) rowView.findViewById(R.id.tv_item_users_name);
        tvLastActivity = (TextView) rowView.findViewById(R.id.tv_item_users_lastActivity);

        Users user = users.get(position);
        tvName.setText(user.getName());
        if (user.getLastOnline() != 0)
            tvLastActivity.setText("" + new Date(users.get(position).getLastOnline()));
        else tvLastActivity.setText("never");
        return rowView;
    }
}