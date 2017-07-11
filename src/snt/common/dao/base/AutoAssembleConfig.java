/**
 * 
 */
package snt.common.dao.base;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanUtils;

import snt.common.business.AutoAssemble;

/**
 * 自动装配配置缓存对象
 * @author <a href="mailto:sealinglip@gmail.com">Sealinglip</a>
 */
public class AutoAssembleConfig {
	private static Log log = LogFactory.getLog(AutoAssembleConfig.class);
	static class Config{		
		String insertSql;
		String updateSql;
		String[] props;
		public Config(String insertSql, String updateSql, String[] props) {
			this.insertSql = insertSql;
			this.updateSql = updateSql;
			this.props = props;
		}
	}
	/**解析属性和表字段映射关系的正则表达式*/
	private static Pattern mapRegex = Pattern.compile("([^=;,]+)\\s*\\=\\s*([^=;,]+)\\s*[;,]?\\s*", Pattern.CANON_EQ);
	/**类和主键属性设置的对应关系*/
	private static Map<Class, PropertyDescriptor[]> cls2PkPropMap = new HashMap<Class, PropertyDescriptor[]>();
	/**类和版本属性的对应关系*/
	private static Map<Class, PropertyDescriptor> cls2VerPropMap = new HashMap<Class, PropertyDescriptor>();
	/**类和属性字段映射关系的对应*/
	private static Map<Class, List<String[]>> cls2PropFLdMapMap = new HashMap<Class, List<String[]>>();
	/**类和匹配得对应*/
	private static Map<Class, Config> cls2ConfigMap = new HashMap<Class, Config>();
	
	private static PropertyDescriptor[] getPkFldPropDesc(Class pojoCls){
		PropertyDescriptor[] propertyDescriptor = cls2PkPropMap.get(pojoCls);
		if (propertyDescriptor == null) {
			initClassInfo(pojoCls);
			propertyDescriptor = cls2PkPropMap.get(pojoCls);
		}
		return propertyDescriptor;
	}
	
	static PropertyDescriptor getVerPropDesc(Class pojoCls){
		return cls2VerPropMap.get(pojoCls);		
	}
	
	static void updateVersion(Object pojo){
		PropertyDescriptor verDesc = getVerPropDesc(pojo.getClass());
		if (verDesc != null) {
			try {
				verDesc.getWriteMethod().invoke(pojo, new Object[]{new Integer(1)});
			} catch (Exception e) {
				log.error("更新对象的版本信息出错！", e);
			}
		}
	}
	
	static void updateVersion(List<?> pojoList){
		if (pojoList == null || pojoList.size() == 0) {
			return;
		}
		Object pojo = pojoList.get(0);
		PropertyDescriptor verDesc = getVerPropDesc(pojo.getClass());
		if (verDesc != null) {
			try {
				Object params = new Object[]{new Integer(1)}; 
				Method writedMethod = verDesc.getWriteMethod();
				for (Object object : pojoList) {
					writedMethod.invoke(object, params);
				}
			} catch (Exception e) {
				log.error("更新对象的版本信息出错！", e);
			}
		}
	}
	
	static List<String[]> getPropFldMap(Class pojoCls){
		List<String[]> propFLdMap = cls2PropFLdMapMap.get(pojoCls);
		if (propFLdMap == null) {
			initClassInfo(pojoCls);
			propFLdMap = cls2PropFLdMapMap.get(pojoCls);
		}
		return propFLdMap;
	}
	
	private static Config getConfig(Class pojoCls){
		Config config = cls2ConfigMap.get(pojoCls);
		if (config == null) {
			initClassInfo(pojoCls);
			config = cls2ConfigMap.get(pojoCls);
		}
		return config;
	}
	
