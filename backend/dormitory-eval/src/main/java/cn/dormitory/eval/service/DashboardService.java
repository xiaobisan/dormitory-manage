package cn.dormitory.eval.service;

import cn.dormitory.eval.dto.DashboardVO;

/** 数据看板统计（/api/eval/dashboard）。按角色返回不同数据范围。 */
public interface DashboardService {

    /** 看板总览：入住率/收缴率/报修时效/违纪趋势/卫生分 Top。 */
    DashboardVO overview();
}
