package vn.login.loginpage.domain.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResResponse<T> {
    private int statusCode;
    private String error;

    // message maybe string or arrayList
    private Object message;
    private T data;

}
