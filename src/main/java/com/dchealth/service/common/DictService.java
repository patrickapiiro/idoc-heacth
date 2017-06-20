package com.dchealth.service.common;

import com.dchealth.entity.DictType;
import com.dchealth.entity.YunDictitem;
import com.dchealth.entity.YunDicttype;
import com.dchealth.facade.common.BaseFacade;
import com.dchealth.util.IDUtils;
import org.eclipse.persistence.annotations.QueryRedirectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/6/16.
 */
@Controller
@Produces("application/json")
@Path("dict")
public class DictService {


    @Autowired
    private BaseFacade baseFacade;

    /**
     * 获取字典列表
     *
     * @param typeName 字典名称，如果为空或者不写则返回所有的值
     * @return
     */
    @GET
    @Path("list-type-by-name")
    public List<YunDicttype> listDictType(@QueryParam("typeName") String typeName) {
        String hql = "from YunDicttype where 1=1 ";
        if (typeName != null && !"".equals(typeName)) {
            hql += " and typeName like '%" + typeName + "%'";
        }
        return baseFacade.createQuery(YunDicttype.class, hql, new ArrayList<Object>()).getResultList();
}

    /**
     * 获取所有的字典项目
     *
     * @param typeId
     * @return
     */
    @GET
    @Path("list-item-by-type")
    public List<YunDictitem> listDictItemByTypeId(@QueryParam("typeId") String typeId) {
        String hql = "from YunDictitem as item where item.typeIdDm='" + typeId + "'";
        return baseFacade.createQuery(YunDictitem.class, hql, new ArrayList<Object>()).getResultList();
    }


    /**
     * 添加新的字典
     *
     * @param yunDicttype
     * @return
     */
    @Path("add-new-type")
    @POST
    @Transactional
    public Response addDictType(YunDicttype yunDicttype) {
        long id = IDUtils.genItemId();
        yunDicttype.setTypeId(id);
        YunDicttype dicttype = baseFacade.merge(yunDicttype);
        return Response.status(Response.Status.OK).entity(dicttype).build();
    }


    /**
     * 单个添加字典项目
     *
     * @param yunDictitem
     * @return
     */
    @POST
    @Transactional
    @Path("add-new-item")
    public Response addDictItem(YunDictitem yunDictitem) {
        if (yunDictitem.getSerialNo() == 0 || yunDictitem.getSerialNo() == null) {
            yunDictitem.setSerialNo(IDUtils.genItemId());
        }
        YunDictitem merge = baseFacade.merge(yunDictitem);
        return Response.status(Response.Status.OK).entity(merge).build();
    }

    /**
     * 添加字典项目
     * @param yunDictitems
     * @return
     */
    @POST
    @Transactional
    @Path("add-new-items")
    public Response addDictItmes(List<YunDictitem> yunDictitems) {
        for (YunDictitem item : yunDictitems) {
            if (item.getSerialNo() == 0 || item.getSerialNo() == null) {
                item.setSerialNo(IDUtils.genItemId());
            }
            baseFacade.merge(item);
        }
        return Response.status(Response.Status.OK).entity(yunDictitems).build();
    }


    /**
     * 删除某一下字典
     * @param serialNo
     * @return
     */
    @POST
    @Transactional
    @Path("del-item")
    public Response removeDictItem(@QueryParam("serialNo") String serialNo){
        List<Long> ids = new ArrayList<>() ;
        ids.add(Long.parseLong(serialNo));
        baseFacade.remove(YunDictitem.class,ids);
        return Response.status(Response.Status.OK).build();
    }

    /**
     * 删除所有的类别
     * @param typeId
     * @return
     */
    @Transactional
    @POST
    @Path("del-type")
    public Response removeDictType(@QueryParam("typeId")String typeId){

        String delItemHql = "delete YunDictitem as item where item.typeIdDm='"+typeId+"'" ;
        baseFacade.getEntityManager().createQuery(delItemHql).executeUpdate();
        String delHql = "delete YunDicttype as type where typeId='"+typeId+"'" ;
        baseFacade.getEntityManager().createQuery(delHql).executeUpdate();
        return Response.status(Response.Status.OK).build();
    }

}
