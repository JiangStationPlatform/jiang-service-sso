package cn.jiang.station.platform.service.sso.controller;

import cn.jiang.station.platform.common.domain.TbSysUser;
import cn.jiang.station.platform.common.utils.CookieUtils;
import cn.jiang.station.platform.common.utils.MapperUtils;
import cn.jiang.station.platform.service.sso.service.LoginService;
import cn.jiang.station.platform.service.sso.service.consumer.RedisService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;


@Controller
public class LoginController {
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);
    @Autowired
    private LoginService loginService;
    @Autowired
    private RedisService redisService;

    /**
     * 跳转登陆页
     *
     * @return
     */
    @RequestMapping(value = "login", method = RequestMethod.GET)
    public String login(@RequestParam(value = "url", required = false) String url,
                        HttpServletRequest request, Model model) {
        String token = CookieUtils.getCookieValue(request, "token");
        if (StringUtils.isNotBlank(token)) {
            String loginCode = redisService.get(token);
            if (StringUtils.isNotBlank(loginCode)) {
                String json = redisService.get(loginCode);
                try {
                    TbSysUser tbSysUser = MapperUtils.json2pojo(json, TbSysUser.class);
                    if (tbSysUser != null) {
                        logger.info("已登录");
                        if (StringUtils.isNotBlank(url)) {
                            logger.info("重定向到原页面");
                            return "redirect:" + url;
                        }
                    }
                    model.addAttribute("tbSysUser", tbSysUser);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        if (StringUtils.isNotBlank(url)) {
            model.addAttribute("url", url);
        }
        return "login";
    }

    /**
     * 登陆业务
     *
     * @param loginCode
     * @param password
     * @param url
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "login", method = RequestMethod.POST)
    public String login(@RequestParam(value = "loginCode", required = true) String loginCode,
                        @RequestParam(value = "password", required = true) String password,
                        @RequestParam(value = "url", required = false) String url,
                        HttpServletRequest request, HttpServletResponse response, RedirectAttributes redirectAttributes) {
        TbSysUser tbSysUser = loginService.login(loginCode, password);
        if (tbSysUser == null) {
            logger.info("登陆失败");
            redirectAttributes.addFlashAttribute("message", "用户名或密码错误，请重新输入");

        } else {
            logger.info("登陆成功，登记令牌");
            String token = UUID.randomUUID().toString();
            String result = redisService.put(token, loginCode, 60 * 60 * 224);
            if (StringUtils.isNotBlank(result) && "ok".equals(result)) {
                logger.info("redis-token登记成功");
                CookieUtils.setCookie(request, response, "token", token, 60 * 60 * 24);
                if (StringUtils.isNotBlank(url)) {
                    return "redirect:" + url;
                }
            } else {
                logger.info("redis熔断");
                redirectAttributes.addFlashAttribute("message", "服务器异常，请稍后再试");

            }

        }
        return "redirect:/login";
    }

    /**
     * 注销登陆
     *
     * @return
     */
    @RequestMapping(value = "logout", method = RequestMethod.GET)
    public String logout(HttpServletRequest request,
                         HttpServletResponse response,
                         @RequestParam(required = false) String url,
                         Model model) {
        CookieUtils.deleteCookie(request, response, "token");
        return login(url, request, model);
    }

}
