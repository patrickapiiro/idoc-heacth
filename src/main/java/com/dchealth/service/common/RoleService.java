package com.dchealth.service.common;

import com.dchealth.entity.common.*;
import com.dchealth.facade.common.BaseFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/6/22.
 */
@Controller
@Produces("application/json")
@Path("role")
public class RoleService {

    @Autowired
    private BaseFacade baseFacade ;

    /**
     * 获取所有角色
     * @return
     */
    @GET
    @Path("list")
    public List<RoleDict> listRole(){
        String hql = "from RoleDict as rd where rd.status='1'" ;
        return baseFacade.createQuery(RoleDict.class,hql,new ArrayList<Object>()).getResultList();
    }

    /**
     * 获取角色用户
     * @param roleId
     * @return
     */
    @GET
    @Path("list-role-user")
    public List<YunUsers> listRoleUsers(String roleId){
        String hql="select y from YunUsers as y ,RoleVsUser rvu where y.id=rvu.userId and rvu.roleId='"+roleId+"'" ;
        return baseFacade.createQuery(YunUsers.class,hql,new ArrayList<Object>()).getResultList();
    }


    /**
     * 添加、修改、删除角色
     * 删除为逻辑删除
     * status=1正常、status=-1删除
     * @param roleDict
     * @return
     */
    @POST
    @Path("merge")
    @Transactional
    public Response mergeRoleDict(RoleDict roleDict){
        return Response.status(Response.Status.OK).entity(baseFacade.merge(roleDict)).build();
    }

    /**
     * 添加角色菜单
     * @param menuDicts 菜单对象数组
     * @param roleId 角色ID通过拼接在访问路径后面
     * @return
     * @throws Exception
     */
    @POST
    @Path("add-role-menu")
    @Transactional
    public Response roleMenuAdd(List<MenuDict> menuDicts,@QueryParam("roleId") String roleId) throws Exception {
        if(roleId==null||"".equals(roleId)){
            throw new Exception("传入的角色信息为空！");
        }
        String delHql = "delete RoleVsMenus as rm where rm.roleId='"+roleId+"'" ;
        baseFacade.removeByHql(delHql);
        List<RoleVsMenus> roleVsMenusList = new ArrayList<>();
        for (MenuDict md :menuDicts){
            RoleVsMenus roleVsMenus = new RoleVsMenus();
            roleVsMenus.setRoleId(roleId);
            roleVsMenus.setMenuDict(md.getId());
            roleVsMenusList.add(baseFacade.merge(roleVsMenus)) ;
        }

        return Response.status(Response.Status.OK).entity(roleVsMenusList).build();
    }


    /**
     * 添加角色用户
     * @param users
     * @param roleId
     * @return
     * @throws Exception
     */
    @Transactional
    @Path("add-role-user")
    @POST
    public Response addRoleUser(List<YunUsers> users,@QueryParam("roleId") String roleId) throws Exception {
        if(roleId==null||"".equals(roleId)){
            throw new Exception("传入的角色信息为空！");
        }
        String delHql = "delete RoleVsUser as rm where rm.roleId='"+roleId+"'" ;
        baseFacade.removeByHql(delHql);
        List<RoleVsUser> roleVsUserArrayList = new ArrayList<>();
        for (YunUsers user :users){
            RoleVsUser roleVsUser = new RoleVsUser();
            roleVsUser.setRoleId(roleId);
            roleVsUser.setUserId(user.getId());
            roleVsUserArrayList.add(baseFacade.merge(roleVsUser)) ;
        }
        return Response.status(Response.Status.OK).entity(roleVsUserArrayList).build();
    }


}
