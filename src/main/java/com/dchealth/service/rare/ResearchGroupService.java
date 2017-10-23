package com.dchealth.service.rare;

import com.aliyun.mns.client.impl.queue.CreateQueueAction;
import com.dchealth.VO.Page;
import com.dchealth.entity.common.YunUsers;
import com.dchealth.entity.rare.InviteApplyRecord;
import com.dchealth.entity.rare.ResearchGroup;
import com.dchealth.entity.rare.ResearchGroupVsUser;
import com.dchealth.facade.common.BaseFacade;
import com.dchealth.provider.MessagePush;
import com.dchealth.util.DwrScriptSessionManagerUtil;
import com.dchealth.util.StringUtils;
import com.dchealth.util.UserUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Administrator on 2017/10/19.
 */
@Controller
@Produces("application/json")
@Path("research-group")
public class ResearchGroupService {

    @Autowired
    private BaseFacade baseFacade ;

    /**
     * 添加，修改，删除群组，删除为逻辑删除 status = -1
     * @param researchGroup
     * @return
     */
    @POST
    @Path("merge")
    @Transactional
    public Response mergeResearchGroup(ResearchGroup researchGroup) throws Exception{
        if("-1".equals(researchGroup.getStatus())){
            String delHospitalHql = " delete from ResearchGroupVsHospital where groupId = '"+researchGroup.getId()+"'";
            baseFacade.excHql(delHospitalHql);
            String delUser = "delete from ResearchGroupVsUser where groupId = '"+researchGroup.getId()+"'";
            baseFacade.excHql(delUser);
        }
        if(StringUtils.isEmpty(researchGroup.getId())){
            ResearchGroup merge = baseFacade.merge(researchGroup);
            YunUsers yunUsers = UserUtils.getYunUsers();
            ResearchGroupVsUser researchGroupVsUser = new ResearchGroupVsUser();
            researchGroupVsUser.setLearderFlag("1");
            researchGroupVsUser.setCreaterFlag("1");
            researchGroupVsUser.setUserId(yunUsers.getId());
            researchGroupVsUser.setGroupId(merge.getId());
            baseFacade.merge(researchGroupVsUser);
            return Response.status(Response.Status.OK).entity(merge).build();
        }
        return Response.status(Response.Status.OK).entity(baseFacade.merge(researchGroup)).build();
    }

    /**
     *根据传入条件查询研究群组信息
     * @param groupName 研究组名称
     * @param diseaseId 多中心标志
     * @param userId 级别范围A，B两个级别
     * @param status 0表示正常申请，1表示审批通过 -1表示删除
     * @param perPage 每页条数
     * @param currentPage 当前页数
     * @return
     */
    @GET
    @Path("get-research-groups")
    public Page<ResearchGroup> getResearchGroups(@QueryParam("groupName")String groupName,@QueryParam("diseaseId")String diseaseId,
                                                 @QueryParam("userId")String userId,@QueryParam("status")String status,@QueryParam("userType")String userType,
                                                 @QueryParam("perPage") int perPage,@QueryParam("currentPage") int currentPage){
        String hql = " from ResearchGroup as r where r.status <>'-1'";
        if(!StringUtils.isEmpty(groupName)){
            hql += " and r.researchGroupName like '%"+groupName+"%'";
        }
        if(!StringUtils.isEmpty(diseaseId)){
            hql += " and r.researchDiseaseId = '"+diseaseId+"'";
        }
        if(!StringUtils.isEmpty(userId) && !StringUtils.isEmpty(userType)){
            if("0".equals(userType)){//userType=0表示查询出该用户创建的科研项目
                hql += " and exists(select 1 from ResearchGroupVsUser where groupId = r.id and userId = '"+userId+"' and createrFlag = '1')";
            }else if("1".equals(userType)){
                hql += " and exists(select 1 from ResearchGroupVsUser where groupId = r.id and userId = '"+userId+"' and createrFlag = '0')";
            }else if("2".equals(userType)){//查询出该用户未参加的，切与本医院有关的群组
                hql += " and not exists(select 1 from ResearchGroupVsUser where groupId = r.id and userId = '"+userId+"') and " +
                        " id in (select groupId from ResearchGroupVsHospital where hospitalId = (select h.id from HospitalDict as h,YunUsers as u where " +
                        " u.hospitalCode = h.hospitalCode and u.id = '"+userId+"'))";
            }
        }
        if(!StringUtils.isEmpty(status)){
            hql += " and status = '"+status+"'";
        }
        return baseFacade.getPageResult(ResearchGroup.class,hql,perPage,currentPage);
    }

