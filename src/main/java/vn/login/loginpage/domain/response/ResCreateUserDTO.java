package vn.login.loginpage.domain.response;

import java.time.Instant;

import lombok.Getter;
import lombok.Setter;
import vn.login.loginpage.util.constant.GenderEnum;

@Getter
@Setter
public class ResCreateUserDTO {
    private Long id;

    private String name;
    private String email;
    private GenderEnum gender;
    private String address;

    private int age;
    private Instant createdAt;
}
