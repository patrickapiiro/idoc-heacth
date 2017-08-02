package com.dchealth.service.common;

import com.dchealth.VO.Page;
import com.dchealth.VO.YunUserVO;
import com.dchealth.entity.common.RoleDict;
import com.dchealth.entity.common.RoleVsUser;
import com.dchealth.entity.rare.YunDiseaseList;
import com.dchealth.entity.rare.YunUserDisease;
import com.dchealth.entity.rare.YunUserDiseaseManager;
import com.dchealth.entity.common.YunUsers;
import com.dchealth.facade.security.UserFacade;
import com.dchealth.security.PasswordAndSalt;
import com.dchealth.security.SystemPasswordService;
import com.dchealth.util.SmsSendUtil;
import com.dchealth.util.StringUtils;
import com.dchealth.util.UserUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.TypedQuery;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 * Created by Administrator on 2017/6/6.
 */

@Produces("application/json")
@Path("yun-user")
@Controller
public class YunUserService {

    @Autowired
    private UserFacade userFacade ;

    /**
     * 注册用户信息
     * @param yunUsers
     * @return
     */
    @Path("regist")
    @POST
    @Transactional
    public Response registYunUser(YunUsers yunUsers){

        long id = new Date().getTime();
        yunUsers.setId(String.valueOf(id));
        PasswordAndSalt passwordAndSalt = SystemPasswordService.enscriptPassword(yunUsers.getUserId(), yunUsers.getPassword());
        yunUsers.setPassword(passwordAndSalt.getPassword());
        yunUsers.setSalt(passwordAndSalt.getSalt());
        return Response.status(Response.Status.OK).entity(userFacade.merge(yunUsers)).build();
    }


    /**
     * 更新用户
     * @param yunUsers
     * @return
     */
    @POST
    @Transactional
    @Path("update")
    public Response updateYunUser(YunUsers yunUsers) throws Exception {
        String id = yunUsers.getId();
        if(id==null||"".equals(id)){
            System.out.println(id);
            throw new Exception("获取不到原信息的ID");
        }
        YunUsers users = userFacade.merge(yunUsers);
        return Response.status(Response.Status.OK).entity(users).build();
    }

    /**
     * 修改密码
     * @param oldPassword
     * @param newPassowrd
     * @param userId
     * @return
     */
    @POST
    @Transactional
    @Path("change-pwd")
    public Response chagePassword(@QueryParam("oldPassword") String oldPassword, @QueryParam("newPassword") String newPassowrd,
                                  @QueryParam("userId") String userId) throws Exception {
        YunUsers yunUsers = userFacade.getYunUsersByUserId(userId);
        String passwordWithSalt = SystemPasswordService.enscriptPasswordWithSalt(yunUsers.getSalt(), userId, oldPassword);
        String oldDbPassword = yunUsers.getPassword() ;
        if(passwordWithSalt.equals(oldDbPassword)){
            PasswordAndSalt passwordAndSalt = SystemPasswordService.enscriptPassword(userId, newPassowrd);
            yunUsers.setPassword(passwordAndSalt.getPassword());
            yunUsers.setSalt(passwordAndSalt.getSalt());
            Subject subject = SecurityUtils.getSubject();
            subject.logout();
            return Response.status(Response.Status.OK).entity(userFacade.merge(yunUsers)).build();
        }else{
            throw new Exception("原密码错误！");
        }
    }


    /**
     * 重置用户密码
     * @param userId
     * @return
     * @throws Exception
     */
    @POST
    @Transactional
    @Path("rest-pwd")
    public Response restPassword(@QueryParam("userId")String userId) throws Exception {

        YunUsers yunUsers = userFacade.getYunUsersByUserId(userId) ;
        Properties properties = new Properties() ;
        InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream("dchealth.properties");
        properties.load(resourceAsStream);
        String newPassword =properties.getProperty("newPassword");
        if("".equals(newPassword)||null==newPassword){
            newPassword = "123456" ;
        }
        if (yunUsers!=null){
            PasswordAndSalt passwordAndSalt = SystemPasswordService.enscriptPassword(userId, newPassword);
            yunUsers.setPassword(passwordAndSalt.getPassword());
            yunUsers.setSalt(passwordAndSalt.getSalt());
            userFacade.merge(yunUsers);
        }

        return Response.status(Response.Status.OK).entity(yunUsers).build();
    }

    @Produces("text/plain")
    @POST
    @Path("logout")
    public Response logOut(){
        Subject subject = SecurityUtils.getSubject();
        subject.logout();
        return Response.status(Response.Status.OK).entity("success").build();
    }
    /**
     * 获取当前登录用户
     * @return
     * @throws Exception
     */
    @GET
    @Path("current-user")
    public YunUsers getCurrentUser() throws Exception {
        YunUsers yunUsers = UserUtils.getYunUsers();
        return yunUsers;
    }


