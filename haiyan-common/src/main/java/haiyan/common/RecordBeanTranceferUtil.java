package haiyan.common;

import haiyan.common.annotation.GetMethod;
import haiyan.common.annotation.SetMethod;
import haiyan.common.intf.database.orm.IDBRecord;
import haiyan.common.session.User;

import java.lang.reflect.Method;

/**
 * IDBRecord和普通的Bean互转的工具类，
 * 用注解的方式执行普通Bean的get\set方法
 * @author 商杰
 *
 */
public class RecordBeanTranceferUtil {
	
	public static IDBRecord bean2Record(Object obj,IDBRecord record) throws Throwable{
		Method[] methods = obj.getClass().getDeclaredMethods();
		for(Method method : methods){
			if(!method.isAnnotationPresent(GetMethod.class))
				continue;
			GetMethod getMethod = method.getAnnotation(GetMethod.class);
			String key = getMethod.value();
			Object value = null;
			try {
				value = method.invoke(obj);
			} catch (Throwable e) {
				throw e;
			}
			record.set(key, value);
		}
		return record;
	}
	
	
	public static Object record2Bean(IDBRecord record,Object obj) throws Throwable{
		Method[] methods = User.class.getDeclaredMethods();
		for(Method method : methods){
			if(!method.isAnnotationPresent(SetMethod.class))
				continue;
			SetMethod getMethod = method.getAnnotation(SetMethod.class);
			String key = getMethod.value();
			Object value = record.get(key);
			try {
				method.invoke(obj,value);
			} catch (Throwable e) {
				throw e;
			}
		}
		return obj;
	}
}
