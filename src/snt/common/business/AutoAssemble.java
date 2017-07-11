/**
 * 
 */
package snt.common.business;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * 自动装配声明<br>
 * 正确添加了自动装配声明的类型可以由通用数据访问接口自动从数据库中取得数据装配成该类型对象或者<br>
 * 将该类型对象自动更新或保存到数据库中去
 * 
 * @author <a href="mailto:sealinglip@gmail.com">Sealinglip</a>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AutoAssemble{
	
	/**
	 * 指定和该类型对应的数据库表名
	 * @return String
	 */
	String tableName();
	
	/**
	 * 指定主键字段名
	 * @return String[]
	 */
	String[] pkFld() default {"pk_id"};
	
	/**
	 * 指定该表主键是否自动生成（比如自增列）
	 * @return boolean
	 */
	boolean pkAutoGen() default false;
	
	/**
	 * 指定Pojo对象字段和表字段的对应关系<br>
	 * 如果取默认值，则使用默认对应关系，即名称完全匹配（不区分大小写）<br>
	 * 如果需要自定义对应关系，格式为prop1=fld1;prop2=fld2;...
	 * @return String
	 */
	String prop2FldMap() default "";
	
	/**
	 * 如果不是用主键是否为空来判断是否已经持久化，可以在version里指定一个属性字段<br>
	 * 作为对象的版本，必须是整数类型，默认version对应的字段为0表示对象是个临时对象，<br>
	 * 如果version为其它值，则表示已经持久化过了。
	 * @return String
	 */
	String version() default "";
}
