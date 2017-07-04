package com.dchealth.service.rare;

import com.dchealth.VO.YunGroupVo;
import com.dchealth.entity.common.YunUsers;
import com.dchealth.entity.rare.YunDept;
import com.dchealth.entity.rare.YunOrganNumber;
import com.dchealth.entity.rare.YunOrganization;
import com.dchealth.facade.common.BaseFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Administrator on 2017/7/4.
 */
@Controller
@Produces("application/json")
@Path("group")
public class GroupService {

    @Autowired
    private BaseFacade baseFacade;

    /**
     * 获取所有的群组信息
     * @return
     */
    @GET
    @Path("yun-organization-list")
    public List<YunOrganization> getYunOrganizationList(){
        List<YunOrganization> yunOrganizationList = baseFacade.findAll(YunOrganization.class);
        return yunOrganizationList;
    }

    /**
     * 根据群组Id查询其下的成员列表
     * @param id
     * @return
     */
    @GET
    @Path("yun-organ-number-list")
    public List<YunUsers> getYunOrganUserList(@QueryParam("id")String id){
        String hql = "select yu from YunOrganNumber as y,YunUsers as yu where y.userId = yu.id ";
        if(id!=null && !"".equals(id)){
            hql += " and y.id = " + id;
        }
        List<YunUsers> yunUsersList = baseFacade.createQuery(YunUsers.class,hql, new ArrayList<Object>()).getResultList();
        return yunUsersList;
    }

    /**
     * 创建新的群组
     * @param yunOrganization
     * @return
     */
    @POST
    @Path("add-new-organ")
    @Transactional
    public Response mergeYunOrganization(YunOrganization yunOrganization){
        try {
            YunOrganization merge =  baseFacade.merge(yunOrganization);
            Response build = Response.status(Response.Status.OK).entity(merge).build();
            return build;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * 根据群组id删除群组信息
     * @param id
     * @return
     */
    @POST
    @Transactional
    @Path("del-organ")
    public Response delYunOrganization(@QueryParam("id") String id){
        //删除群组下的成员信息
        //删除群组信息
        List<String> ids = new ArrayList<>();
        ids.add(id);
        baseFacade.removeByStringIds(YunOrganNumber.class,ids);
        baseFacade.removeByStringIds(YunOrganization.class,ids);
        return Response.status(Response.Status.OK).entity(ids).build();
    }

    /**
     * 根据传入的yunGroupVo对象生成群组成员关系表
     * @param yunGroupVo
     * @return
     */
    @POST
    @Transactional
    @Path("add-organ-number")
    public Response mergeYunOrganNumber(YunGroupVo yunGroupVo){
        YunOrganNumber yunOrganNumber = new YunOrganNumber();
        yunOrganNumber.setId(Long.valueOf(yunGroupVo.getGroupId()));
        yunOrganNumber.setUserId(yunGroupVo.getUserId());
        yunOrganNumber.setModify_date(new Timestamp(new Date().getTime()));
        YunOrganNumber merge =  baseFacade.merge(yunOrganNumber);
        return Response.status(Response.Status.OK).entity(merge).build();
    }

    /**
     * 根据传入的群组Id和用户Id删除其关联关系
     * @param groupId
     * @param userId
     * @return
     */
    @POST
    @Transactional
    @Path("del-organ-number")
    public Response delYunOrganizationNumber(@QueryParam("groupId") String groupId,@QueryParam("userId") String userId){
        String hql = " from YunOrganNumber as y where 1=1 ";
        if(groupId!=null && !"".equals(groupId)){
            hql += " and y.id = "+groupId;
        }
        if(userId!=null && !"".equals(userId)){
            hql += " and y.userId = '"+userId+"'";
        }
        YunOrganNumber yunOrganNumber = baseFacade.createQuery(YunOrganNumber.class,hql,new ArrayList<Object>()).getSingleResult();
        baseFacade.remove(yunOrganNumber);
        return Response.status(Response.Status.OK).entity(yunOrganNumber).build();
    }

    /**
     * 获取所有的科室信息
     * @return
     */
    @GET
    @Path("yun-dept-list")
    public List<YunDept> getYunDeptList(){
        List<YunDept> yunDeptList = baseFacade.findAll(YunDept.class);
        return yunDeptList;
    }

    /**
     * 根据科室Id查询其下的成员列表
     * @param deptId
     * @return
     */
    @GET
    @Path("yun-dept-user-list")
    public List<YunUsers> getYunDeptUserList(@QueryParam("deptId") String deptId){
        String hql = " from YunUsers as yu where 1=1 ";
        if(deptId!=null && !"".equals(deptId)){
            hql += " and yu.deptId = " + deptId;
        }
        List<YunUsers> yunUsersList = baseFacade.createQuery(YunUsers.class,hql, new ArrayList<Object>()).getResultList();
        return yunUsersList;
    }

    /**
     * 创建新的科室
     * @param yunDept
     * @return
     */
    @POST
    @Path("add-new-dept")
    @Transactional
    public Response mergeYunDept(YunDept yunDept){
        try {
            YunDept merge =  baseFacade.merge(yunDept);
            Response build = Response.status(Response.Status.OK).entity(merge).build();
            return build;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
    /**
     * 根据科室id删除科室信息
     * @param deptId
     * @return
     */
    @POST
    @Transactional
    @Path("del-dept")
    public Response delYunDept(@QueryParam("deptId") String deptId){
        //成员的科室Id置空
        //删除科室信息
        List<String> ids = new ArrayList<>();
        ids.add(deptId);
        String hql = " update YunUsers user set user.deptId = 0 where user.deptId = " + deptId;
        baseFacade.createNativeQuery(hql).executeUpdate();
        baseFacade.removeByStringIds(YunDept.class,ids);
        return Response.status(Response.Status.OK).entity(ids).build();
    }
    /**
     * 根据传入的yunGroupVo对象生成科室成员关系
     * @param yunGroupVo
     * @return
     */
    @POST
    @Transactional
    @Path("add-dept-user-relation")
    public Response createYunDeptUserRelation(YunGroupVo yunGroupVo){
        String deptId = yunGroupVo.getGroupId();
        String userId = yunGroupVo.getUserId();
        String hql = " update YunUsers user set user.deptId = " + deptId +" where user.id = '"+userId+"' ";
        int res = baseFacade.createNativeQuery(hql).executeUpdate();
        return Response.status(Response.Status.OK).entity(res).build();
    }

    /**
     * 根据传入的科室Id和用户Id删除其关联关系
     * @param deptId
     * @param userId
     * @return
     */
    @POST
    @Transactional
    @Path("del-user-dept-relation")
    public Response delYunDeptUserRelation(@QueryParam("deptId") String deptId,@QueryParam("userId") String userId){
        String hql = " update YunUsers user set user.deptId = 0 where user.id = '"+userId+"' and user.deptId = "+deptId;
        Integer res = baseFacade.createNativeQuery(hql).executeUpdate();
        return Response.status(Response.Status.OK).entity(res).build();
    }
}
