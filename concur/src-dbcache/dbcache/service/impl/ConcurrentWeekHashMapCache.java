package dbcache.service.impl;

import java.io.Serializable;

import org.springframework.stereotype.Component;

import dbcache.ref.ConcurrentReferenceMap;
import dbcache.ref.ConcurrentReferenceMap.ReferenceKeyType;
import dbcache.ref.ConcurrentReferenceMap.ReferenceValueType;
import dbcache.service.Cache;

/**
 * ConcurrentWeekHashMap缓存容器
 * 
 * @author jake
 * @date 2014-8-1-下午8:30:34
 */
//@Component("concurrentWeekHashMapCache")
public class ConcurrentWeekHashMapCache implements Cache {

	/**
	 * 初始容量
	 */
	private static final int DEFAULT_CAPACITY_OF_ENTITY_CACHE = 5000;

	/**
	 * 空值的引用
	 */
	private static final ValueWrapper NULL_HOLDER = new NullHolder();

	/**
	 * 缓存容器
	 */
	private final ConcurrentReferenceMap<Object, ValueWrapper> store;

	/**
	 * 构造方法
	 */
	public ConcurrentWeekHashMapCache() {
		this(new ConcurrentReferenceMap<Object, ValueWrapper>(
				ReferenceKeyType.STRONG, ReferenceValueType.WEAK,
				DEFAULT_CAPACITY_OF_ENTITY_CACHE));
	}

	/**
	 * 构造方法
	 * 
	 * @param concurrentReferenceMap
	 *            弱引用Map
	 */
	public ConcurrentWeekHashMapCache(
			ConcurrentReferenceMap<Object, ValueWrapper> concurrentReferenceMap) {
		this.store = concurrentReferenceMap;
	}
	
	
	@Override
	public ValueWrapper get(Object key) {
		Object value = this.store.get(key);
		return (ValueWrapper) fromStoreValue(value);
	}

	@Override
	public void put(Object key, Object value) {
		this.store.put(key, toStoreValue(SimpleValueWrapper.valueOf(value)));
	}

	@Override
	public ValueWrapper putIfAbsent(String key, Object value) {
		return this.store.putIfAbsent(key, toStoreValue(SimpleValueWrapper.valueOf(value)));
	}

	@Override
	public void evict(Object key) {
		this.store.remove(key);
	}

	@Override
	public void clear() {
		this.store.clear();
	}
	
	
	/**
	 * Convert the given value from the internal store to a user value
	 * returned from the get method (adapting <code>null</code>).
	 * @param storeValue the store value
	 * @return the value to return to the user
	 */
	protected Object fromStoreValue(Object storeValue) {
		if (storeValue == NULL_HOLDER) {
			return null;
		}
		return storeValue;
	}

	/**
	 * Convert the given user value, as passed into the put method,
	 * to a value in the internal store (adapting <code>null</code>).
	 * @param userValue the given user value
	 * @return the value to store
	 */
	protected ValueWrapper toStoreValue(ValueWrapper userValue) {
		if (userValue == null) {
			return NULL_HOLDER;
		}
		return userValue;
	}
	

	@SuppressWarnings("serial")
	private static class NullHolder implements ValueWrapper, Serializable {

		@Override
		public Object get() {
			return null;
		}
	}

	/**
	 * 缓存Value简单包装
	 * 
	 * @author jake
	 * @date 2014-7-31-下午8:29:49
	 */
	public static class SimpleValueWrapper implements ValueWrapper {

		private final Object value;

		/**
		 * 构造方法
		 * 
		 * @param value
		 *            实体(可以为空)
		 */
		public SimpleValueWrapper(Object value) {
			this.value = value;
		}

		/**
		 * 获取实例
		 * 
		 * @param value
		 *            值
		 * @return
		 */
		public static SimpleValueWrapper valueOf(Object value) {
			if (value == null) {
				return null;
			}
			return new SimpleValueWrapper(value);
		}

		/**
		 * 获取实体
		 */
		public Object get() {
			return this.value;
		}

	}

}