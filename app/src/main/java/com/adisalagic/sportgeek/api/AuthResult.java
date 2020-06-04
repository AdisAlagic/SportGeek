
package com.adisalagic.sportgeek.api;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class AuthResult {

    @SerializedName("role")
    @Expose
    private Integer role;
    @SerializedName("token")
    @Expose
    private String token;
    @SerializedName("error")
    @Expose
    private String error;

    public void setError(String error) {
        this.error = error;
    }

    public String getError() {
        return error;
    }

    public Integer getRole() {
        return role;
    }

    public void setRole(Integer role) {
        this.role = role;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

}
