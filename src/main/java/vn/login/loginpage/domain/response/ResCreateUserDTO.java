package vn.login.loginpage.domain.response;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
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
    private RoleUser role;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RoleUser {
        private long id;
        private String name;
    }
}
