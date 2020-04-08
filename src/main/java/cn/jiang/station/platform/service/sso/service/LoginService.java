package cn.jiang.station.platform.service.sso.service;

import cn.jiang.station.platform.common.domain.TbSysUser;

public interface LoginService {
    /**
     * 登陆
     * @param loginCode
     * @param plantPassword
     * @return
     */
    public TbSysUser login(String loginCode,String plantPassword);
}
