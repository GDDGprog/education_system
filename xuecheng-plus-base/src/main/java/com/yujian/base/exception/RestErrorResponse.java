package com.yujian.base.exception;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * 错误响应参数包装
 */
@Data
@AllArgsConstructor
public class RestErrorResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private String errMessage;

    private String errCode;

    public RestErrorResponse(String errMessage) {
        this.errMessage = errMessage;
    }
}
