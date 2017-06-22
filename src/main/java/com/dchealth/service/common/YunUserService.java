package com.dchealth.service.common;

import com.dchealth.VO.YunUserVO;
import com.dchealth.entity.rare.YunDiseaseList;
import com.dchealth.entity.rare.YunUserDisease;
import com.dchealth.entity.rare.YunUserDiseaseManager;
import com.dchealth.entity.common.YunUsers;
import com.dchealth.facade.security.UserFacade;
import com.dchealth.security.PasswordAndSalt;
import com.dchealth.security.SystemPasswordService;
import com.dchealth.util.UserUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Administrator on 2017/6/6.
 */
@Controller
@Produces("application/json")
@Path("yun-user")
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
        if (yunUsers!=null){
            PasswordAndSalt passwordAndSalt = SystemPasswordService.enscriptPassword(userId, "123456");
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
     * @param loginFlag
     * @return
     */
    @GET
    @Path("user-list")
    public List<YunUsers> listYunUsersByFlags(@QueryParam("loginFlag") String loginFlag){
        String hql = "from YunUsers as user where 1=1 " ;
        if(!"".equals(loginFlag)&&loginFlag!=null){
            hql+=" and user.loginFlags='"+loginFlag+"'";
        }
        List<YunUsers> resultList = userFacade.createQuery(YunUsers.class, hql, new ArrayList<Object>()).getResultList();
        return resultList;
    }

}
