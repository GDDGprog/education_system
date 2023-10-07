package com.yujian.base.exception;

import lombok.Data;

/**
 * 学成在线项目异常类
 */
@Data
public class XueChengPlusException extends RuntimeException {
    private String errMessage;

    private String errCode;

    public XueChengPlusException() {
    }

    public XueChengPlusException(String message) {
        super(message);
        this.errMessage = message;
    }

    public XueChengPlusException(String message,String errCode) {
        this.errMessage = message;
        this.errCode = errCode;
    }

    public static void cast(String errMessage){
        throw new XueChengPlusException(errMessage);
    }

    public static void cast(String errMessage,String errCode){
        throw new XueChengPlusException(errMessage,errCode);
    }

    public static void cast(CommonError error){
        throw new RuntimeException(error.getErrMessage());
    }
}
