package com.dchealth.service.rare;

import com.dchealth.VO.DcodeCountInfo;
import com.dchealth.VO.DiseaseShareCount;
import com.dchealth.VO.DiseaseStatisVo;
import com.dchealth.entity.common.RoleVsUser;
import com.dchealth.entity.rare.YunDiseaseList;
import com.dchealth.facade.common.BaseFacade;
import com.dchealth.util.GroupQuerySqlUtil;
import com.dchealth.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.util.*;

/**
 * Created by Administrator on 2017/7/11.
 */
@Controller
@Path("statistics")
@Produces("application/json")
public class DiseaseCountService {
    @Autowired
    private BaseFacade baseFacade ;

    /**
     *根据医生id统计其研究病历的统计信息
     * @param doctorId
     * @return
     */
    @GET
    @Path("pat-disease-statis")
    public Response getDiseaseStatistics(@QueryParam("doctorId") String doctorId){
        List<String> dateList = getStaticsDate();
        List<DiseaseStatisVo> diseaseStatisVos = new ArrayList<>();
        //首先查询是否有管理病历信息
       //Set<String> diseaseSet =  getManageDisease(doctorId);
       String  diseasHql = "select ydl from YunUserDisease yud ,YunDiseaseList ydl where ydl.dcode=yud.dcode ";
       String userIds = GroupQuerySqlUtil.getUserIds(doctorId,baseFacade);
       if(StringUtils.isEmpty(userIds)){
           diseasHql += " and yud.userId='"+doctorId+"'";
       }else{
           diseasHql += " and yud.userId  in ("+doctorId+")";
       }
       List<YunDiseaseList>  yunDiseaseLists = baseFacade.createQuery(YunDiseaseList.class,diseasHql,new ArrayList<Object>()).getResultList();
        Map<String,Long>  discussMap = getPatNumberByType(doctorId,userIds,"0");//
        Map<String,Long>  manageMap = getPatNumberByType(doctorId,userIds,"1");//
        discussMap.putAll(manageMap);
       for(YunDiseaseList yunDiseaseList:yunDiseaseLists){
            DiseaseStatisVo diseaseStatisVo = new DiseaseStatisVo();
            String dcode = yunDiseaseList.getDcode();
            diseaseStatisVo.setDcode(dcode);
            diseaseStatisVo.setMonths(dateList);
            List<DcodeCountInfo> dcodeCountInfos = new ArrayList<>();
            for(String queryDate:dateList){
                DcodeCountInfo dcodeCountInfo = new DcodeCountInfo();
                dcodeCountInfo.setDate(queryDate);
                Long total = discussMap.get(queryDate+dcode)==null?0L:discussMap.get(queryDate+dcode);
                dcodeCountInfo.setTotal(total);//getDiseasCount(queryDate,dcode,doctorId,diseaseSet)
                dcodeCountInfos.add(dcodeCountInfo);
            }
            diseaseStatisVo.setDcodeCountInfos(dcodeCountInfos);
            diseaseStatisVos.add(diseaseStatisVo);
        }
        return Response.status(Response.Status.OK).entity(diseaseStatisVos).build();
    }

