package com.dchealth.entity.rare;

import javax.persistence.*;
import java.sql.Timestamp;

/**
 * Created by Administrator on 2017/7/4.
 */
@Entity
@Table(name = "yun_organ_number", schema = "emhbase", catalog = "")
public class YunOrganNumber {

    private String id;
    private String userId;
    private Timestamp modify_date;

    @Id
    @Column(name = "id")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Basic
    @Column(name = "userid")
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Basic
    @Column(name = "modify_date")
    public Timestamp getModify_date() {
        return modify_date;
    }

    public void setModify_date(Timestamp modify_date) {
        this.modify_date = modify_date;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (userId != null ? userId.hashCode() : 0);
        result = 31 * result + (modify_date != null ? modify_date.hashCode() : 0);
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        YunOrganNumber that = (YunOrganNumber)o;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (userId != null ? !userId.equals(that.userId) : that.userId != null) return false;
        if (modify_date != null ? !modify_date.equals(that.modify_date) : that.modify_date != null) return false;

        return true;
    }
}