    /**
     * 获取用户研究疾病、管理疾病信息
     * @param userId
     * @return
     */
    @GET
    @Path("get-user-disease-info")
    public YunUserVO getYunUserDiseaseInfo(@QueryParam("userId") String userId) throws Exception {
        YunUserVO yunUserVO = new YunUserVO() ;
        YunUsers yunUsers = userFacade.getYunUserById(userId) ;
        yunUserVO.setYunUsers(yunUsers);
        String hqlDisease = "select di from YunUserDisease as du,YunDiseaseList di where di.dcode=du.dcode and  du.userId='"+userId+"'";
        List<YunDiseaseList> yunUserDisease = userFacade.createQuery(YunDiseaseList.class,hqlDisease,new ArrayList<Object>()).getResultList();
        yunUserVO.setYunUserDisease(yunUserDisease);
        String diseaseManagerHql = "select di from YunUserDiseaseManager dm,YunDiseaseList di where di.dcode=dm.dcode and dm.userId='"+userId+"'";
        List<YunDiseaseList> yunUserDiseaseManagers = userFacade.createQuery(YunDiseaseList.class,diseaseManagerHql,new ArrayList<Object>()).getResultList();
        yunUserVO.setYunUserDiseaseManager(yunUserDiseaseManagers);
        return yunUserVO;
    }


    /**
     * 修改研究疾病和管理疾病
     * @param yunUserVO
     * @return
     */
    @POST
    @Transactional
    @Path("merge-user-disease-info")
    public Response mergeYunUserDiseaseInfo(YunUserVO yunUserVO){
        YunUsers yunUsers = yunUserVO.getYunUsers() ;
        List<YunDiseaseList> yunUserDiseasese = yunUserVO.getYunUserDisease();
        List<YunDiseaseList> yunUserDiseaseManager = yunUserVO.getYunUserDiseaseManager();
        String hql = "delete from YunUserDisease as yd where yd.userId='"+yunUsers.getId()+"'" ;
        String hql2 = "delete from YunUserDiseaseManager as ym where ym.userId='"+yunUsers.getId()+"'" ;

        userFacade.removeByHql(hql);
        userFacade.removeByHql(hql2);

        for(YunDiseaseList diseaseList:yunUserDiseasese){
            YunUserDisease yunUserDisease = new YunUserDisease() ;
            yunUserDisease.setUserId(yunUsers.getId());
            yunUserDisease.setDcode(diseaseList.getDcode());
            userFacade.merge(yunUserDisease) ;
        }

        for (YunDiseaseList yunDiseaseList:yunUserDiseaseManager){
            YunUserDiseaseManager yunUserDiseaseManager1 = new YunUserDiseaseManager() ;
            yunUserDiseaseManager1.setUserId(yunUsers.getId());
            yunUserDiseaseManager1.setDcode(yunDiseaseList.getDcode());
            userFacade.merge(yunUserDiseaseManager1) ;
        }
        return Response.status(Response.Status.OK).entity(yunUsers).build();
    }


    /**
     * 获取用户列表，肯根据用户状态，用户状态不传递或者传递为空则获取全部用户
     * @param where
     * @return
     */
    @GET
    @Path("user-list")
    public Page<YunUsers> listYunUsersByFlags(@QueryParam("loginFlag") String loginFlag,@QueryParam("userName") String userName,
                                              @QueryParam("userId")String userId,@QueryParam("email")String email,
                                              @QueryParam("rolename")String rolename,@QueryParam("where")String where,@QueryParam("mobile")String mobile,
                                              @QueryParam("perPage")int perPage,@QueryParam("currentPage") int currentPage){
        String hql = "from YunUsers as user where 1=1 " ;
        String hqlCount = "select count(user) from YunUsers as user where 1=1 " ;
        if(!"".equals(where)&&where!=null){
            hql+=(" and "+where);
            hqlCount+=(" and "+where);
        }
        if(!"".equals(loginFlag)&&loginFlag!=null){
            hql+=" and user.loginFlags='"+loginFlag+"'";
            hqlCount+=" and user.loginFlags='"+loginFlag+"'";
        }
        if(!"".equals(mobile)&&mobile!=null){
            hql+=" and user.mobile='"+mobile+"'";
            hqlCount+=" and user.mobile='"+mobile+"'";
        }
        if(!"".equals(userName)&&userName!=null){
            hql+=" and user.userName='"+userName+"'";
            hqlCount+=" and user.userName='"+userName+"'";
        }
        if(!"".equals(userId)&&userId!=null){
            hql+=" and user.userId='"+userId+"'";
            hqlCount+=" and user.userId='"+userId+"'";
        }
        if(!"".equals(email)&&email!=null){
            hql+=" and user.email='"+email+"'";
            hqlCount+=" and user.email='"+email+"'";
        }
        if(!"".equals(rolename)&&rolename!=null){
            hql+=" and user.rolename='"+rolename+"'";
            hqlCount+=" and user.rolename='"+rolename+"'";
        }

        hql+=" order by user.createDate desc";
        hqlCount+=" order by user.createDate desc";
        TypedQuery<YunUsers> query = userFacade.createQuery(YunUsers.class, hql, new ArrayList<Object>());
        Page<YunUsers> yunUsersPage = new Page<>();
        Long counts = userFacade.createQuery(Long.class, hqlCount, new ArrayList<Object>()).getSingleResult();
        yunUsersPage.setCounts(counts);
        if(perPage>0){
            query.setFirstResult(currentPage*perPage) ;
            query.setMaxResults(perPage);
            yunUsersPage.setPerPage((long) perPage);

        }
        List<YunUsers> resultList = query.getResultList();
        yunUsersPage.setData(resultList);

        return yunUsersPage;
    }


