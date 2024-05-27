package fr.vocaltech.location.services.retrofit;

import java.util.List;

import fr.vocaltech.location.models.Position;
import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface RetrofitInterface {
    @GET("/positions/{userid}")
    Call<List<Position>> positionsByUserId(@Path("userid") String userid);

    @DELETE("/positions/{userid}")
    Call<Void> deletePositionsByUserId(@Path("userid") String userid);
}
