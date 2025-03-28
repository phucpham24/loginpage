package vn.login.loginpage.domain;

import java.time.Instant;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import io.swagger.v3.oas.annotations.media.Schema;
import vn.login.loginpage.util.constant.GenderEnum;

@Getter
@Setter
@Table("users")
public class User {

    @Id
    @Schema(hidden = true) // This hides it from the Swagger UI
    private Long id;

    @NotBlank(message = "Name is required")
    @Column("name")
    private String name;

    @NotBlank(message = "Email is required")
    @Column("email")
    @Schema(description = "Username", required = true)
    private String email;

    @NotBlank(message = "Password is required")
    @Column("password")
    @Schema(description = "Password", required = true)
    private String password;

    @Column("age")
    private int age;

    @Column("gender")
    private GenderEnum gender;

    @Column("address")
    private String address;

    @Column("refresh_token")
    private String refreshToken;

    @Column("created_at")
    private Instant createdAt;

    @Column("updated_at")
    private Instant updatedAt;
}
