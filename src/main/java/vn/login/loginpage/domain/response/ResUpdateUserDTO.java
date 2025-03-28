package vn.login.loginpage.domain.response;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.login.loginpage.util.constant.GenderEnum;

@Getter
@Setter
public class ResUpdateUserDTO {
    private Long id;

    private String name;
    private GenderEnum gender;
    private String address;

    private int age;
    private Instant updateAt;
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
