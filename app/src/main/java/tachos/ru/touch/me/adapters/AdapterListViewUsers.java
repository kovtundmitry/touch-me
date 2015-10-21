package tachos.ru.touch.me.adapters;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

import tachos.ru.touch.me.MainActivity;
import tachos.ru.touch.me.R;
import tachos.ru.touch.me.data.Avatar;
import tachos.ru.touch.me.data.DataManager;
import tachos.ru.touch.me.data.Users;

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
            holder.rbLiked = (CheckBox) rowView.findViewById(R.id.cb_item_users_liked);
            rowView.setTag(holder);
        } else {
            holder = (ViewHolder) rowView.getTag();
        }
        final Users user = users.get(position);
        holder.tvName.setText(user.getName());
        holder.rbLiked.setOnCheckedChangeListener(null);
        holder.rbLiked.setChecked(DataManager.getLikedUsersIds().contains(user.getObjectId()));
        holder.rbLiked.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (activity == null) return;
                ((MainActivity) activity).startLoading();
                if (isChecked) {
                    DataManager.likeUser(user, new AsyncCallback<Users>() {
                        @Override
                        public void handleResponse(Users response) {
                            if (activity == null) return;
                            ((MainActivity) activity).stopLoading();
                        }

                        @Override
                        public void handleFault(BackendlessFault fault) {
                            if (activity == null) return;
                            ((MainActivity) activity).stopLoading();
                        }
                    });
                } else {
                    DataManager.unLikeUser(user, new AsyncCallback<Users>() {
                        @Override
                        public void handleResponse(Users response) {
                            if (activity == null) return;
                            ((MainActivity) activity).stopLoading();
                        }

                        @Override
                        public void handleFault(BackendlessFault fault) {
                            if (activity == null) return;
                            ((MainActivity) activity).stopLoading();
                        }
                    });
                }
            }
        });
        if (user.getLastOnline() != 0) {
            long lastOnline = users.get(position).getLastOnline();
            if (System.currentTimeMillis() - lastOnline < 20000) {
                holder.tvLastActivity.setText("online");
                holder.tvLastActivity.setTextColor(activity.getResources().getColor(R.color.text_online));
            } else if (System.currentTimeMillis() - lastOnline < 120000) {
                holder.tvLastActivity.setText("away");
                holder.tvLastActivity.setTextColor(activity.getResources().getColor(R.color.text_away));
            } else {
                holder.tvLastActivity.setText("offline");
                holder.tvLastActivity.setTextColor(activity.getResources().getColor(R.color.text_offline));
            }
        } else holder.tvLastActivity.setText("never");
        ImageLoader.getInstance().displayImage(Avatar.generateFullPathToAva(user.getObjectId()), holder.ivAvatar);
        return rowView;
    }

    static class ViewHolder {
        public TextView tvName, tvLastActivity;
        ImageView ivAvatar;
        CheckBox rbLiked;
    }
}