    /**
     * 添加用户角色
     * @param roleDicts
     * @param userId
     * @return
     */
    @Transactional
    @POST
    @Path("add-user-role")
    public Response addRoles(List<RoleDict> roleDicts,@QueryParam("userId") String userId) throws Exception {
        YunUsers yunUserById = userFacade.getYunUserById(userId);
        List<RoleVsUser> roleVsUsers = new ArrayList<>() ;
        String hql = "delete from RoleVsUser as r where r.userId='"+userId+"'" ;
        for(RoleDict roleDict:roleDicts){
            RoleVsUser roleVsUser = new RoleVsUser();
            roleVsUser.setUserId(userId);
            roleVsUser.setRoleId(roleDict.getId());
            roleVsUsers.add(userFacade.merge(roleVsUser));
        }
        return Response.status(Response.Status.OK).entity(roleVsUsers).build();
    }

    /**
     * 获取用户
     * @param id
     * @return
     * @throws Exception
     */
    @GET
    @Path("get-user-by-id")
    public YunUsers getYunUser(@QueryParam("userId") String id) throws Exception {
        return userFacade.getYunUserById(id);
    }

    /**
     * 手机短信验证码 获取
     * @param loginName 登录用户名
     * @return
     * @throws Exception
     */
    @GET
    @Path("get-very-code")
    public List getVeryCode(@QueryParam("loginName") String loginName,@Context HttpServletRequest request) throws Exception{
        List<String> list = new ArrayList<>();
        YunUsers yunUsers = userFacade.getYunUsersByLoginName(loginName);
        if(yunUsers.getMobile()==null || "".equals(yunUsers.getMobile())){
            throw new Exception("用户未绑定手机号，请修改个人信息进行手机号码绑定");
        }
        if(!SmsSendUtil.isMobile(yunUsers.getMobile())){
            throw new Exception("用户手机号不正确，请修改手机号");
        }
        String veryCode = SmsSendUtil.sendVeryCode(yunUsers.getMobile());
        list.add(loginName);
        if(request!=null){
            request.getSession().setAttribute(request.getSession().getId(),veryCode);
        }
        return list;
    }

    /**
     * 忘记密码 设置新密码
     * @param userName
     * @param veryCode
     * @param newPassword
     * @param confirmPassword
     * @param request
     * @return
     * @throws Exception
     */
    @POST
    @Path("reset-user-pwd")
    @Transactional
    public Response resetUserPassWord(@QueryParam("loginName") String userName,@QueryParam("veryCode") String veryCode,
                                       @QueryParam("newPassword") String newPassword, @QueryParam("confirmPassword") String confirmPassword,
                                       @Context HttpServletRequest request) throws Exception {
        YunUsers yunUsers = userFacade.getYunUsersByLoginName(userName);
        if(StringUtils.isEmpty(veryCode)){
            throw new Exception("请输入验证码");
        }
        String sessionVeryCode = (String)request.getSession().getAttribute(request.getSession().getId());
        if(!veryCode.equals(sessionVeryCode)){
            throw new Exception("验证码不正确，请重新输入");
        }
        if(StringUtils.isEmpty(newPassword)){
            throw new Exception("输入密码不能为空");
        }
        if(StringUtils.isEmpty(confirmPassword)){
            throw new Exception("确认密码不能为空");
        }
        if(!newPassword.equals(confirmPassword)){
            throw new Exception("输入密码和确认密码不一致，请重新输入");
        }
        PasswordAndSalt passwordAndSalt = SystemPasswordService.enscriptPassword(yunUsers.getUserId(), confirmPassword);
        yunUsers.setPassword(passwordAndSalt.getPassword());
        yunUsers.setSalt(passwordAndSalt.getSalt());
        Subject subject = SecurityUtils.getSubject();
        subject.logout();
        return Response.status(Response.Status.OK).entity(userFacade.merge(yunUsers)).build();
    }
}
