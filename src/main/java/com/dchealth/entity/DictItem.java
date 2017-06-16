package com.dchealth.entity;

import javax.persistence.*;

/**
 * Created by Administrator on 2017/6/16.
 */
@Entity
@Table(name = "dict_item", schema = "emhbase", catalog = "")
public class DictItem {
    private Long serialNo;
    private Long typeIdDm;
    private String itemCode;
    private String itemName;
    private String inputCode;

    @Id
    @Column(name = "serial_no")
    public Long getSerialNo() {
        return serialNo;
    }

    public void setSerialNo(Long serialNo) {
        this.serialNo = serialNo;
    }

    @Basic
    @Column(name = "type_id_dm")
    public Long getTypeIdDm() {
        return typeIdDm;
    }

    public void setTypeIdDm(Long typeIdDm) {
        this.typeIdDm = typeIdDm;
    }

    @Basic
    @Column(name = "item_code")
    public String getItemCode() {
        return itemCode;
    }

    public void setItemCode(String itemCode) {
        this.itemCode = itemCode;
    }

    @Basic
    @Column(name = "item_name")
    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    @Basic
    @Column(name = "input_code")
    public String getInputCode() {
        return inputCode;
    }

    public void setInputCode(String inputCode) {
        this.inputCode = inputCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DictItem dictItem = (DictItem) o;

        if (serialNo != null ? !serialNo.equals(dictItem.serialNo) : dictItem.serialNo != null) return false;
        if (typeIdDm != null ? !typeIdDm.equals(dictItem.typeIdDm) : dictItem.typeIdDm != null) return false;
        if (itemCode != null ? !itemCode.equals(dictItem.itemCode) : dictItem.itemCode != null) return false;
        if (itemName != null ? !itemName.equals(dictItem.itemName) : dictItem.itemName != null) return false;
        if (inputCode != null ? !inputCode.equals(dictItem.inputCode) : dictItem.inputCode != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = serialNo != null ? serialNo.hashCode() : 0;
        result = 31 * result + (typeIdDm != null ? typeIdDm.hashCode() : 0);
        result = 31 * result + (itemCode != null ? itemCode.hashCode() : 0);
        result = 31 * result + (itemName != null ? itemName.hashCode() : 0);
        result = 31 * result + (inputCode != null ? inputCode.hashCode() : 0);
        return result;
    }
}
