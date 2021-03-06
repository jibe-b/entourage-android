package social.entourage.android.map.encounter;

/**
 * Presenter controlling the ReadEncounterActivity
 * @see ReadEncounterActivity
 */
public class ReadEncounterPresenter {
    private final ReadEncounterActivity activity;

    public ReadEncounterPresenter(final ReadEncounterActivity activity) {
        this.activity = activity;
    }

    public void displayEncounter() {
        activity.displayEncounter();
    }
}
