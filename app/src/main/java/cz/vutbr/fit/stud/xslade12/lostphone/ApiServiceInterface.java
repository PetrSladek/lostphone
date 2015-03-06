package cz.vutbr.fit.stud.xslade12.lostphone;

import java.util.Date;

import cz.vutbr.fit.stud.xslade12.lostphone.messages.Message;
import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.Part;
import retrofit.http.PartMap;
import retrofit.http.Path;
import retrofit.mime.MultipartTypedOutput;
import retrofit.mime.TypedFile;
import retrofit.mime.TypedString;

/**
 * Created by Peggy on 4.3.2015.
 */
public interface ApiServiceInterface {

    public static final String ENDPOINT = "http://local2.sdhklepacov.cz/api/";

    @POST("/messages/")
    void createMessage(@Body Message message, Callback<String> cb);

    @Multipart
    @POST("/messages/")
    void createWrongPassMessage(@Part("type") TypedString type, @Part("date") TypedString date, @Part("frontPhoto") TypedFile frontPhoto, Callback<String> cb);


    @FormUrlEncoded
    @POST("/ack/{id}")
    void ackCommand(@Path("id") int id, @Field("date") Date date, Callback<String> cb);
}
