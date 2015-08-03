package social.entourage.android.guide.poi;

import dagger.Module;
import dagger.Provides;

/**
 * Module related to ReadPoiActivity
 * @see ReadPoiActivity
 */
@Module
final class ReadPoiModule {
    private final ReadPoiActivity activity;

    public ReadPoiModule(final ReadPoiActivity activity) {
        this.activity = activity;
    }

    @Provides
    public ReadPoiPresenter providesMainPresenter() {
        return new ReadPoiPresenter(activity);
    }
}