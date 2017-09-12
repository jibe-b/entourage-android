package social.entourage.android.carousel.pages;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import social.entourage.android.R;
import social.entourage.android.tools.Utils;

/**
 * Carousel Page 2
 */
public class CarouselPage2Fragment extends Fragment {


    public CarouselPage2Fragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_carousel_page2, container, false);

        TextView textView = (TextView)v.findViewById(R.id.carousel_p2_content);
        if (textView != null) {
            textView.setText( Utils.fromHtml(getString(R.string.carousel_p2_content)) );
        }

        return v;
    }

}
