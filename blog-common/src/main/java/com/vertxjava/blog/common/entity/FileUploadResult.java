package com.vertxjava.blog.common.entity;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

/**
 * @author jack
 * @create 2017-09-06 14:20
 **/
@DataObject(generateConverter = true)
public class FileUploadResult {

    private int success;    //成功标准 0失败 1成功
    private String url;     //图片url
    private String message; //错误信息

    public FileUploadResult() {
    }

    public FileUploadResult(int success, String url, String message) {
        this.success = success;
        this.url = url;
        this.message = message;
    }

    public FileUploadResult(JsonObject json) {
        FileUploadResultConverter.fromJson(json, this);
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        FileUploadResultConverter.toJson(this, json);
        return json;
    }

    public int getSuccess() {
        return success;
    }

    public void setSuccess(int success) {
        this.success = success;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return this.toJson().encodePrettily();
    }

}