	@SuppressWarnings("unchecked")
	private static void initClassInfo(Class pojoCls){
		AutoAssemble autoAssemble = (AutoAssemble)pojoCls.getAnnotation(AutoAssemble.class);
		if(autoAssemble == null){
			throw new IllegalArgumentException(pojoCls+"必须带有AutoAssemble声明！");
		}
		PropertyDescriptor[] propDescs = BeanUtils.getPropertyDescriptors(pojoCls);
		String verProp = autoAssemble.version();
		if (verProp.length()>0) {
			cls2VerPropMap.put(pojoCls, BeanUtils.getPropertyDescriptor(pojoCls, verProp));
		}
		String[] pkFlds = autoAssemble.pkFld();
		Set<String> pkFldSet = new HashSet<String>();
		pkFldSet.addAll(Arrays.asList(pkFlds));		
		List<PropertyDescriptor> pkPropDescList = new ArrayList<PropertyDescriptor>(pkFlds.length);
		
		String[][] pkProp2Fld = new String[pkFlds.length][];
		int pkIndex = 0;
		List<String[]> prop2FldMap = new ArrayList<String[]>();
		if(autoAssemble.prop2FldMap().length()>0){//设置了对应关系
			Matcher regexMatcher = mapRegex.matcher(autoAssemble.prop2FldMap());
			while (regexMatcher.find()) {
				String key = regexMatcher.group(1);
				String value= regexMatcher.group(2);
				if(pkFldSet.contains(value)){
					pkPropDescList.add(BeanUtils.getPropertyDescriptor(pojoCls, key));
					pkProp2Fld[pkIndex++] = new String[]{key, value};
					continue;
				}
				
				prop2FldMap.add(new String[]{key, value});				
			}
		}
		else{//采用默认的对应关系
			for (PropertyDescriptor descriptor : propDescs) {
				if(pkFldSet.contains(descriptor.getName())){
					pkPropDescList.add(descriptor);
					pkProp2Fld[pkIndex++] = new String[]{descriptor.getName(), descriptor.getName()};
					continue;
				}
				prop2FldMap.add(new String[]{descriptor.getName(), descriptor.getName()});				
			}
		}
		//修正主键属性－字段映射关系
		String[][] newPkProp2Fld = new String[pkIndex][];
		if (pkIndex>0) {
			System.arraycopy(pkProp2Fld, 0, newPkProp2Fld, 0, pkIndex);
		}
		pkProp2Fld = newPkProp2Fld;
		if(pkProp2Fld.length>0){
			for (String[] strings : pkProp2Fld) {
				prop2FldMap.add(strings);//把主键字段放到最后
			}			
		}
		cls2PropFLdMapMap.put(pojoCls, prop2FldMap);
		StringBuffer insertBuf = new StringBuffer(100);
		StringBuffer paramBuf = new StringBuffer();
		StringBuffer updateBuf = new StringBuffer(200);
		insertBuf.append("insert into ").append(autoAssemble.tableName()).append(" (");
		updateBuf.append("update ").append(autoAssemble.tableName()).append(" set ");
		String[] props = new String[prop2FldMap.size()];
		for (int i = 0, in =props.length-pkProp2Fld.length; i < in; i++) {			
			if(i>0){
				insertBuf.append(",");
				paramBuf.append(",");
				updateBuf.append(",");				
			}
			String[] prop2Fld = prop2FldMap.get(i);
			props[i] = prop2Fld[0];
			insertBuf.append(prop2Fld[1]);
			paramBuf.append("?");
			updateBuf.append(prop2Fld[1]).append("=?");
		}
		for (int i = 0, in=pkProp2Fld.length; i < in; i++) {
			props[props.length-in+i] = pkProp2Fld[i][0];
		}
		
		if(!autoAssemble.pkAutoGen()){
			for (int i = 0, in=pkProp2Fld.length; i < in; i++) {
				insertBuf.append(",").append(pkProp2Fld[i][1]);
			}			
		}
		insertBuf.append(") values (").append(paramBuf.toString());
		if(!autoAssemble.pkAutoGen()){
			insertBuf.append(",?");
		}
		insertBuf.append(")");
		if(pkProp2Fld.length>0){
			updateBuf.append(" where ");
			for (int i = 0, in=pkProp2Fld.length; i < in; i++) {
				if(i>0){
					updateBuf.append(" and ");
				}
				updateBuf.append(pkProp2Fld[i][1]).append("=?");
			}
		}
		
		cls2ConfigMap.put(pojoCls, new Config(insertBuf.toString(), updateBuf.toString(), props));
		cls2PkPropMap.put(pojoCls, pkPropDescList.toArray(new PropertyDescriptor[pkPropDescList.size()]));
	}
	
	static Object[] getProps(Object pojo, String[] props, int length) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException{
		Object[] objs = new Object[length];
		int index = 0;
		Class pojoClass = pojo.getClass();
		for (int i=0; i<length; i++) {
			String prop = props[i];
			PropertyDescriptor propDesc = BeanUtils.getPropertyDescriptor(pojoClass, prop);			
			objs[index++] = propDesc.getReadMethod()!=null?propDesc.getReadMethod().invoke(pojo):null;
		}
		return objs;
	}
	
