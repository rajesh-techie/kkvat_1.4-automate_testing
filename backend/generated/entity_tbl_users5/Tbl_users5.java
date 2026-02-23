package com.kkvat.automation.generated;

import jakarta.persistence.*;

@Entity
@Table(name="tbl_users5")
public class Tbl_users5 {

    private String username;
    @Id
    private String userlogin;
    private Double isactive;

    public Tbl_users5() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getUserlogin() { return userlogin; }
    public void setUserlogin(String userlogin) { this.userlogin = userlogin; }

    public Double getIsactive() { return isactive; }
    public void setIsactive(Double isactive) { this.isactive = isactive; }

    @Override
    public String toString() {
        return "Tbl_users5{ + "id=" + id + " , username=" + username + " , userlogin=" + userlogin + " , isactive=" + isactive + "}";
    }

}