    public Map<String,Long> getPatNumberByType(String doctorId,String userIds,String type){
        Map<String,Long> retMap = new HashMap<String,Long>();
        String sql ="SELECT count(*) CT,f.diagnosis_code,DATE_FORMAT(f.create_date,'%Y-%m') from yun_patient p,yun_folder f where p.id = f.patient_id ";
        if("0".equals(type)){
            if(StringUtils.isEmpty(userIds)){
                sql +=" and p.doctor_id = '"+doctorId+"' ";
            }else{
                sql +=" and p.doctor_id in ("+userIds+") ";
            }
        }else if("1".equals(type)){
            if(StringUtils.isEmpty(userIds)){
                sql += " and exists(select 1 from yun_user_disease_manager ym where ym.dcode = f.diagnosis_code and ym.user_id = '"+doctorId+"')";
            }else{
                sql += " and exists(select 1 from yun_user_disease_manager ym where ym.dcode = f.diagnosis_code and ym.user_id in ("+userIds+"))";
            }
        }
        sql += " and f.create_date BETWEEN date_sub(date_sub(date_format(now(),'%y-%m-%d 00:00:00'),interval extract( day from now())-1 day),interval 5 month) and NOW() " +
                " GROUP BY f.diagnosis_code,DATE_FORMAT(f.create_date,'%Y-%m')";
        List list = baseFacade.createNativeQuery(sql).getResultList();
        if(list!=null && !list.isEmpty()){
            int size = list.size();
            for(int i=0;i<size;i++){
                Object[] params = (Object[])list.get(i);
                retMap.put(params[2]+""+params[1]+"",Long.parseLong(params[0].toString()));
            }
        }
        return retMap;
    }
    public Set getManageDisease(String doctorId){
        Set<String> set = new HashSet<>();
        String hql = "select ydl from YunUserDiseaseManager yud ,YunDiseaseList ydl where ydl.dcode=yud.dcode "+
                " and yud.userId='"+doctorId+"'" ;
        List<YunDiseaseList> yunDiseaseLists = baseFacade.createQuery(YunDiseaseList.class, hql, new ArrayList<Object>()).getResultList();
        for(YunDiseaseList yunDiseaseList:yunDiseaseLists){
            set.add(yunDiseaseList.getDcode());
        }
        return set;
    }
    /**
     *根据医生Id判断其是否有病人列表高级权限
     * @param doctorId
     * @return
     */
    public Boolean checkIsAdmin(String doctorId){
        Boolean isAdmin = false;
        String hql = " select rvu from RoleVsUser as rvu,RoleVsResource as rvr,ResourceDict as rd where " +
                " rvu.roleId = rvr.roleId and rvr.resourceId = rd.id and rd.code = 'SICK_PLUS' " +
                " and rvu.userId = '"+doctorId+"'";
        List<RoleVsUser> yunDiseaseLists = baseFacade.createQuery(RoleVsUser.class,hql,new ArrayList<Object>()).getResultList();
        if(yunDiseaseLists!=null && !yunDiseaseLists.isEmpty()){
            isAdmin = true;
        }
        return isAdmin;
    }

    /**
     * 根据日期和疾病编码查询该月份下的疾病数
     * @param queryDate
     * @param dcode
     * @return
     */
    public Long getDiseasCount(String queryDate,String dcode,String doctorId,Set diseaseSet){
        String [] dateArray = queryDate.split("-");
        String year = dateArray[0];
        String month = dateArray[1];
        String hql = "select count(p) from YunPatient as p,YunFolder as f where p.id = f.patientId and Year(f.createDate) = " +year+
                " and Month(f.createDate) = "+month+" and f.diagnosisCode = '"+dcode+"'";
        if(diseaseSet==null || !diseaseSet.contains(dcode)){
            hql += " and p.doctorId = '"+doctorId+"'";
        }
        return baseFacade.createQuery(Long.class,hql,new ArrayList<Object>()).getSingleResult();
    }

    /**
     * 获取要统计的月份信息(从当前月份向前推六个月)
     * @return
     */
    public List getStaticsDate(){
        List<String> dateList = new ArrayList<>();
        for(int i=0;i<6;i++){
            Calendar ca = Calendar.getInstance();
            ca.add(Calendar.MONTH,-i);
            int year = ca.get(Calendar.YEAR);
            int month = ca.get(Calendar.MONTH)+1;
            String dateStr = "";
            if(month<10){
                dateStr = year+"-"+"0"+month;
            }else{
                dateStr = year+"-"+month;
            }
            dateList.add(dateStr);
        }
        return dateList;
    }

    /**
     *获取数据共享各个疾病的病例数统计
     * @return
     */
    @GET
    @Path("get-disease-share-count")
    public List<DiseaseShareCount> getDiseaseShareCounts(){
        List<DiseaseShareCount> diseaseShareCounts = new ArrayList<>();
        String sql = "select count(p.id),f.diagnosis_code,(select name from yun_disease_list where dcode = f.diagnosis_code)" +
                " from yun_folder f,yun_patient p where f.patient_id = p.id and f.diagnosis_code !='' group by f.diagnosis_code";
        List list = baseFacade.createNativeQuery(sql).getResultList();
        if(list!=null && !list.isEmpty()) {
            int size = list.size();
            for (int i = 0; i < size; i++) {
                DiseaseShareCount diseaseShareCount = new DiseaseShareCount();
                Object[] params = (Object[]) list.get(i);
                int count = Integer.valueOf(params[0].toString());
                diseaseShareCount.setCount(count);
                diseaseShareCount.setDcode(params[1]+"");
                diseaseShareCount.setDcodeName(params[2]+"");
                diseaseShareCounts.add(diseaseShareCount);
            }
        }
        return diseaseShareCounts;
    }
}
