package social.entourage.android.api;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.POST;
import retrofit.http.Path;
import social.entourage.android.api.model.EncounterWrapper;

public interface EncounterRequest {

    @POST("/tours/{tour_id}/encounters.json")
    void create( @Path("tour_id") long tourId, @Body EncounterWrapper encounterWrapper, Callback<EncounterResponse> callback);
}