package com.wayn.mall.controller.mall;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wayn.mall.base.BaseController;
import com.wayn.mall.constant.Constants;
import com.wayn.mall.controller.vo.MallUserVO;
import com.wayn.mall.entity.MallUser;
import com.wayn.mall.service.MallUserService;
import com.wayn.mall.util.R;
import com.wayn.mall.util.security.Md5Utils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;

@Controller
public class MallUserController extends BaseController {

    @Autowired
    private MallUserService mallUserService;

    @GetMapping("/personal")
    public String personalPage(HttpServletRequest request) {
        request.setAttribute("path", "personal");
        return "mall/personal";
    }

    @GetMapping("/logout")
    public String logout(HttpSession httpSession) {
        httpSession.removeAttribute(Constants.MALL_USER_SESSION_KEY);
        return "mall/login";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "mall/login";
    }

    @ResponseBody
    @PostMapping("/login")
    public R doLogin(MallUserVO mallUserVO,
                     @RequestParam("verifyCode") String verifyCode,
                     HttpSession session) {
        String kaptchaCode = (String) session.getAttribute(Constants.MALL_VERIFY_CODE_KEY);
        if (!StringUtils.equalsIgnoreCase(verifyCode, kaptchaCode)) {
            return R.error("验证码错误");
        }
        MallUser user = mallUserService.getOne(new QueryWrapper<MallUser>()
                .eq("login_name", mallUserVO.getLoginName())
                .eq("password_md5", Md5Utils.hash(mallUserVO.getPassword())));
        if (user == null) {
            return R.error("账户名称或者密码错误");
        }
        if (user.getLockedFlag() == 1) {
            return R.error("该账户已被禁用");
        }
        BeanUtils.copyProperties(user, mallUserVO);
        session.setAttribute(Constants.MALL_USER_SESSION_KEY, mallUserVO);
        return R.success();
    }

    @GetMapping("/register")
    public String registerPage() {
        return "mall/register";
    }

    @ResponseBody
    @PostMapping("/register")
    public R register(@RequestParam("loginName") String loginName,
                      @RequestParam("verifyCode") String verifyCode,
                      @RequestParam("password") String password,
                      HttpSession session) {
        String kaptchaCode = (String) session.getAttribute(Constants.MALL_VERIFY_CODE_KEY);
        if (!StringUtils.equalsIgnoreCase(verifyCode, kaptchaCode)) {
            return R.error("验证码错误");
        }
        List<MallUser> list = mallUserService.list(new QueryWrapper<MallUser>()
                .eq("login_name", loginName));
        if (CollectionUtils.isNotEmpty(list) && list.size() > 0) {
            return R.error("该账户名已存在");
        }
        MallUser mallUser = new MallUser();
        mallUser.setLoginName(loginName);
        mallUser.setPasswordMd5(Md5Utils.hash(password));
        mallUserService.save(mallUser);
        return R.success();
    }


    @PostMapping("/personal/updateInfo")
    @ResponseBody
    public R updateInfo(@RequestBody MallUser mallUser) {
        mallUserService.updateById(mallUser);
        MallUser user = mallUserService.getById(mallUser.getUserId());
        MallUserVO mallUserVO = new MallUserVO();
        BeanUtils.copyProperties(user, mallUserVO);
        session.setAttribute(Constants.MALL_USER_SESSION_KEY, mallUserVO);
        return R.success();
    }
}
