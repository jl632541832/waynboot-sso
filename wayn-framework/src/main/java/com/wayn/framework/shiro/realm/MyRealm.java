package com.wayn.framework.shiro.realm;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wayn.common.domain.User;
import com.wayn.common.exception.BusinessException;
import com.wayn.common.service.RoleMenuService;
import com.wayn.common.service.UserRoleService;
import com.wayn.common.service.UserService;
import com.wayn.common.util.shiro.util.ShiroUtil;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.mgt.eis.SessionDAO;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.support.DefaultSubjectContext;
import org.apache.shiro.util.ByteSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.Collection;
import java.util.Set;

public class MyRealm extends AuthorizingRealm {


    @Value("${wayn.singeUserAuth}")
    private boolean singeUserAuth;

    @Value("${wayn.singeKickoutBefore}")
    private boolean singeKickoutBefore;

    /**
     * 用户服务
     */
    @Autowired
    private UserService userService;
    /**
     * 用户角色服务
     */
    @Autowired
    private UserRoleService userRoleService;
    /**
     * 角色菜单服务
     */
    @Autowired
    private RoleMenuService roleMenuService;

    @Autowired
    private SessionDAO sessionDAO;

    /**
     * 密码加密 测试
     *
     * @param args
     */
    public static void main(String[] args) {
        // MD5,"原密码","盐",加密次数
        String pwd = new SimpleHash("MD5", "123456", "admin", 1024).toString();
        System.out.println(pwd);
    }

    /**
     * 授权
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        User sysUser = (User) principals.getPrimaryPrincipal();
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        Set<String> roles = userRoleService.findRolesByUid(sysUser.getId());
        Set<String> permissions = roleMenuService.findMenusByUid(sysUser.getId());
        info.setRoles(roles);
        info.setStringPermissions(permissions);
        return info;
    }

    /**
     * 认证
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        UsernamePasswordToken token2 = (UsernamePasswordToken) token;
        String username = token2.getUsername();
        String password = ShiroUtil.md5encrypt(new String(token2.getPassword()), username);
        User sysUser = userService.getOne(new QueryWrapper<User>().eq("userName", username));
        if (sysUser == null) {
            throw new UnknownAccountException("用户不存在");
        }
        if (sysUser.getUserStatus() == -1) {
            throw new UnknownAccountException("用户已被禁用");
        }
        // 是否进行单一用户登陆处理
        if (singeUserAuth) {
            Collection<Session> activeSessions = sessionDAO.getActiveSessions();
            for (Session activeSession : activeSessions) {
                if (activeSession.getAttribute(DefaultSubjectContext.PRINCIPALS_SESSION_KEY) == null) {
                    continue;
                } else {
                    SimplePrincipalCollection principalCollection = (SimplePrincipalCollection) activeSession
                            .getAttribute(DefaultSubjectContext.PRINCIPALS_SESSION_KEY);
                    Object primaryPrincipal = principalCollection.getPrimaryPrincipal();
                    User user = (User) primaryPrincipal;
                    if (user.getUserName().equals(username) && singeKickoutBefore) {
                        if (singeKickoutBefore) {
                            Session session = sessionDAO.readSession(activeSession.getId());
                            if (session != null) {
                                session.stop();
                                sessionDAO.delete(session);
//                                simpMessagingTemplate.convertAndSendToUser(user.getId(), "/queue/getResponse", "新消息：" + "该账号已在其他机器登陆！");
                            }
                        } else {
                            throw new BusinessException("该用户已登陆，请先登出！");
                        }
                    }
                }
            }
        }
        //盐值加密
        ByteSource byteSource = ByteSource.Util.bytes(username);
        SimpleAuthenticationInfo info = new SimpleAuthenticationInfo(sysUser, sysUser.getPassword(), byteSource, getName());
        return info;
    }

    public void clearCachedAuthorizationInfo() {
        doClearCache(SecurityUtils.getSubject().getPrincipals());
        clearCachedAuthorizationInfo(SecurityUtils.getSubject().getPrincipals());
    }

}
