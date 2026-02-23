package com.kkvat.automation.generated;

import jakarta.persistence.*;

@Entity
@Table(name="tbl_users1")
public class Tbl_users1 {

    @Id
    private String userlogin;
    private String username;
    private Double isactive;

    public Tbl_users1() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUserlogin() { return userlogin; }
    public void setUserlogin(String userlogin) { this.userlogin = userlogin; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public Double getIsactive() { return isactive; }
    public void setIsactive(Double isactive) { this.isactive = isactive; }

    @Override
    public String toString() {
        return "Tbl_users1{ + "id=" + id + " , userlogin=" + userlogin + " , username=" + username + " , isactive=" + isactive + "}";
    }

}
