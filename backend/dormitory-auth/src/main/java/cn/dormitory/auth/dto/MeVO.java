package cn.dormitory.auth.dto;

import lombok.Data;

import java.io.Serializable;

/** 当前登录用户信息（/api/auth/me 出参）。 */
@Data
public class MeVO implements Serializable {
    private Long userId;
    private String name;
    private String role;
    private Long buildingId;
    private Long deptId;
}
