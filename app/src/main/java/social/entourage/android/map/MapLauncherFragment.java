package social.entourage.android.map;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import social.entourage.android.R;
import social.entourage.android.api.model.TourType;

/**
 * Created by NTE on 13/07/15.
 */
public class MapLauncherFragment extends Fragment {

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    private OnTourStartListener callback;

    @InjectView(R.id.launcher_tour_type_1)
    RadioGroup radioGroupType1;
    @InjectView(R.id.launcher_tour_type_2)
    RadioGroup radioGroupType2;

    // ----------------------------------
    // CONSTRUCTOR
    // ----------------------------------

    public static MapLauncherFragment newInstance() {
        return new MapLauncherFragment();
    }

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View toReturn = inflater.inflate(R.layout.fragment_map_launcher, container, false);
        ButterKnife.inject(this, toReturn);
        return toReturn;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            callback = (OnTourStartListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnTourStartListener");
        }
    }

    // ----------------------------------
    // PUBLIC METHODS
    // ----------------------------------

    @OnClick(R.id.launcher_tour_go)
    public void startNewTour(View view) {
        TourType tourType1 = TourType.findByRessourceId(radioGroupType1.getCheckedRadioButtonId());
        TourType tourType2 = TourType.findByRessourceId(radioGroupType2.getCheckedRadioButtonId());
        callback.onTourStart(tourType1.getName(), tourType2.getName());
    }

    // ----------------------------------
    // INTERFACES
    // ----------------------------------

    public interface OnTourStartListener {
        void onTourStart(String type1, String type2);
    }
}
