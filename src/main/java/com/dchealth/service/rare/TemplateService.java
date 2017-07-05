package com.dchealth.service.rare;

import com.dchealth.VO.*;
import com.dchealth.entity.common.YunDictitem;
import com.dchealth.entity.common.YunUsers;
import com.dchealth.entity.rare.YunDisTemplet;
import com.dchealth.entity.rare.YunReleaseTemplet;
import com.dchealth.entity.rare.YunValueFormat;
import com.dchealth.facade.common.BaseFacade;
import com.dchealth.util.JSONUtil;
import com.dchealth.util.UserUtils;
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
 * Created by Administrator on 2017/6/21.
 */
@Controller
@Produces("application/json")
@Path("template")
public class TemplateService {

    @Autowired
    private BaseFacade baseFacade ;


    /**
     * 获取工作流列表
     * @param dcode
     * @param doctorId
     * @param title
     * @param mblx
     * @return
     */
    @GET
    @Path("list")
    public List<YunDisTemplet> getDisTemplate(@QueryParam("dcode") String dcode,@QueryParam("doctorId") String doctorId,
                                              @QueryParam("title")String title,@QueryParam("mblx")String mblx,
                                              @QueryParam("deptId")String deptId,
                                              @QueryParam("pubFlag") String pubFlag) throws Exception {
        String hql = "from YunDisTemplet as t where 1=1 and mblx<>'WORK'" ;
        //所属疾病
        if(!"".equals(dcode)&&dcode!=null){
            hql+=" and t.dcode='"+dcode+"'" ;
        }
        //标题
        if(!"".equals(title)&&title!=null){
            hql+=" and t.title='"+title+"'" ;
        }
        //模板类型
        if(!"".equals(mblx)&&mblx!=null){
            hql+=" and t.mblx='"+mblx+"'" ;
        }


        if("".equals(pubFlag)||pubFlag==null){
            throw  new Exception("缺少pubFlag，公共私有数据标识。0表示私有数据，1表示公共数据");
        }

        if("1".equals(pubFlag)){
            hql += " and t.doctorId='0'" ;
        }

        if("0".equals(pubFlag)){
            if(doctorId==null||"".equals(doctorId)){
                throw  new Exception("缺少doctorId，用户标识");
            }
            if(deptId==null||"".equals(deptId)){
                throw  new Exception("缺少deptId，科室标识");
            }
            hql+=" and( t.doctorId='"+doctorId+"' or (t.deptId='"+deptId+"' and t.deptId <>'0'))" ;
        }

        List<YunDisTemplet> yunDisTemplets = baseFacade.createQuery(YunDisTemplet.class, hql, new ArrayList<Object>()).getResultList();
        return yunDisTemplets;
    }


    /**
     * 添加或者修改表单模板
     * @param yunDisTemplet
     * @return
     */
    @POST
    @Path("merge")
    @Transactional
    public Response mergeYunDisTemplate(YunDisTemplet yunDisTemplet){
        YunDisTemplet merge = baseFacade.merge(yunDisTemplet);
        return Response.status(Response.Status.OK).entity(merge).build();
    }

    /**
     * 删除模板
     * @param templateId
     * @return
     */
    @POST
    @Path("del")
    @Transactional
    public Response removeDisTemplate(@QueryParam("templateId") String templateId){
        YunDisTemplet templet = baseFacade.get(YunDisTemplet.class, templateId);
        baseFacade.remove(templet);
        return Response.status(Response.Status.OK).entity(templet).build();
    }


    /**
     * 获取模板设计的内容
     * @param templateId
     * @return
     * @throws Exception
     */
    @GET
    @Path("get-form-design")
    public Form getFormInfo(@QueryParam("templateId") String templateId) throws Exception {
        YunDisTemplet yunDisTemplet = baseFacade.get(YunDisTemplet.class,templateId);
        if(yunDisTemplet==null){
            throw  new Exception("找不到对应的模板信息");
        }
        String mbsj = yunDisTemplet.getMbsj() ;
        if(mbsj==null||"".equals(mbsj)){
            return null ;
        }
        Form form =getFormData(mbsj);
        return form ;
    }

    /**
     * 根据模板数据设置Form
     * @param mbsj
     * @return
     * @throws Exception
     */
    private Form getFormData(String mbsj) throws Exception {

        Form o = (Form) JSONUtil.JSONToObj(mbsj, Form.class);

        List<FormData> form_datas = o.getForm_data();
        FormPage formPage = new FormPage();

        for(FormData formData :form_datas){
            ModelPage modelPage = new ModelPage();
            modelPage.setTitle(formData.getName());
            modelPage.setValue(formData.getValue());

            List<Row> rows = formData.getRows();
            List<RowObject> rowssubjects = modelPage.getRowssubjects();
            for (Row row:rows){
                RowObject rowObject = new RowObject();
                setRowObject(row,rowObject);
                rowssubjects.add(rowObject);
            }
            formPage.getPages().add(modelPage);

        }
        o.setForm_template(formPage);
        return  o;
    }


