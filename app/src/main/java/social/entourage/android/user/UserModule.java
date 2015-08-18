package social.entourage.android.user;

import dagger.Module;
import dagger.Provides;

/**
 * Module related to UserActivity
 * @see UserActivity
 */
@Module
final class UserModule {

    private final UserActivity activity;

    public UserModule(final UserActivity activity) {
        this.activity = activity;
    }

    @Provides
    public UserActivity providesActivity() {
        return activity;
    }
}
