import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface HuggingFaceApi {

    @Headers("Authorization: Bearer YOUR_HUGGING_FACE_API_KEY")
    @POST("https://api-inference.huggingface.co/models/your-model-name")
    Call<HuggingFaceResponse> analyzeDream(@Body String input);
}
