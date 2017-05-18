package social.entourage.android.newsfeed;

import android.view.View;
import android.widget.TextView;

import org.w3c.dom.Text;

import social.entourage.android.R;
import social.entourage.android.api.tape.Events;
import social.entourage.android.base.BottomViewHolder;
import social.entourage.android.tools.BusProvider;

/**
 * Created by mihaiionescu on 10/05/2017.
 */

public class NewsfeedBottomViewHolder extends BottomViewHolder {

    public static final int CONTENT_TYPE_LOAD_MORE = 0;
    public static final int CONTENT_TYPE_NO_ITEMS = 1;

    private View loadMoreView;
    private TextView loadMoreTextView;
    private TextView noItemsTextView;

    public NewsfeedBottomViewHolder(final View view) {
        super(view);
    }

    @Override
    protected void bindFields() {
        content = itemView.findViewById(R.id.newsfeed_bottom_content);
        loadMoreView = itemView.findViewById(R.id.newsfeed_load_more_layout);
        noItemsTextView = (TextView) itemView.findViewById(R.id.newsfeed_no_items);
        loadMoreTextView = (TextView) itemView.findViewById(R.id.newsfeed_load_more);
        loadMoreTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                BusProvider.getInstance().post(new Events.OnNewsfeedLoadMoreEvent());
            }
        });
    }

    @Override
    public void populate(final boolean showContent, final int contentType) {
        super.populate(showContent, contentType);
        if (showContent) {
            // switch between content types
            switch (contentType) {
                case CONTENT_TYPE_LOAD_MORE:
                    loadMoreView.setVisibility(View.VISIBLE);
                    noItemsTextView.setVisibility(View.GONE);
                    break;
                case CONTENT_TYPE_NO_ITEMS:
                    loadMoreView.setVisibility(View.GONE);
                    noItemsTextView.setVisibility(View.VISIBLE);
                    break;
                default:
                    break;
            }
        }
    }

    public static int getLayoutResource() {
        return R.layout.layout_newsfeed_bottom_card;
    }
}
