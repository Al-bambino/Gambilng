package model;

import java.io.Serializable;

public class Response implements Serializable
{
    private Status status;
    private String data;

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
