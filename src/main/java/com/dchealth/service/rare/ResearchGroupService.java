package com.dchealth.service.rare;

import com.aliyun.mns.client.impl.queue.CreateQueueAction;
import com.dchealth.VO.InviteUserVo;
import com.dchealth.VO.Page;
import com.dchealth.VO.ResearchGroupVo;
import com.dchealth.entity.common.HospitalDict;
import com.dchealth.entity.common.YunUsers;
import com.dchealth.entity.rare.InviteApplyRecord;
import com.dchealth.entity.rare.ResearchGroup;
import com.dchealth.entity.rare.ResearchGroupVsHospital;
import com.dchealth.entity.rare.ResearchGroupVsUser;
import com.dchealth.facade.common.BaseFacade;
import com.dchealth.provider.MessagePush;
import com.dchealth.util.DwrScriptSessionManagerUtil;
import com.dchealth.util.StringUtils;
import com.dchealth.util.UserUtils;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.TypedQuery;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.*;

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
     * @param researchGroupVo
     * @return
     */
    @POST
    @Path("merge")
    @Transactional
    public Response mergeResearchGroup(ResearchGroupVo researchGroupVo) throws Exception{
        if("-1".equals(researchGroupVo.getStatus())){
            String delHospitalHql = " delete from ResearchGroupVsHospital where groupId = '"+researchGroupVo.getId()+"'";
            baseFacade.excHql(delHospitalHql);
            String delUser = "delete from ResearchGroupVsUser where groupId = '"+researchGroupVo.getId()+"'";
            baseFacade.excHql(delUser);
            ResearchGroup researchGroup = baseFacade.get(ResearchGroup.class,researchGroupVo.getId());
            //researchGroup.setStatus(researchGroupVo.getStatus());
            researchGroup.setStatus(researchGroupVo.getStatus());
            ResearchGroup merge = baseFacade.merge(researchGroup);
            return Response.status(Response.Status.OK).entity(merge).build();
        }
        if(StringUtils.isEmpty(researchGroupVo.getId())){
            ResearchGroup researchGroup = new ResearchGroup();
            researchGroup.setDataShareLevel(researchGroupVo.getDataShareLevel());
            researchGroup.setGroupDesc(researchGroupVo.getGroupDesc());
            researchGroup.setGroupInInfo(researchGroupVo.getGroupInInfo());
            researchGroup.setManyHospitalFlag(researchGroupVo.getManyHospitalFlag());
            researchGroup.setResearchDiseaseId(researchGroupVo.getResearchDiseaseId());
            researchGroup.setResearchGroupName(researchGroupVo.getResearchGroupName());
            //researchGroup.setStatus(researchGroupVo.getStatus());
            researchGroup.setStatus("0");
            ResearchGroup merge = baseFacade.merge(researchGroup);
            if(researchGroupVo.getHospitals()!=null && !researchGroupVo.getHospitals().isEmpty()){
                for(HospitalDict hospitalDict:researchGroupVo.getHospitals()){
                    ResearchGroupVsHospital researchGroupVsHospital = new ResearchGroupVsHospital();
                    researchGroupVsHospital.setGroupId(merge.getId());
                    researchGroupVsHospital.setHospitalId(hospitalDict.getId());
                    ResearchGroupVsHospital mergeHospital = baseFacade.merge(researchGroupVsHospital);
                }
            }
            YunUsers yunUsers = UserUtils.getYunUsers();
            ResearchGroupVsUser researchGroupVsUser = new ResearchGroupVsUser();
            researchGroupVsUser.setLearderFlag("1");
            researchGroupVsUser.setCreaterFlag("1");
            researchGroupVsUser.setUserId(yunUsers.getId());
            researchGroupVsUser.setGroupId(merge.getId());
            baseFacade.merge(researchGroupVsUser);
            return Response.status(Response.Status.OK).entity(merge).build();
        }else{
            String delHospitalHql = " delete from ResearchGroupVsHospital where groupId = '"+researchGroupVo.getId()+"'";
            baseFacade.excHql(delHospitalHql);
            ResearchGroup researchGroup = baseFacade.get(ResearchGroup.class,researchGroupVo.getId());
            researchGroup.setDataShareLevel(researchGroupVo.getDataShareLevel());
            researchGroup.setGroupDesc(researchGroupVo.getGroupDesc());
            researchGroup.setGroupInInfo(researchGroupVo.getGroupInInfo());
            researchGroup.setManyHospitalFlag(researchGroupVo.getManyHospitalFlag());
            researchGroup.setResearchDiseaseId(researchGroupVo.getResearchDiseaseId());
            researchGroup.setResearchGroupName(researchGroupVo.getResearchGroupName());
            ResearchGroup merge = baseFacade.merge(researchGroup);
            if(researchGroupVo.getHospitals()!=null && !researchGroupVo.getHospitals().isEmpty()){
                for(HospitalDict hospitalDict:researchGroupVo.getHospitals()){
                    ResearchGroupVsHospital researchGroupVsHospital = new ResearchGroupVsHospital();
                    researchGroupVsHospital.setGroupId(merge.getId());
                    researchGroupVsHospital.setHospitalId(hospitalDict.getId());
                    ResearchGroupVsHospital mergeHospital = baseFacade.merge(researchGroupVsHospital);
                }
            }
            return Response.status(Response.Status.OK).entity(merge).build();
        }
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
    public Page<ResearchGroupVo> getResearchGroups(@QueryParam("groupName")String groupName,@QueryParam("diseaseId")String diseaseId,
                                                 @QueryParam("userId")String userId,@QueryParam("status")String status,@QueryParam("userType")String userType,
                                                 @QueryParam("perPage") int perPage,@QueryParam("currentPage") int currentPage){
        String hql = "select new com.dchealth.VO.ResearchGroupVo(r.id,r.researchGroupName,r.researchDiseaseId,r.groupDesc,r.groupInInfo," +
                "r.manyHospitalFlag,r.dataShareLevel,r.status)  from ResearchGroup as r where r.status <>'-1'";
        if(!StringUtils.isEmpty(groupName)){
            hql += " and r.researchGroupName like '%"+groupName+"%'";
        }
        if(!StringUtils.isEmpty(diseaseId)){
            hql += " and r.researchDiseaseId = '"+diseaseId+"'";
        }
        if(!StringUtils.isEmpty(userId) && !StringUtils.isEmpty(userType)){
            if("0".equals(userType)){//userType=0表示查询出该用户创建的科研项目
                hql += " and exists(select 1 from ResearchGroupVsUser where groupId = r.id and userId = '"+userId+"' and createrFlag = '1')";
            }else if("1".equals(userType)){//该用户参与的群组
                hql += " and exists(select 1 from ResearchGroupVsUser where groupId = r.id and userId = '"+userId+"' and createrFlag = '0')";
            }else if("2".equals(userType)){//查询出该用户未参加的，切与本医院有关的群组
                hql += " and not exists(select 1 from InviteApplyRecord where groupId = r.id and status = '1' and userId = '"+userId+"') and " +
                        " id in (select groupId from ResearchGroupVsHospital where hospitalId = (select h.id from HospitalDict as h,YunUsers as u where " +
                        " u.hospitalCode = h.hospitalCode and u.id = '"+userId+"'))";
            }else if("3".equals(userType)){//查询出该用户参加待审核的，切与本医院有关的群组
                hql += " and not exists(select 1 from InviteApplyRecord where groupId = r.id and status = '0' and userId = '"+userId+"') and " +
                        " id in (select groupId from ResearchGroupVsHospital where hospitalId = (select h.id from HospitalDict as h,YunUsers as u where " +
                        " u.hospitalCode = h.hospitalCode and u.id = '"+userId+"'))";
            }
        }
        if(!StringUtils.isEmpty(status)){
            hql += " and status = '"+status+"'";
        }
        Page<ResearchGroupVo> researchGroupPage = baseFacade.getPageResult(ResearchGroupVo.class,hql,perPage,currentPage);
        Map<String,List<HospitalDict>> map = new HashMap<String,List<HospitalDict>>();
        StringBuffer groupIds = new StringBuffer("");
        for(ResearchGroupVo researchGroupVo:researchGroupPage.getData()){
            groupIds.append("'").append(researchGroupVo.getId()).append("',");
            if(map.get(researchGroupVo.getId())==null){
                List<HospitalDict> hospitalDicts = new ArrayList<>();
                map.put(researchGroupVo.getId(),hospitalDicts);
            }
        }
        String groupIdsTo = groupIds.toString();
        if(!StringUtils.isEmpty(groupIdsTo)){
            groupIdsTo = groupIdsTo.substring(0,groupIdsTo.length()-1);
            String hospitalSql = "select h.id,h.hospital_name,h.hospital_code,h.status,r.group_id from hospital_dict as h,research_group_vs_hospital as r where h.status<>'-1' and r.hospital_id = h.id and r.group_id in ("+groupIdsTo+")";
            List list = baseFacade.createNativeQuery(hospitalSql).getResultList();
            if(list!=null && !list.isEmpty()){
                int size = list.size();
                for(int i=0;i<size;i++){
                    Object[] params = (Object[])list.get(i);
                    String groupId = (String)params[4];
                    if(map.get(groupId)!=null){
                        List<HospitalDict> hospitalDicts = map.get(groupId);
                        HospitalDict hospitalDict = new HospitalDict();
                        hospitalDict.setId((String)params[0]);
                        hospitalDict.setHospitalName((String)params[1]);
                        hospitalDict.setHospitalCode((String)params[2]);
                        hospitalDict.setStatus((String)params[3]);
                        hospitalDicts.add(hospitalDict);
                    }
                }
            }
            for(ResearchGroupVo researchGroupVo:researchGroupPage.getData()){
                researchGroupVo.setHospitals(map.get(researchGroupVo.getId()));
            }
        }
        return researchGroupPage;
    }

    /**
     *根据群组id获取群组信息
     * @param groupId 群组id
     * @return
     */
    @GET
    @Path("get-research-group")
    public ResearchGroupVo getResearchGroupById(@QueryParam("groupId")String groupId) throws Exception{
        String hql = " from ResearchGroup where id = '"+groupId+"'";
        List<ResearchGroup> researchGroups = baseFacade.createQuery(ResearchGroup.class,hql,new ArrayList<Object>()).getResultList();
        String hospitalSql = "select h from HospitalDict as h,ResearchGroupVsHospital as r where h.status<>'-1' and r.hospitalId = h.id and r.groupId  ='"+groupId+"'";
        List<HospitalDict> hospitalDicts = baseFacade.createQuery(HospitalDict.class,hospitalSql,new ArrayList<Object>()).getResultList();
        if(researchGroups!=null && !researchGroups.isEmpty()){
            ResearchGroupVo researchGroupVo = new ResearchGroupVo(researchGroups.get(0).getId(),researchGroups.get(0).getResearchGroupName(),
                    researchGroups.get(0).getResearchDiseaseId(),researchGroups.get(0).getGroupDesc(),researchGroups.get(0).getGroupInInfo(),
                    researchGroups.get(0).getManyHospitalFlag(),researchGroups.get(0).getDataShareLevel(),researchGroups.get(0).getStatus());
            researchGroupVo.setHospitals(hospitalDicts);
            return researchGroupVo;
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

    /**
     * 登录用户查看自己创建的组已邀请人员信息
     * @param userID 用户id 对应id 非userId
     * @param status 0表示待处理、1表示邀请人同意 -1表示拒绝
     * @param perPage 每页条数
     * @param currentPage 当前页
     *                    flag    1表示邀请   0表示申请
     * @return
     */
    @GET
    @Path("get-already-invite-users")
    public Page<InviteUserVo> getInviteUsers(@QueryParam("userID")String userID,@QueryParam("status")String status,
                                         @QueryParam("perPage") int perPage,@QueryParam("currentPage") int currentPage){
        return getInviteOrApplyUserVos(userID,status,"1",perPage,currentPage);
    }

    /**
     *根据用户id获取用户创建组申请人员信息
     * @param userID 用户id 对应id 非userId
     * @param status 0表示待处理、1表示邀请人同意 -1表示拒绝
     * @param perPage 每页条数
     * @param currentPage 当前页
     *                    flag    1表示邀请   0表示申请
     * @return
     */
    @GET
    @Path("get-already-apply-users")
    public Page<InviteUserVo> getApplyUsers(@QueryParam("userID")String userID,@QueryParam("status")String status,
                                             @QueryParam("perPage") int perPage,@QueryParam("currentPage") int currentPage){
        return getInviteOrApplyUserVos(userID,status,"0",perPage,currentPage);
    }
    /**
     *根据flag值判断是邀请或者申请，如果是邀请 获取当前用户邀请人员信息，如果为申请获取该用户创建组的申请人员信息
     * @param userID 用户id 对应id 非userId
     * @param status 0表示待处理、1表示邀请人同意 -1表示拒绝
     * @param flag 1表示邀请   0表示申请
     * @param perPage 每页条数
     * @param currentPage 当前页
     * @return
     */
    public Page<InviteUserVo> getInviteOrApplyUserVos(String userID,String status,String flag,int perPage,int currentPage){
        String hql = "select new com.dchealth.VO.InviteUserVo(u.id,u.userName,u.sex,u.nation,u.mobile,u.tel,u.email,u.birthDate," +
                "u.title,u.hospitalName,g.researchGroupName as groupName,(select d.name from YunDiseaseList as d where d.id = g.researchDiseaseId) as diseaseName,p.id as recordId,p.status)" +
                " from YunUsers as u,ResearchGroup as g,InviteApplyRecord as p,ResearchGroupVsUser as c" +
                " where g.id = p.groupId and g.id = c.groupId and u.id = p.userId and p.flag = '"+flag+"' and " +
                " c.userId = '"+userID+"'";
        String hqlCount = "select count(*) from YunUsers as u,ResearchGroup as g,InviteApplyRecord as p,ResearchGroupVsUser as c" +
                " where g.id = p.groupId and g.id = c.groupId and u.id = p.userId and p.flag = '"+flag+"' and " +
                " c.userId = '"+userID+"'";
        if(!StringUtils.isEmpty(status)){
            hql += " and p.status = '"+status+"'";
            hqlCount += " and p.status = '"+status+"'";
        }
        Page<InviteUserVo> resultPage = new Page<>();
        TypedQuery<InviteUserVo> typedQuery = baseFacade.createQuery(InviteUserVo.class,hql,new ArrayList<Object>());
        Long counts =  baseFacade.createQuery(Long.class,hqlCount,new ArrayList<Object>()).getSingleResult();
        resultPage.setCounts(counts);
        if(perPage<=0){
            perPage =1;
        }
        if(currentPage<=0){
            currentPage=1;
        }
        typedQuery.setFirstResult((currentPage-1)*perPage);
        typedQuery.setMaxResults(perPage);
        resultPage.setPerPage((long)perPage);
        List<InviteUserVo> resultList = typedQuery.getResultList();
        resultPage.setData(resultList);
        return resultPage;
    }
    /**
     *flag=1表示获取邀请自己的记录 包含同意的，未同意的，待处理的，flag=0表示自己申请的记录信息
     * @param userID 用户id 对应id 非userId
     * @param status 0表示待处理、1表示邀请人同意 -1表示拒绝
     * flag    1表示邀请   0表示申请
     * @return
     */
    @GET
    @Path("get-invite-or-apply-records")
    public Page<InviteUserVo> getMyInviteApplyRecords(@QueryParam("userID")String userID,@QueryParam("status")String status,@QueryParam("flag")String flag,
                                                      @QueryParam("perPage") int perPage,@QueryParam("currentPage") int currentPage){
        String hql = "select new com.dchealth.VO.InviteUserVo(u.id,u.userName,u.sex,u.nation,u.mobile,u.tel,u.email,u.birthDate," +
                "u.title,u.hospitalName,g.researchGroupName as groupName,(select d.name from YunDiseaseList as d where d.id = g.researchDiseaseId) as diseaseName,p.id as recordId,p.status)" +
                " from YunUsers as u,ResearchGroup as g,InviteApplyRecord as p,ResearchGroupVsUser as c" +
                " where g.id = p.groupId and g.id = c.groupId and u.id = p.userId and p.flag = '"+flag+"' and " +
                " c.userId != '"+userID+"' and p.userId = '"+userID+"'";
        String hqlCount = "select count(*) from YunUsers as u,ResearchGroup as g,InviteApplyRecord as p,ResearchGroupVsUser as c" +
                " where g.id = p.groupId and g.id = c.groupId and u.id = p.userId and p.flag = '"+flag+"' and " +
                " c.userId != '"+userID+"' and p.userId = '"+userID+"'";
        if(!StringUtils.isEmpty(status)){
            hql += " and p.status = '"+status+"'";
            hqlCount += " and p.status = '"+status+"'";
        }
        Page<InviteUserVo> resultPage = new Page<>();
        TypedQuery<InviteUserVo> typedQuery = baseFacade.createQuery(InviteUserVo.class,hql,new ArrayList<Object>());
        Long counts =  baseFacade.createQuery(Long.class,hqlCount,new ArrayList<Object>()).getSingleResult();
        resultPage.setCounts(counts);
        if(perPage<=0){
            perPage =1;
        }
        if(currentPage<=0){
            currentPage=1;
        }
        typedQuery.setFirstResult((currentPage-1)*perPage);
        typedQuery.setMaxResults(perPage);
        resultPage.setPerPage((long)perPage);
        List<InviteUserVo> resultList = typedQuery.getResultList();
        resultPage.setData(resultList);
        return resultPage;
    }

    /**
     *对群组邀请进行操作，同意或拒绝，或者申请进行操作，同意或拒绝
     * @param recordId 邀请记录id
     * @param status 状态值 0表示待处理、1表示同意-1表示为拒绝
     * @return
     */
    @POST
    @Path("confirm-invite-record")
    @Transactional
    public Response confirmInviteRecord(@QueryParam("recordId")String recordId,@QueryParam("status")String status) throws Exception{
        InviteApplyRecord inviteApplyRecord = null;
        try {
            inviteApplyRecord = baseFacade.get(InviteApplyRecord.class,recordId);
            if("0".equals(inviteApplyRecord.getStatus())){
                inviteApplyRecord.setStatus(status);
                inviteApplyRecord = baseFacade.merge(inviteApplyRecord);
                if("1".equals(status)){
                    ResearchGroupVsUser researchGroupVsUser = new ResearchGroupVsUser();
                    researchGroupVsUser.setLearderFlag("0");
                    researchGroupVsUser.setCreaterFlag("0");
                    researchGroupVsUser.setUserId(inviteApplyRecord.getUserId());
                    researchGroupVsUser.setGroupId(inviteApplyRecord.getGroupId());
                    baseFacade.merge(researchGroupVsUser);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            throw new Exception("操作异常，请联系管理员");
        }
        return Response.status(Response.Status.OK).entity(inviteApplyRecord).build();
    }

    /**
     *获取群组下的成员信息
     * @param groupId 群组id
     * @param perPage 每页条数
     * @param currentPage 当前页
     * @return
     */
    @GET
    @Path("get-research-group-users")
    public Page<YunUsers> getResearchGroupUsers(@QueryParam("groupId")String groupId,@QueryParam("perPage") int perPage,@QueryParam("currentPage") int currentPage){
        String hql = "select u from YunUsers as u,ResearchGroupVsUser as v where u.id = v.userId and v.groupId = '"+groupId+"'";
        return baseFacade.getPageResult(YunUsers.class,hql,perPage,currentPage);
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