    /**
     * 根据疾病编码和标题获取表单数据
     * @param dcode
     * @param title
     * @return
     * @throws Exception
     */
    @GET
    @Path("get-work-form")
    public Form getReleaseInfo(@QueryParam("decode")String dcode,@QueryParam("title")String title) throws Exception {
        String hql = "from YunReleaseTemplet as r where r.hstatus='R' and  r.dcode='"+dcode+"' and r.title='"+title+"'" ;
        List<YunReleaseTemplet> resultList = baseFacade.createQuery(YunReleaseTemplet.class, hql, new ArrayList<Object>()).getResultList();
        YunUsers yunUsers = UserUtils.getYunUsers();
        if(resultList.size()>0){
            YunReleaseTemplet yunReleaseTemplet = resultList.get(0);
            String mbsj = yunReleaseTemplet.getMbsj();
            if(mbsj!=null&&!"".equals(mbsj)){
                return getFormData(mbsj);
            }else{
                return  null ;
            }
        }else{
            String hqlPrivate = "from YunDisTemplet as t where t.dcode='"+dcode+"' and t.title='"+title+"' and (t.doctorId='"+yunUsers.getId()+"'" +
                    " or (t.deptId='"+yunUsers.getDeptId()+"' and t.deptId<>'0'))" ;
            List<YunDisTemplet> yunDisTemplets = baseFacade.createQuery(YunDisTemplet.class, hqlPrivate, new ArrayList<Object>()).getResultList();
            if(yunDisTemplets.size()>0){
                YunDisTemplet tmplate = yunDisTemplets.get(0);
                String mbsj = tmplate.getMbsj();
                if(mbsj!=null&&!"".equals(mbsj)){
                    return getFormData(mbsj);
                }else{
                    return  null ;
                }
            }else{
                return null ;
            }
        }


    }

    /**
     * 根据formData Row 设置templateObject 的rowObject
     * @param row
     * @param rowObject
     */
    private void setRowObject(Row row, RowObject rowObject) throws Exception {

        List<Col> cols = row.getCols();
        if(cols.size()>0){
            rowObject.setColumn(String.valueOf(12/cols.size()));
        }

        List<ElementRow> rows = rowObject.getRows();
        for (Col col:cols){
            ElementRow elementRow = new ElementRow();
            elementRow.setName(col.getValue());
            setElementRow(elementRow,col);
            rows.add(elementRow);
        }

    }

    /**
     * 根据列设置行元素
     * @param elementRow
     * @param col
     */
    private void setElementRow(ElementRow elementRow, Col col) throws Exception {
        String value = col.getValue();
        YunUsers yunUsers = UserUtils.getYunUsers();
        String deptId = yunUsers.getDeptId();
        String id = yunUsers.getId();
        String hql = "select t from YunValueFormat t,YunValue as v  where t.id=v.id and  t.title='"+value+"" +
                "' and ((v.doctorId='0' and v.deptId='0') or (v.doctorId='"+id+"' and v.deptId='"+deptId+"')" +
                " or (v.doctorId='"+id+"' and v.deptId='0'))" ;

        List<YunValueFormat> resultList = baseFacade.createQuery(YunValueFormat.class, hql, new ArrayList<Object>()).getResultList();
        if(resultList.size()<1){
            throw new Exception("获取名称为【"+value+"】的元数据格式失败！");
        }
        YunValueFormat yunValueFormat = resultList.get(0);


        DataElementFormat dataElement = (DataElementFormat) JSONUtil.JSONToObj(yunValueFormat.getFormat(), DataElementFormat.class);
        Extend extend = new Extend();
        extend.setHead(dataElement.getHead());
        extend.setPlac(dataElement.getPlac());
        extend.setRelyon(yunValueFormat.getRelyon());
        extend.setRelyonvalue(dataElement.getRelyonvalue());
        extend.setTail(dataElement.getTail());
        extend.setTemplet(dataElement.getTemplet());
        elementRow.setExtend(extend);
        elementRow.setType(dataElement.getPart());
        elementRow.setName(value);
        String dict = yunValueFormat.getDict();

        if(dict!=null&&!"".equals(dict)){
            String hqlDict = "select yi from YunDicttype as yd,YunDictitem yi  where yd.id=yi.typeIdDm and yd.typeName='"+dict+"'" +
                    " and ((yd.deptId='0' and yd.userId='0') or (yd.deptId='0' and yd.userId='"+id+"') or (yd.deptId='"+deptId+"' and yd.userId='"+id+"'))" +
                    " order by yd.userId ,yd.deptId desc" ;
            List<YunDictitem> resultList1 = baseFacade.createQuery(YunDictitem.class, hqlDict, new ArrayList<Object>()).getResultList();
            if(resultList1.size()<1){
                throw new Exception("获取名称为【"+dict+"】的字典失败");
            }

            for(YunDictitem yunDictitem:resultList1){
                RowItem rowItem = new RowItem();
                rowItem.setInputcode(yunDictitem.getInputCode());
                rowItem.setName(value);
                rowItem.setText(yunDictitem.getItemName());
                rowItem.setValue(yunDictitem.getItemCode());
                elementRow.getItems().add(rowItem);
            }
        }
    }


