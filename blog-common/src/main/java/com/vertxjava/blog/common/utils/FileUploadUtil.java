package com.vertxjava.blog.common.utils;


import com.qiniu.common.QiniuException;
import com.qiniu.common.Zone;
import com.qiniu.http.Response;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import com.vertxjava.blog.common.entity.FileUploadResult;

import java.util.Calendar;
import java.util.Date;


public class FileUploadUtil {

    private static final String ACCESS_KEY = "GCswT2wH39-Gs-Afp8KM-WxGk6Jb2XPZYF8htYny";
    private static final String SECRET_KEY = "sYPKVtCctUaNbTlNzyyXY05sIDh6mA2vwRYkHyl4";
    private static final String BUCKET_NAME = "blogimg";
    private static final String BASE_PATH = "http://ovfz2ppts.bkt.clouddn.com/";

    private static String getFilePath(String fileName){
        Calendar instance = Calendar.getInstance();
        return new Date().getTime()+"-"+fileName;
    }

    private Auth getAuth(){
        return Auth.create(ACCESS_KEY,SECRET_KEY);
    }

    private static String getUpToken(){
        Auth auth = Auth.create(ACCESS_KEY, SECRET_KEY);
        return auth.uploadToken(BUCKET_NAME);
    }

    public static FileUploadResult uploadPhoto(String realName, String filename){
        FileUploadResult result = new FileUploadResult();
        try {
            Configuration cfg = new Configuration(Zone.zone1());
            String filePath = getFilePath(filename);
            Response response = new UploadManager(cfg).put(realName, filePath, getUpToken());
            if (response.isOK()){
                result.setSuccess(1);
                result.setUrl(BASE_PATH+filePath);
                return result;
            }
        } catch (QiniuException e) {
            result.setSuccess(0);
            result.setMessage(e.getMessage());
            return result;
        }
        return result;
    }

    public int deletePhoto(String[] fileNames){
        int result = 0;
        Configuration cfg = new Configuration(Zone.zone1());
        BucketManager bucketManager = new BucketManager(getAuth(),cfg);
        for (String filename : fileNames) {
            try {
                bucketManager.delete(BUCKET_NAME,filename);
                result++;
            } catch (QiniuException e) {
                e.printStackTrace();
                return 0;
            }
        }
        return result;
    }

}
