package com.lquan.Bean;

import java.io.Serializable;

public class Account implements Serializable{
	
	private static final long serialVersionUID = 1L;

	/**账户主键*/
	private long pk_id;
	
	/**登录名*/
	private String login_name ;
	
	/**用户名**/
	private String login_pwd;
	
	
	/**电话号码**/
	private String dealer_name;
	
	/**手机号码**/
	private String dealer_id;
	
	/**传真**/
	private String area_id;
	
	/**分公司**/
	private String role;
	
	/**邮政编码**/
	private String type;
	
	
	/**电子邮件**/
	private String emailname;


	public long getPk_id() {
		return pk_id;
	}


	public void setPk_id(long pk_id) {
		this.pk_id = pk_id;
	}


	public String getLogin_name() {
		return login_name;
	}


	public void setLogin_name(String login_name) {
		this.login_name = login_name;
	}


	public String getLogin_pwd() {
		return login_pwd;
	}


	public void setLogin_pwd(String login_pwd) {
		this.login_pwd = login_pwd;
	}


	public String getDealer_name() {
		return dealer_name;
	}


	public void setDealer_name(String dealer_name) {
		this.dealer_name = dealer_name;
	}


	public String getDealer_id() {
		return dealer_id;
	}


	public void setDealer_id(String dealer_id) {
		this.dealer_id = dealer_id;
	}


	public String getArea_id() {
		return area_id;
	}


	public void setArea_id(String area_id) {
		this.area_id = area_id;
	}


	public String getRole() {
		return role;
	}


	public void setRole(String role) {
		this.role = role;
	}


	public String getType() {
		return type;
	}


	public void setType(String type) {
		this.type = type;
	}


	public String getEmailname() {
		return emailname;
	}


	public void setEmailname(String emailname) {
		this.emailname = emailname;
	}


	@Override
	public String toString() {
		return "Account [pk_id=" + pk_id + ", login_name=" + login_name
				+ ", login_pwd=" + login_pwd + ", dealer_name=" + dealer_name
				+ ", dealer_id=" + dealer_id + ", area_id=" + area_id
				+ ", role=" + role + ", type=" + type + ", emailname="
				+ emailname + "]";
	}
	
}
