package vn.login.loginpage.domain.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReqLoginDTO {
    @NotBlank(message = "username can not be empty")
    private String username;
    @NotBlank(message = "password can not be empty")
    private String password;
}