    /**
     *根据群组id获取群组信息
     * @param groupId 群组id
     * @return
     */
    @GET
    @Path("get-research-group")
    public ResearchGroup getResearchGroupById(@QueryParam("groupId")String groupId) throws Exception{
        String hql = " from ResearchGroup where id = '"+groupId+"'";
        List<ResearchGroup> researchGroups = baseFacade.createQuery(ResearchGroup.class,hql,new ArrayList<Object>()).getResultList();
        if(researchGroups!=null && !researchGroups.isEmpty()){
            return researchGroups.get(0);
        }else{
            throw new Exception("该群组信息不存在");
        }
    }

    /**
     *移除一个群组成员
     * @param groupId 群组id
     * @param userId 用户id
     * @return
     * @throws Exception
     */
    @POST
    @Path("remove-user")
    @Transactional
    public Response removeResearchGroupUser(@QueryParam("groupId")String groupId,@QueryParam("userId")String userId) throws Exception{
        if(StringUtils.isEmpty(groupId)){
            throw new Exception("群组id不能为空");
        }
        if(StringUtils.isEmpty(userId)){
            throw new Exception("用户id不能为空");
        }
        String delHql = "delete from ResearchGroupVsUser where groupId = '"+groupId+"' and userId = '"+userId+"'";
        baseFacade.excHql(delHql);
        List<String> resultList = new ArrayList<>();
        resultList.add(groupId);
        return Response.status(Response.Status.OK).entity(resultList).build();
    }

    /**
     * 邀请一个人人员入组
     * @param groupId
     * @param userId
     * @return
     */
    @POST
    @Path("invite-research-user")
    @Transactional
    public Response inviteResearchUser(@QueryParam("groupId")String groupId,@QueryParam("userId")String userId) throws Exception{
        if(StringUtils.isEmpty(groupId)){
            throw new Exception("群组id不能为空");
        }
        if(StringUtils.isEmpty(userId)){
            throw new Exception("用户id不能为空");
        }
        InviteApplyRecord inviteApplyRecord = new InviteApplyRecord();
        inviteApplyRecord.setGroupId(groupId);
        inviteApplyRecord.setUserId(userId);
        inviteApplyRecord.setFlag("1");
        inviteApplyRecord.setStatus("0");
        inviteApplyRecord.setCreateDate(new Date());
        InviteApplyRecord merge = baseFacade.merge(inviteApplyRecord);
        return Response.status(Response.Status.OK).entity(merge).build();
    }

    /**
     * 申请一个群组加入
     * @param groupId
     * @param userId
     * @return
     */
    @POST
    @Path("apply-research-group")
    @Transactional
    public Response applyResearchGroup(@QueryParam("groupId")String groupId,@QueryParam("userId")String userId) throws Exception{
        if(StringUtils.isEmpty(groupId)){
            throw new Exception("群组id不能为空");
        }
        if(StringUtils.isEmpty(userId)){
            throw new Exception("用户id不能为空");
        }
        InviteApplyRecord inviteApplyRecord = new InviteApplyRecord();
        inviteApplyRecord.setGroupId(groupId);
        inviteApplyRecord.setUserId(userId);
        inviteApplyRecord.setFlag("0");
        inviteApplyRecord.setStatus("0");
        inviteApplyRecord.setCreateDate(new Date());
        InviteApplyRecord merge = baseFacade.merge(inviteApplyRecord);
        return Response.status(Response.Status.OK).entity(merge).build();
    }

    /**
     *审批或者同意个人入组
     * @param recordId 申请或者邀请记录Id
     * @return
     */
    @POST
    @Path("agree-group-user")
    @Transactional
    public Response agreeGroupUser(@QueryParam("recordId") String recordId) throws Exception{
        String hql = "from InviteApplyRecord where id = '"+recordId+"'";
        List<InviteApplyRecord> researchGroups = baseFacade.createQuery(InviteApplyRecord.class,hql,new ArrayList<Object>()).getResultList();
        if(researchGroups!=null && !researchGroups.isEmpty()){
            InviteApplyRecord inviteApplyRecord = researchGroups.get(0);
            inviteApplyRecord.setStatus("1");
            ResearchGroupVsUser researchGroupVsUser = new ResearchGroupVsUser();
            researchGroupVsUser.setGroupId(inviteApplyRecord.getGroupId());
            researchGroupVsUser.setUserId(inviteApplyRecord.getUserId());
            researchGroupVsUser.setCreaterFlag("0");
            researchGroupVsUser.setLearderFlag("0");
            baseFacade.merge(inviteApplyRecord);
            ResearchGroupVsUser merge = baseFacade.merge(researchGroupVsUser);
            return Response.status(Response.Status.OK).entity(merge).build();
        }else{
            throw new Exception("申请记录信息不存在");
        }
    }

    @GET
    @Path("test-send")
    public List<String> sendNotice(){
        List list = new ArrayList();
        String userId = "zhang";
        DwrScriptSessionManagerUtil dwrScriptSessionManagerUtil = new DwrScriptSessionManagerUtil();
        dwrScriptSessionManagerUtil.sendMessageAuto(userId,"test");
        list.add(userId);
        return list;
    }
}
