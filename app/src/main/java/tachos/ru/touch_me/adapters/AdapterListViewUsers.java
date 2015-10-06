package tachos.ru.touch_me.adapters;

import android.app.Activity;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

import tachos.ru.touch_me.R;
import tachos.ru.touch_me.data.Avatar;
import tachos.ru.touch_me.data.Users;

public class AdapterListViewUsers extends BaseAdapter {
    private List<Users> users;
    private Activity activity;

    public AdapterListViewUsers(List<Users> users, Activity activity) {
        this.users = users;
        this.activity = activity;
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
        View rowView = convertView;
        ViewHolder holder;
        if (rowView == null) {
            holder = new ViewHolder();
            rowView = activity.getLayoutInflater().inflate(R.layout.item_users, parent, false);
            holder.tvName = (TextView) rowView.findViewById(R.id.tv_item_users_name);
            holder.tvLastActivity = (TextView) rowView.findViewById(R.id.tv_item_users_lastActivity);
            holder.ivAvatar = (ImageView) rowView.findViewById(R.id.iv_item_users_ava);
            rowView.setTag(holder);
        } else {
            holder = (ViewHolder) rowView.getTag();
        }
        Users user = users.get(position);
        holder.tvName.setText(user.getName());
        if (user.getLastOnline() != 0) {
            long lastOnline = users.get(position).getLastOnline();
            if (System.currentTimeMillis() - lastOnline < 20000) {
                holder.tvLastActivity.setText("online");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    holder.tvLastActivity.setTextColor(activity.getResources().getColor(R.color.text_online, null));
                } else {
                    holder.tvLastActivity.setTextColor(activity.getResources().getColor(R.color.text_online));
                }
            } else if (System.currentTimeMillis() - lastOnline < 120000) {
                holder.tvLastActivity.setText("away");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    holder.tvLastActivity.setTextColor(activity.getResources().getColor(R.color.text_away, null));
                } else {
                    holder.tvLastActivity.setTextColor(activity.getResources().getColor(R.color.text_away));
                }
            } else {
                holder.tvLastActivity.setText("offline");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    holder.tvLastActivity.setTextColor(activity.getResources().getColor(R.color.text_offline, null));
                } else {
                    holder.tvLastActivity.setTextColor(activity.getResources().getColor(R.color.text_offline));
                }
            }
        } else holder.tvLastActivity.setText("never");
        ImageLoader.getInstance().displayImage(Avatar.generateFullPathToAva(user.getObjectId()), holder.ivAvatar);
        return rowView;
    }

    static class ViewHolder {
        public TextView tvName, tvLastActivity;
        ImageView ivAvatar;

    }
}