package haiyan.orm.database;

import haiyan.cache.CacheUtil;
import haiyan.common.StringUtil;
import haiyan.common.config.DataConstant;
import haiyan.common.intf.database.orm.IDBRecord;
import haiyan.common.intf.database.orm.IDBRecordCacheManager;
import haiyan.config.castorgen.Table;
import haiyan.config.intf.database.orm.ITableRecordCacheManager;
import haiyan.config.intf.session.ITableDBContext;
import haiyan.config.util.ConfigUtil;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class DBRecordCacheFactory {

	private static final String CACHE_DEMI = "@";
	private DBRecordCacheFactory() {}
	private static ITableRecordCacheManager createRecordCacheManager() {
		//ITableRecordCacheManager cacheMgrDBSession = 
		return new ITableRecordCacheManager() { // 模拟了在内存中的ACID
			// 跟着DBSession走，DBSession既是IDBManager
			protected transient Map<String,IDBRecord> transaction = new HashMap<String,IDBRecord>(); // ConcurrentHashMap 
			@Override
			public void clearCache(String[] regTables) {
				if (!ConfigUtil.isORMUseCache())
					return;
				for (String realTable : regTables) {
					String[] linkedTables = ConfigUtil.getSameDBTableNamesByReal(realTable);
					for (String t : linkedTables) {
						CacheUtil.clearData(t);
					}
				}
			}
			@Override
			public void removeCache(ITableDBContext context, Table table, String[] ids, short type) throws Throwable {
				if (!ConfigUtil.isORMUseCache() || table.getName().toUpperCase().startsWith("V_"))
					return;
				// 要注意直接执行SQL语句的操作对缓冲数据的影响
				String DSN = context.getDSN();
				String tableName = table.getName();
				if (!StringUtil.isBlankOrNull(DSN))
					tableName += "." + DSN;
				for (String id : ids) {
					//CacheUtil.removeData(tableName, IDVal);
					this.transaction.put(tableName+CACHE_DEMI+id, null);
				}
				String[] linkedTables = ConfigUtil.getSameDBTableNames(table.getName());
				for (String linkedTable : linkedTables) {
					if (linkedTable.equalsIgnoreCase(table.getName()))
						continue;
					tableName = linkedTable;
					if (!StringUtil.isBlankOrNull(DSN))
						tableName += "." + DSN;
					for (String id : ids) {
						//CacheUtil.removeData(tableName, IDVal);
						this.transaction.put(tableName+CACHE_DEMI+id, null);
					}
				}
			}
			@Override
			public void updateCache(ITableDBContext context, Table table, IDBRecord record,
					short type) throws Throwable {
				if (!ConfigUtil.isORMUseCache() || table.getName().toUpperCase().startsWith("V_"))
					return;
				// 双缓冲：更新缓存后能提高DB->VO的转换效率
				// 要注意直接执行SQL语句的操作对缓冲数据的影响
				String DSN = context.getDSN();
				String tableName = table.getName();
				if (!StringUtil.isBlankOrNull(DSN))
					tableName += "." + DSN;
				String id = (String)record.get(table.getId().getName());
				if (type==IDBRecordCacheManager.DBSESSION) {
					// record.setDirty(); // 只要update或者insert过重取  
					// dirty后doEditOne才能取到最新的for executePlugin
					// NOTICE 但同一个事务中会把可能会回滚修改的记录reget放到trasaction里...
					transaction.put(tableName+CACHE_DEMI+id, record);
				} else { // load from db
					record.clearDirty();
					record.commit();
					CacheUtil.setData(tableName, id, record); // create
					// record.syncVersion();
					// 需要轮询不同配置拿到最新的版本号
					// 这样处理也可以必须在同一个配置中使用一个版本号
				}
				// CacheUtil.setData(tableName, id, record);
				// // 删掉吧,至少用businessobj能获取autonaming
				// CacheUtil.removeData(tableName, id);
				// TODO new Runnable()
				IDBRecord linkedRecord;
				String[] linkedTables = ConfigUtil.getSameDBTableNames(table.getName());
				for (String linkedTable : linkedTables) {
					if (linkedTable.equalsIgnoreCase(table.getName()))
						continue;
					tableName = linkedTable;
					if (!StringUtil.isBlankOrNull(DSN))
						tableName += "." + DSN;
					// // 删掉吧,至少用businessobj能获取autonaming
					// CachUtil.removeData(tableName, id);
					linkedRecord = (IDBRecord) CacheUtil.getData(tableName, id);
					// if (linkedRecord == null) {
					// linkedRecord = context.getDBM().createRecord();
					// record.copyTo(linkedRecord);
					// }
					if (linkedRecord != null) {
						String key;
						Iterator<String> iter = linkedRecord.keySet().iterator(); // 不同配置字段不同
						if (iter != null)
							while (iter.hasNext()) {
								key = (String) iter.next();
								if (record.contains(key))
									linkedRecord.set(key, record.get(key));
							}
						if (type==IDBRecordCacheManager.DBSESSION) {
							//linkedRecord.setDirty(); // 只要update或者insert过重取 
							// dirty后doEditOne才能取到最新的for executePlugin 
							// NOTICE 但同一个事务中是会把可能会回滚修改的记录reget放到trasaction里...
							//transaction.put(tableName+CACHE_DEMI+id, linkedRecord);
						    CacheUtil.removeLocalData(tableName, id); // remove最合适因为version变了
						} else { // load from db
							linkedRecord.clearDirty();
							linkedRecord.commit();
				            CacheUtil.setData(tableName, id, linkedRecord); // create
							// linkedRecord.syncVersion();
							// 需要轮询不同配置拿到最新的版本号
							// 这样处理也可以必须在同一个配置中使用一个版本号
						}
						// CacheUtil.setData(tableName, id, linkedRecord);
					}
				}
			}
			@Override
			public IDBRecord getCache(ITableDBContext context, Table table, String id, short type) throws Throwable {
				if (!ConfigUtil.isORMUseCache() || table.getName().toUpperCase().startsWith("V_"))
					return null;
				// 双缓冲：要注意直接执行SQL语句的操作对缓冲数据的影响
				String DSN = context.getDSN();
				String tableName = table.getName();
				if (!StringUtil.isBlankOrNull(DSN))
					tableName += "." + DSN;
				// String id = rs.getString(1); // 只做索引
				String k = tableName+CACHE_DEMI+id;
				if (type == IDBRecordCacheManager.DBSESSION)
					if (this.transaction.containsKey(k))
						return this.transaction.get(k);
				IDBRecord record = (IDBRecord) CacheUtil.getData(tableName, id);
				if (type == IDBRecordCacheManager.DBSESSION)
					this.transaction.put(k, record);
				return record;
			}
			@Override
			public void clear() {
				this.transaction.clear();
			}
			@Override
			public void commit() throws Throwable {
				Iterator<String> iter = this.transaction.keySet().iterator();
				String k;
				String[] arr;
				IDBRecord r;
				while(iter.hasNext()) {
					k = iter.next();
					arr = k.split(CACHE_DEMI);
					r = this.transaction.get(k);
					if (r==null) { // deleted
						CacheUtil.deleteData(arr[0], arr[1]);
					} else {
						//r.updateVersion(); // 不能加这里因为save完直接load生成客户端view了
						r.commit();
						//CacheUtil.setRemoteDirty(a[0], a[1]);
						r.setDirty(); // setDirty()+setCache()和removeCache()都會reget
						//CacheUtil.removeData(a[0], a[1]); // reget
						//update和insert设置setdirty后只用removeData
						r.remove(DataConstant.HYFORMKEY);
						CacheUtil.updateData(arr[0], arr[1], r);
					}
//					if ("SYSOPERATOR".equalsIgnoreCase(arr[0]) || "SYSORGA".equalsIgnoreCase(arr[0]))
//						RightUtil.clearOrgasOfUser(arr[1]);
				}
			}
			@Override
			public void rollback() throws Throwable {
				Iterator<String> iter = this.transaction.keySet().iterator();
				String k;
				String[] a;
				IDBRecord r;
				while(iter.hasNext()) {
					k = iter.next();
					a = k.split(CACHE_DEMI);
					r = this.transaction.get(k);
					if (r==null) { // deleted
					} else {
						//r.rollback();
						//CacheUtil.setRemoteDirty(a[0], a[1]);
						r.setDirty(); // setDirty()+setCache()和removeCache()都會reget
						//CacheUtil.removeData(a[0], a[1]); // reget
						//update和insert设置setdirty后只用removeData
						if (!r.rollback())
							CacheUtil.removeLocalData(a[0], a[1]);
						else {
			                r.remove(DataConstant.HYFORMKEY);
						    CacheUtil.setLocalData(a[0], a[1], r);
						}
					}
				}
			}
		};
	}
	public static ITableRecordCacheManager getCacheManager(short type) { 
		switch(type) {
		case IDBRecordCacheManager.DEFAULTSESSION:
			return null;
		case IDBRecordCacheManager.THREADSESSION:
		case IDBRecordCacheManager.DBSESSION:
			return createRecordCacheManager();
		case IDBRecordCacheManager.USESESSION:
			return null;
		case IDBRecordCacheManager.APPSESSION:
			return null;
		case IDBRecordCacheManager.WEBSESSION:
			return null;
		}
		return null;
	}

}
