package cn.dormitory.auth.dto;

import lombok.Data;

import java.io.Serializable;

/** 登录入参。 */
@Data
public class LoginDTO implements Serializable {
    private String username;
    private String password;
}
