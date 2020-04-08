package cn.jiang.station.platform.service.sso.service.impl;

import cn.jiang.station.platform.common.domain.TbSysUser;
import cn.jiang.station.platform.common.utils.MapperUtils;
import cn.jiang.station.platform.service.sso.mapper.TbSysUserMapper;
import cn.jiang.station.platform.service.sso.service.LoginService;
import cn.jiang.station.platform.service.sso.service.consumer.RedisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import tk.mybatis.mapper.entity.Example;

@Service
public class LoginServiceImpl implements LoginService {
    private static final Logger logger = LoggerFactory.getLogger(LoginServiceImpl.class);
    @Autowired
    private TbSysUserMapper tbSysUserMapper;

    @Autowired
    private RedisService redisService;

    @Override
    public TbSysUser login(String loginCode, String plantPassword) {
        TbSysUser tbSysUser = null;
        logger.info("开始查询redis");
        String json = redisService.get(loginCode);
        if (json == null) {
            logger.info("开始查询数据库");
            Example example = new Example(TbSysUser.class);
            example.createCriteria().andEqualTo("loginCode", loginCode);
            tbSysUser = tbSysUserMapper.selectOneByExample(example);
            if (tbSysUser == null) {
                return null;
            }
            logger.info("开始验证密码");
            String password = DigestUtils.md5DigestAsHex(plantPassword.getBytes());
            if (password.equals(tbSysUser.getPassword())) {
                logger.info("开始写入缓存");
                try {
                    String put = redisService.put(loginCode, MapperUtils.obj2json(tbSysUser), 60 * 60 * 24);
                    logger.info("缓存写入状态：" + put);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return tbSysUser;
            } else {
                return null;
            }
        } else {
            try {
                tbSysUser = MapperUtils.json2pojo(json, TbSysUser.class);
            } catch (Exception e) {
                logger.warn("触发熔断:{}", e.getMessage());
            }
        }
        return tbSysUser;
    }
}
