package com.example.julisarrelli.findmycar;

/**
 * Created by julisarrelli on 11/11/16.
 */
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.http.GET;

/**
 * Created by Angie on 16/10/2016.
 */
public interface QuestionService {

    @GET("/questions")
    Call<List<Question>> getQuestions();


}