    /**
     * 模板设计保存
     * @param form
     * @param templateId
     * @return
     * @throws Exception
     */
    @POST
    @Path("merge-form-design")
    @Transactional
    public Response desginFormInfo(Form form,@QueryParam("templateId") String templateId) throws Exception {
        String mbsj = JSONUtil.objectToJson(form).toString();
        YunDisTemplet yunDisTemplet = baseFacade.get(YunDisTemplet.class, templateId);
        if(yunDisTemplet==null){
            throw new Exception("获取模板数据失败");
        }
        yunDisTemplet.setMbsj(mbsj);
        YunDisTemplet merge = baseFacade.merge(yunDisTemplet);
        return Response.status(Response.Status.OK).entity(merge).build();
    }


    /**
     * 发布表单或者工作流
     * @param templateId
     * @return
     */
    @Transactional
    @POST
    @Path("pub")
    public Response publishTemplateOrWork(@QueryParam("templateId") String templateId) throws Exception {

        YunDisTemplet yunDisTemplet = baseFacade.get(YunDisTemplet.class, templateId);
        if(yunDisTemplet==null){
            throw new Exception("") ;
        }

        String decode = yunDisTemplet.getDcode() ;
        String title = yunDisTemplet.getTitle() ;
        String doctorId = yunDisTemplet.getDoctorId() ;
        String deptId = yunDisTemplet.getDeptId();
        String hql = "from YunReleaseTemplet as r where r.dcode='"+decode+"'" +
                " and r.title='"+title+"'" ;
        Hversion hversion= new Hversion();
        hversion.setDept(deptId);
        hversion.setDoctor(doctorId);
        hversion.setNum("1");

        List<YunReleaseTemplet> resultList = baseFacade.createQuery(YunReleaseTemplet.class, hql, new ArrayList<Object>()).getResultList();
        for(YunReleaseTemplet templet:resultList){
            String hversion1 = templet.getHversion();
            Hversion tempHversion = (Hversion) JSONUtil.JSONToObj(hversion1, Hversion.class);
            if(doctorId.equals(tempHversion.getDoctor())&&deptId.equals(tempHversion.getDept())){
                baseFacade.remove(templet);
            }
        }
        YunReleaseTemplet yunReleaseTemplet = new YunReleaseTemplet();
        yunReleaseTemplet.setDcode(decode);
        yunReleaseTemplet.setHstatus("C");
        yunReleaseTemplet.setMblx(yunDisTemplet.getMblx());
        yunReleaseTemplet.setMbsj(yunDisTemplet.getMbsj());
        yunReleaseTemplet.setModifyDate((Timestamp) new Date());
        yunReleaseTemplet.setNote(yunDisTemplet.getNote());
        yunReleaseTemplet.setTitle(title);
        yunReleaseTemplet.setValuedata("");
        yunReleaseTemplet.setHversion(JSONUtil.objectToJson(hversion).toString());
        YunReleaseTemplet releaseTemplet = baseFacade.merge(yunReleaseTemplet);
        return Response.status(Response.Status.OK).entity(releaseTemplet).build();

    }

    /**
     * 发布确认
     * @param relaseId
     * @return
     * @throws Exception
     */
    @POST
    @Transactional
    @Path("pub-confirm")
    public Response publishConfirmTemplateOrWork(@QueryParam("relaseId") String relaseId) throws Exception {

        YunReleaseTemplet yunReleaseTemplet = baseFacade.get(YunReleaseTemplet.class, relaseId);
        if(yunReleaseTemplet==null){
            throw  new Exception("没有获取到对应的信息");
        }
        yunReleaseTemplet.setModifyDate((Timestamp) new Date());
        yunReleaseTemplet.setHstatus("R");
        YunReleaseTemplet releaseTemplet = baseFacade.merge(yunReleaseTemplet);
        return Response.status(Response.Status.OK).entity(releaseTemplet).build();

    }

