/*
 * Created on 2004-10-29
 */
package haiyan.common.session;

import haiyan.common.annotation.GetMethod;
import haiyan.common.annotation.SetMethod;
import haiyan.common.intf.session.IRole;
import haiyan.common.intf.session.IUser;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author zhouxw
 */
public class User implements IUser, Serializable { // Externalizable
	private static final long serialVersionUID = 1L;
	private Map<String, Object> properties = new HashMap<String, Object>();
	private String ID;
	private String name;
	private String code;
	private String password;
	private String deptID;
	private String DSN;
	private IRole[] roles;
	private boolean alive;
	public User() {
	}
	User(String ID, String code, String name) {
		this.ID = ID;
		this.code = code;
		this.name = name;
	}
	@GetMethod("DSN")
	@Override
	public String getDSN() {
		return DSN;
	}
	@SetMethod("DSN")
	@Override
	public void setDSN(String dsn) {
		DSN = dsn;
	}
	@GetMethod("deptID")
	public String getDeptID() {
		return this.deptID;
	}
	@SetMethod("deptID")
	public void setDeptID(String deptID) {
		this.deptID = deptID;
	}
	@GetMethod("ID")
	@Override
	public String getId() {
		return this.ID;
	}
	@SetMethod("ID")
	@Override
	public void setId(String id) {
		this.ID = id;
	}
	@GetMethod("password")
	@Override
	public String getPassword() {
		return this.password;
	}
	@SetMethod("password")
	@Override
	public void setPassword(String passWord) {
		this.password = passWord;
	}
	@GetMethod("roles")
	@Override
	public IRole[] getRoles() {
		return this.roles;
	}
	@SetMethod("roles")
	@Override
	public void setRoles(IRole[] roles) {
		this.roles = roles;
	}
	@GetMethod("code")
	@Override
	public String getCode() {
		return this.code;
	}
	@SetMethod("code")
	@Override
	public void setCode(String code) {
		this.code = code;
	}
	@GetMethod("name")
	@Override
	public String getName() {
		return this.name;
	}
	@SetMethod("name")
	@Override
	public void setName(String name) {
		this.name = name;
	}
	private String languageName;
	@SetMethod("languageName")
	@Override
	public void setLanguageName(String languageName) {
		this.languageName = languageName;
	}
	@GetMethod("languageName")
	@Override
	public String getLanguageName() {
		return this.languageName;
	}
	public void setProperty(String key, Object value) {
		this.properties.put(key, value);
	}
	
	public Object getProperty(String key) {
		return this.properties.get(key);
	}
	@SetMethod("properties")
	public void setProperties(Map<String, Object> properties) {
		this.properties = properties;
	}
	@GetMethod("properties")
	public Map<?, ?> getProperties() {
		return this.properties;
	}
	@GetMethod("alive")
	@Override
	public boolean isAlive() {
		return alive;
	}
	@SetMethod("alive")
	@Override
	public void setAlive(boolean alive) {
		this.alive = alive;
	}
	@Override
	public void close() throws IOException {
	}

}