package com.didi.carrera.console.web;


import com.alibaba.fastjson.annotation.JSONField;


public class ConsoleBaseResponse<T> {
    /**
     * 状态
     */
    public enum Status {

        SUCCESS(0),

        INVALID_PARAM(400),

        INTERNAL_ERROR(500);

        private int code;

        Status(int code){
            this.code = code;
        }

		public int getCode() {
			return code;
		}
    }

    protected int errno;
    protected String errmsg;

	private T data = null;

    public static <T> ConsoleBaseResponse<T> success() {
        return success(null);
    }

    public static <T> ConsoleBaseResponse<T> success(T data) {
        ConsoleBaseResponse<T> response = new ConsoleBaseResponse<>();
        response.setErrno(Status.SUCCESS.code);
        response.setErrmsg("SUCCESS");
        response.setData(data);
        return response;
    }

    public static <T> ConsoleBaseResponse<T> error(Status status) {
        return error(status, "");
    }

    public static <T> ConsoleBaseResponse<T> error(Status status, String message) {
        ConsoleBaseResponse<T> response = new ConsoleBaseResponse<>();
        response.setErrno(status.code);
        response.setErrmsg(message);
        return response;
    }

    public ConsoleBaseResponse() {
    }

    @JSONField(serialize = false)
    public boolean isSuccess() {
        return this.errno == Status.SUCCESS.getCode();
    }

    public void setStatus(Status status) {
        if(status != null){
            this.errno = status.code;
        }
    }

    public void setErrno(int errno) {
        this.errno = errno;
    }

    public void setErrmsg(String errmsg) {
        this.errmsg = errmsg;
    }

    public int getErrno() {
        return errno;
    }

    public String getErrmsg() {
        return errmsg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "ConsoleBaseResponse [errno=" + errno + ", errmsg=" + errmsg + ", data=" + data + "]";
    }
}