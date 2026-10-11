package cn.dormitory.auth.dto;

import lombok.Data;

import java.io.Serializable;

/** 登录出参：JWT token + 用户基础信息。 */
@Data
public class LoginVO implements Serializable {
    private String token;
    private Long userId;
    private String name;
    private String role;
    private Long buildingId;
    private Long deptId;
}
