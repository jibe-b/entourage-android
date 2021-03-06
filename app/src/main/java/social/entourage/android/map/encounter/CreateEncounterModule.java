package social.entourage.android.map.encounter;

import dagger.Module;
import dagger.Provides;

/**
 * Module related to ReadEncounterActivity
 * @see CreateEncounterActivity
 */
@Module
final class CreateEncounterModule {
    private final CreateEncounterActivity activity;

    public CreateEncounterModule(final CreateEncounterActivity activity) {
        this.activity = activity;
    }

    @Provides
    public CreateEncounterActivity providesActivity() {
        return activity;
    }
}