    /**
     * 备案文件添加病种和启动队列
     * @param yunReleaseTemplateVo
     * @return
     * @throws Exception
     */
    @POST
    @Transactional
    @Path("add-new-release")
    public Response mergeYunDiseaseTemplate(YunReleaseTemplateVo yunReleaseTemplateVo) throws Exception{
        YunReleaseTemplet yunReleaseTemplet = new YunReleaseTemplet();
        yunReleaseTemplet.setDcode(yunReleaseTemplateVo.getDcode());
        yunReleaseTemplet.setHstatus(yunReleaseTemplateVo.getHstatus());
        yunReleaseTemplet.setTitle(yunReleaseTemplateVo.getTitle());
        yunReleaseTemplet.setMblx(yunReleaseTemplateVo.getMblx());
        yunReleaseTemplet.setMbsj(JSONUtil.objectToJsonString(yunReleaseTemplateVo.getMbsj()));
        yunReleaseTemplet.setHversion(yunReleaseTemplateVo.getHversion());
        yunReleaseTemplet.setModifyDate(new Timestamp(new Date().getTime()));
        YunReleaseTemplet releaseTemplet = baseFacade.merge(yunReleaseTemplet);
        return  Response.status(Response.Status.OK).entity(releaseTemplet).build();
    }

    /**
     * 根据模板类型获取模板数据
     * @param mblx 模板类型
     * @return
     * @throws Exception
     */
    @GET
    @Path("list-yunrelease-template")
    public List<YunReleaseTemplet> getYunReleaseTemplate(@QueryParam("mblx")String mblx,@QueryParam("hstatus")String hstatus) throws Exception{
        String hql = "from YunReleaseTemplet as t where 1=1 " ;
        //所属疾病
        if(!"".equals(mblx) && mblx!=null){
            hql+=" and t.mblx='"+mblx+"'" ;
        }
        if(!"".equals(hstatus) && hstatus!=null){
            if("未通过".equals(hstatus)){
                hql +=" and t.hstatus = ' '";
            }else{
                hql +=" and t.hstatus = '"+hstatus+"'";
            }
        }
        List<YunReleaseTemplet> yunReleaseTemplets = baseFacade.createQuery(YunReleaseTemplet.class, hql, new ArrayList<Object>()).getResultList();
        return yunReleaseTemplets;
    }

    /**
     * 根据资源ID获取模板资源数据
     * @param id
     * @return
     * @throws Exception
     */
    @GET
    @Path("yun-release-data")
    public YunReleaseTemplet getYunReleaseTempletById(@QueryParam("id")String id) throws Exception{
        String hql = "from YunReleaseTemplet as t where 1=1 ";
        if(id!=null && !"".equals(id)){
            hql += " and t.id = '"+id+"'";
        }
        YunReleaseTemplet yunReleaseTemplet = baseFacade.createQuery(YunReleaseTemplet.class, hql, new ArrayList<Object>()).getSingleResult();
        return yunReleaseTemplet;
    }

    /**
     * 根据传入的资源Id和状态进行审核
     * @param yunReleaseTemplateVo
     * @return
     */
    @POST
    @Path("update")
    @Transactional
    public Response updateYunReleaseTemplet(YunReleaseTemplateVo yunReleaseTemplateVo){
        YunReleaseTemplet yunReleaseTemplet = baseFacade.get(YunReleaseTemplet.class, yunReleaseTemplateVo.getId());
        String hstatus = yunReleaseTemplateVo.getHstatus();
        if(hstatus!=null && !"".equals(hstatus)){
            yunReleaseTemplet.setHstatus(hstatus);
        }
        YunReleaseTemplet releaseTemplet = baseFacade.merge(yunReleaseTemplet);
        return Response.status(Response.Status.OK).entity(releaseTemplet).build();
    }
    /**
     * 根据传入的模板id删除备案信息
     * @param id
     * @return
     */
    @POST
    @Path("delete-template")
    @Transactional
    public Response deleteYunReleaseTemplate(@QueryParam("id") String id){
        YunReleaseTemplet yunReleaseTemplet = baseFacade.get(YunReleaseTemplet.class, id);
        baseFacade.remove(yunReleaseTemplet);
        return Response.status(Response.Status.OK).entity(yunReleaseTemplet).build();
    }
}
