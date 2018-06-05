package uca.ruiz.antonio.jwtapp;

/**
 * Created by toni on 04/06/2018.
 */

import java.io.Serializable;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class TokenResponse implements Serializable
{

    @SerializedName("token")
    @Expose
    private String token;
    private final static long serialVersionUID = 8022889269386527992L;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

}
