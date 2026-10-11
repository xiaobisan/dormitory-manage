package cn.dormitory.auth.dto;

import lombok.Data;

import java.io.Serializable;

/** 注册入参（开放注册，默认 role=STUDENT）。 */
@Data
public class RegisterDTO implements Serializable {
    private String username;
    private String password;
    private String name;
    private String phone;
    /** 可选，默认 STUDENT。 */
    private String role;
}
