package knight.rider.kitt.adapter.holder;

import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.LayoutRes;

public class ExpandableViewHolder {

    private final View mItemView;
    private final SparseArray<View> mViews;

    // 初始化
    public ExpandableViewHolder(Context context, @LayoutRes int layout_id) {
        mViews = new SparseArray<>();
        mItemView = LayoutInflater.from(context).inflate(layout_id, null);
        mItemView.setTag(this);
    }

    // 获取视图
    public final View getItemView() {
        return mItemView;
    }


    /**
     * 获取视图
     */
    public final <T extends View> T getView(int resId) {
        View mView = mViews.get(resId);
        if (mView == null) {
            mView = mItemView.findViewById(resId);
            mViews.put(resId, mView);
        }
        return (T) mView;
    }


    public final ImageView getImageView(int id) {
        return getView(id);
    }

    public final TextView getTextView(int id) {
        return getView(id);
    }

    public final EditText getEditText(int id) {
        return getView(id);
    }

    public final Button getButton(int id) {
        return getView(id);
    }

    public final ImageButton getImageButton(int id) {
        return getView(id);
    }

    public final CheckBox getCheckBox(int id) {
        return getView(id);
    }

    public final ProgressBar getProgressBar(int id) {
        return getView(id);
    }

    public final LinearLayout getLinearLayout(int id) {
        return getView(id);
    }

    public final RelativeLayout getRelativeLayout(int id) {
        return getView(id);
    }

    public final FrameLayout getFrameLayout(int id) {
        return getView(id);
    }

    public final Switch getSwitch(int id) {
        return getView(id);
    }

    public final ToggleButton getToggleButton(int id) {
        return getView(id);
    }
}