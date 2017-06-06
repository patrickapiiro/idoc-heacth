package com.dchealth.security;

import com.dchealth.entity.YunUsers;
import com.dchealth.facade.security.UserFacade;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.credential.SimpleCredentialsMatcher;
import org.apache.shiro.crypto.hash.Md5Hash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * shiro密码验证类
 * 重写doCredentialsMatch方法，返回true验证通过
 * 返回false 验证不通过
 * Created by heren on 2015/10/22.
 */
@Service
public class HisCredentialsMatcher extends SimpleCredentialsMatcher {


    @Autowired
    private UserFacade userFacade ;

    @Override
    public boolean doCredentialsMatch(AuthenticationToken token, AuthenticationInfo info) {
        UsernamePasswordToken usernamePasswordToken = (UsernamePasswordToken)token ;
        String username = usernamePasswordToken.getUsername() ;
        char[] password1 = usernamePasswordToken.getPassword();
        String password = password1.toString();
        try {
            YunUsers yunUsers = userFacade.getYunUsersByUserName(username);
            String dbPass = yunUsers.getPassword() ;
            String newPass = SystemPasswordService.enscriptPasswordWithSalt(info.getCredentials().toString(),yunUsers.getSalt());
            return dbPass.equals(newPass);
        } catch (Exception e) {
            return false ;
        }
    }

}