	@SuppressWarnings("unchecked")
	static Object[] prepare4Persistence(Object pojo) throws AutoAssembleException{
		try {
			Class pojoClass = pojo.getClass();			
			PropertyDescriptor[] pkDescs = getPkFldPropDesc(pojoClass);
			PropertyDescriptor verDesc = getVerPropDesc(pojoClass);
			AutoAssemble autoAssemble = (AutoAssemble)pojoClass.getAnnotation(AutoAssemble.class);
			//记录主键是否为null
			boolean pkIsNull = false;
			Object[] pk = new Object[pkDescs.length];
			for (int i = 0; i < pk.length; i++) {
				pk[i] = pkDescs[i].getReadMethod().invoke(pojo);
				if(pk[i] == null){
					pkIsNull = true;
				}
				else if(verDesc == null && pk[i] instanceof Number
						&& ((Number)pk[i]).intValue()==0){
					pkIsNull = true;
				}
			}
			boolean insert = false;
			//版本属性的值为0，表明应该插入
			if(pkDescs.length>0 && verDesc != null && ((Number)verDesc.getReadMethod().invoke(pojo)).intValue()==0 ){
				insert = true;
			}
			if (pkIsNull) {
				insert = true;
				if (!autoAssemble.pkAutoGen()) {
					for(int i = 0; i < pk.length; i++){
						Class pkType = pkDescs[i].getPropertyType();
						if (Integer.class.equals(pkType) || int.class.equals(pkType)) {
							pk[i] = PrimaryKeyGenerator.getIntKey();
						} else if (Long.class.equals(pkType) || long.class.equals(pkType)){
							pk[i] = PrimaryKeyGenerator.getLongKey();
						} else {//默认情况当成字符类型
							pk[i] = Long.toHexString(PrimaryKeyGenerator.getLongKey());
						}
						pkDescs[i].getWriteMethod().invoke(pojo, new Object[]{pk[i]});
					}
				}
			}
			Config config = getConfig(pojoClass);
			String sql = insert?config.insertSql:config.updateSql;
			int length = config.props.length;
			if(insert && autoAssemble.pkAutoGen()){
				length -= 1;
			}
			Object[] args = getProps(pojo, config.props, length);
			return new Object[]{sql, args};
		} catch (Exception e) {
			throw new AutoAssembleException(e);
		} 
	}
	
	@SuppressWarnings("unchecked")
	static Object[] prepare4Persistence(List pojoList) throws AutoAssembleException{
		try {
			Object pojo = pojoList.get(0);
			Class pojoClass = pojo.getClass();			
			PropertyDescriptor[] pkDescs = getPkFldPropDesc(pojoClass);
			PropertyDescriptor verDesc = getVerPropDesc(pojoClass);
			AutoAssemble autoAssemble = (AutoAssemble)pojoClass.getAnnotation(AutoAssemble.class);
			//记录主键是否为null
			boolean pkIsNull = false;
			Object[] pk = new Object[pkDescs.length];
			for (int i = 0; i < pk.length; i++) {
				pk[i] = pkDescs[i].getReadMethod().invoke(pojo);
				if(pk[i] == null){
					pkIsNull = true;
				}
				else if(verDesc == null && pk[i] instanceof Number
						&& ((Number)pk[i]).intValue()==0){
					pkIsNull = true;
				}
			}
			boolean insert = false;
			
			if(pkDescs.length>0 && verDesc != null && ((Number)verDesc.getReadMethod().invoke(pojo)).intValue()==0 ){
				insert = true;
			}
			//程序生成主键类型：0，不必越俎代庖；1，生成整型主键；2，生成长整型主键；3，生成字符型主键
			int[] manualGenPk = new int[pkDescs.length];
			Arrays.fill(manualGenPk, 0);
			if (pkIsNull) {
				insert = true;
				if (!autoAssemble.pkAutoGen()) {
					for(int i = 0; i < manualGenPk.length; i++){
						Class pkType = pkDescs[i].getPropertyType();
						if (Integer.class.equals(pkType) || int.class.equals(pkType)) {
							manualGenPk[i] = 1;
						} else if(Long.class.equals(pkType) || long.class.equals(pkType)){
							manualGenPk[i] = 2;
						} else{
							manualGenPk[i] = 3;
						}
					}
				}
			}
			
			Config config = getConfig(pojoClass);
			int length = config.props.length;
			if(insert && autoAssemble.pkAutoGen()){
				length -= 1;
			}
			String sql = insert?config.insertSql:config.updateSql;
			List<Object[]> argsList = new ArrayList<Object[]>(pojoList.size());
			for (Object object : pojoList) {
				for(int i = 0; i < manualGenPk.length; i++){
					if (manualGenPk[i] > 0) {
						Object genPk = null;
						if (manualGenPk[i] == 1) {
							genPk = PrimaryKeyGenerator.getIntKey();
						} else if(manualGenPk[i] == 2){
							genPk = PrimaryKeyGenerator.getLongKey();
						} else {
							genPk = Long.toHexString(PrimaryKeyGenerator.getLongKey());
						}
						pkDescs[i].getWriteMethod().invoke(object, new Object[]{genPk});
					}
				}
				argsList.add(getProps(object, config.props, length));
			}
			return new Object[]{sql, argsList};
		} catch (Throwable e) {
			throw new AutoAssembleException(e);
		} 
	}
}
