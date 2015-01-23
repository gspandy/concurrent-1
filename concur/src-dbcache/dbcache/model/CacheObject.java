package dbcache.model;

import dbcache.conf.JsonConverter;
import dbcache.support.asm.ValueGetter;
import dbcache.utils.concurrent.LongAdder;

import java.io.Serializable;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * 实体缓存辅助类
 *
 * @author jake
 * @date 2014-7-31-下午8:18:03
 */
public class CacheObject<T extends IEntity<?>> {

	/**
	 * 缓存对象
	 */
	protected final T entity;

	/**
	 * 代理缓存对象
	 */
	protected final T proxyEntity;

	/**
	 * 主键id
	 */
	private final Serializable id;

	/**
	 * 修改版本号
	 */
	private final LongAdder editVersion = new LongAdder();

	/**
	 * 入库版本号
	 */
	private volatile long dbVersion = editVersion.intValue();
	
	/**
	 * 索引属性值获取器列表
	 */
	private Collection<ValueGetter<T>> indexList;

	/**
	 * Json 自动转换器
	 */
	private Collection<JsonConverter<T>> jsonConverters;
	
	/**
	 * 持久化状态
	 */
	private volatile PersistStatus persistStatus;

	/**
	 * 是否在更新处理中
	 */
	private AtomicBoolean updateProcessing = new AtomicBoolean(false);


	/**
	 * 默认构造方法
	 */
	protected CacheObject() {
		this.entity = null;
		this.id = null;
		this.proxyEntity = null;
	}

	/**
	 * 构造方法
	 *
	 * @param entity
	 *            实体
	 * @param id
	 *            主键
	 * @param clazz
	 *            类型
	 */
	public CacheObject(T entity, Serializable id, Class<T> clazz, T proxyEntity) {
		this(entity, id, clazz, proxyEntity, null, null);
	}

	/**
	 * 构造方法
	 *
	 * @param entity
	 *            实体
	 * @param id
	 *            主键
	 * @param clazz
	 *            类型
	 */
	public CacheObject(T entity, Serializable id, Class<T> clazz, T proxyEntity, Collection<ValueGetter<T>> indexes, Collection<JsonConverter<T>> jsonConverters) {
		this.entity = entity;
		this.id = id;
		this.proxyEntity = proxyEntity;
		this.persistStatus = PersistStatus.TRANSIENT;
		this.indexList = indexes;
		this.jsonConverters = jsonConverters;
		
		// 将进入持久化状态
		this.doBeforePersist();
	}

	/**
	 * 初始化
	 */
	public void doInit() {
		// 初始化json自动转换属性
		if(!this.jsonConverters.isEmpty()) {
			for(JsonConverter<T> jsonConverter : this.jsonConverters) {
				jsonConverter.doConvert(this.entity);
			}
		}
				
		// 加载回调
		this.doAfterLoad();
	}
	
	/**
	 * 调用加载回调
	 */
	public void doAfterLoad() {
		// 调用初始化
		if(entity instanceof EntityInitializer){
			EntityInitializer entityInitializer = (EntityInitializer) entity;
			entityInitializer.doAfterLoad();
		}
	}

	/**
	 * 持久化之前的操作
	 */
	public void doBeforePersist() {
		// json持久化
		if(!this.jsonConverters.isEmpty()) {
			for(JsonConverter<T> jsonConverter : this.jsonConverters) {
				jsonConverter.doPersist(this.entity);
			}
		}
		// 持久化前操作
		if(entity instanceof EntityInitializer){
			EntityInitializer entityInitializer = (EntityInitializer) entity;
			entityInitializer.doBeforePersist();
		}
	}


	/**
	 * 更新修改版本号
	 */
	public long increseEditVersion() {
		this.editVersion.increment();
		return this.editVersion.longValue();
	}

	public T getEntity() {
		return entity;
	}

	public T getProxyEntity() {
		return proxyEntity;
	}

	public Serializable getId() {
		return id;
	}

	public long getEditVersion() {
		return editVersion.longValue();
	}

	public PersistStatus getPersistStatus() {
		return persistStatus;
	}

	public void setPersistStatus(PersistStatus persistStatus) {
		this.persistStatus = persistStatus;
	}

	public Collection<ValueGetter<T>> getIndexList() {
		return indexList;
	}

	public boolean isUpdateProcessing() {
		return this.updateProcessing.get();
	}

	/**
	 * 改变状态
	 * @param processing 目标状态
	 * @return 是否改变成功
	 */
	public boolean swapUpdateProcessing(boolean processing) {
		return this.updateProcessing.compareAndSet(!processing, processing);
	}

	public long getDbVersion() {
		return dbVersion;
	}

	public void setDbVersion(long dbVersion) {
		this.dbVersion = dbVersion;
	}
}