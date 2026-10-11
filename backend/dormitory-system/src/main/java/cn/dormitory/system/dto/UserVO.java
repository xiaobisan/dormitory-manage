package cn.dormitory.system.dto;

import cn.dormitory.common.entity.SysUser;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 用户出参。继承实体字段并隐藏密码。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UserVO extends SysUser {

    /** 密码不回传给前端。 */
    private String password = null;

    public static UserVO from(SysUser user) {
        if (user == null) {
            return null;
        }
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setCreatedBy(user.getCreatedBy());
        vo.setCreatedAt(user.getCreatedAt());
        vo.setUpdatedAt(user.getUpdatedAt());
        vo.setRemark(user.getRemark());
        vo.setUsername(user.getUsername());
        vo.setName(user.getName());
        vo.setRole(user.getRole());
        vo.setPhone(user.getPhone());
        vo.setDeptId(user.getDeptId());
        vo.setBuildingId(user.getBuildingId());
        vo.setDataScopeType(user.getDataScopeType());
        vo.setDataScopeId(user.getDataScopeId());
        vo.setStatus(user.getStatus());
        return vo;
    }
}
