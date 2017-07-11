package com.lquan.Bean;

import java.io.Serializable;
import java.util.Date;

/**
 * 系统账户实体
 * @author liuchangqing
 * @date 2014-04-10
 */
public class User implements Serializable {
	private static final long serialVersionUID = -7710971914097595142L;
	
	/**账户主键*/
	private long pk_id;
	
	/**登录名*/
	private String log_name ;
	
	/**用户名**/
	private String user_name;
	
	/**性别**/
	private int gender;
	
	/**电话号码**/
	private String tel;
	
	/**手机号码**/
	private String mobliephone;
	
	/**传真**/
	private String fax;
	
	/**分公司**/
	private String company;
	
	/**邮政编码**/
	private String postalcode;
	
	/**状态**/
	private int stat;
	
	/**电子邮件**/
	private String email;
	
	/**入职日期**/
	private Date add_time;
	
	/**密码**/
	private String password;
	
	/**系统角色ID*/
	private String role;
	
	
	public long getPk_id() {
		return pk_id;
	}
	public void setPk_id(long pk_id) {
		this.pk_id = pk_id;
	}
	public String getLog_name() {
		return log_name;
	}
	public void setLog_name(String log_name) {
		this.log_name = log_name;
	}
	public String getUser_name() {
		return user_name;
	}
	public void setUser_name(String user_name) {
		this.user_name = user_name;
	}
	public int getGender() {
		return gender;
	}
	public void setGender(int gender) {
		this.gender = gender;
	}
	public String getTel() {
		return tel;
	}
	public void setTel(String tel) {
		this.tel = tel;
	}
	public String getMobliephone() {
		return mobliephone;
	}
	public void setMobliephone(String mobliephone) {
		this.mobliephone = mobliephone;
	}
	public String getFax() {
		return fax;
	}
	public void setFax(String fax) {
		this.fax = fax;
	}
	public String getCompany() {
		return company;
	}
	public void setCompany(String company) {
		this.company = company;
	}
	public String getPostalcode() {
		return postalcode;
	}
	public void setPostalcode(String postalcode) {
		this.postalcode = postalcode;
	}
	public int getStat() {
		return stat;
	}
	public void setStat(int stat) {
		this.stat = stat;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public Date getAdd_time() {
		return add_time;
	}
	public void setAdd_time(Date add_time) {
		this.add_time = add_time;
	}
	
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	
	public String getRole() {
		return role;
	}
	public void setRole(String role) {
		this.role = role;
	}
	
	
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
}

