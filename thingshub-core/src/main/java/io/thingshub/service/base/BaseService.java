package io.thingshub.service.base;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import javax.cache.event.CacheEntryEvent;
import javax.cache.event.CacheEntryListenerException;
import javax.cache.event.CacheEntryUpdatedListener;
import javax.cache.event.EventType;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteAtomicSequence;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.cache.CacheAtomicityMode;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.cache.CacheRebalanceMode;
import org.apache.ignite.cache.CacheWriteSynchronizationMode;
import org.apache.ignite.cache.PartitionLossPolicy;
import org.apache.ignite.cache.query.ContinuousQuery;
import org.apache.ignite.cache.query.QueryCursor;
import org.apache.ignite.cache.query.SqlFieldsQuery;
import org.apache.ignite.cache.query.annotations.QuerySqlField;
import org.apache.ignite.cache.store.jdbc.JdbcTypeField;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.DataStorageConfiguration;

import com.google.common.base.CaseFormat;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.db.Entity;
import cn.hutool.db.sql.Condition;
import cn.hutool.db.sql.Condition.LikeType;
import cn.hutool.db.sql.SqlBuilder;
import io.thingshub.commons.Page;
import io.thingshub.commons.ServiceException;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * <p>
 * 服务基类
 * </p>
 *
 * @author albert pi
 * @since 1.0.0
 */

public abstract class BaseService<E> {

	private final Map<String, Field> colFieldMapping = new HashMap<>();

	private final List<String> cols = new ArrayList<>();

	private final List<JdbcTypeField> jdbcFields = new ArrayList<>();

	private String keyProperty;

	private Field keyField;

	private String keyCol;

	@Inject
	private Ignite ignite;

	private IgniteAtomicSequence sequence;

	private IgniteCache<Long, E> cache;

	private Class<E> eClazz;

	private String cacheName;

	public static final DataRegion DEFAULT_DATA_REGION = new DataRegion() {

		@Override
		public Class<? extends Annotation> annotationType() {
			return DataRegion.class;
		}

		@Override
		public String name() {
			return DataStorageConfiguration.DFLT_DATA_REG_DEFAULT_NAME;
		}

		@Override
		public long initSize() {
			return DataStorageConfiguration.DFLT_DATA_REGION_INITIAL_SIZE;
		}

		@Override
		public long maxSize() {
			return DataStorageConfiguration.DFLT_DATA_REGION_MAX_SIZE;
		}

		@Override
		public boolean persistent() {
			return true;
		}

		@Override
		public boolean local() {
			return true;
		}

	};

	public static enum DeletedStatus {
		NOT_DELETED(0), DELETED(1);

		@Accessors(fluent = true)
		@Getter
		private int value;

		DeletedStatus(int value) {
			this.value = value;
		}

	};

	public static enum AvailableStatus {
		NORMAL(0), DISABLED(1);

		@Accessors(fluent = true)
		@Getter
		private int value;

		AvailableStatus(int value) {
			this.value = value;
		}

	};

	@SuppressWarnings("unchecked")
	@PostConstruct
	protected void init() {
		Type[] types = ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments();
		this.eClazz = (Class<E>) types[0];
		this.cacheName = this.eClazz.getSimpleName();

		Field[] _fields = this.eClazz.getDeclaredFields();
		for (Field field : _fields) {
			if (field.getName().equals("id") && this.keyField == null) {
				this.keyProperty = field.getName();
				this.keyField = field;
				this.keyCol = "id";
			}

			if (field.isAnnotationPresent(Key.class)) {
				this.keyProperty = field.getName();
				this.keyField = field;
				this.keyCol = field.getName();
			}

			if (field.isAnnotationPresent(QuerySqlField.class)) {
				QuerySqlField theAnnotation = field.getAnnotation(QuerySqlField.class);
				String colName = StrUtil.isNotBlank(theAnnotation.name()) ? theAnnotation.name() : field.getName();
				this.colFieldMapping.put(colName, field);
				this.cols.add(colName);

				if (field.getType() == Long.class) {
					jdbcFields.add(new JdbcTypeField(java.sql.Types.BIGINT, colName, Long.class, field.getName()));
				} else if (field.getType() == String.class) {
					jdbcFields.add(new JdbcTypeField(java.sql.Types.VARCHAR, colName, String.class, field.getName()));
				} else if (field.getType() == Date.class) {
					jdbcFields.add(new JdbcTypeField(java.sql.Types.TIMESTAMP, colName, Date.class, field.getName()));
				} else if (field.getType() == BigDecimal.class) {
					jdbcFields.add(new JdbcTypeField(java.sql.Types.DECIMAL, colName, BigDecimal.class, field.getName()));
				} else if (field.getType() == Byte.class) {
					jdbcFields.add(new JdbcTypeField(java.sql.Types.TINYINT, colName, Byte.class, field.getName()));
				} else if (field.getType() == Short.class) {
					jdbcFields.add(new JdbcTypeField(java.sql.Types.SMALLINT, colName, Short.class, field.getName()));
				} else if (field.getType() == Integer.class) {
					jdbcFields.add(new JdbcTypeField(java.sql.Types.INTEGER, colName, Integer.class, field.getName()));
				}
			}
		}

		initCache();
	}

	public void initCache() {
		DataRegion dataRegion = this.eClazz.getAnnotation(DataRegion.class);
		if (dataRegion == null) {
			dataRegion = DEFAULT_DATA_REGION;
		}

		CacheConfiguration<Long, E> cacheConfiguration = new CacheConfiguration<Long, E>() //
				.setSqlSchema("THINGSHUB") //
				.setName(cacheName) //
				.setCacheMode(dataRegion.local() ? CacheMode.REPLICATED : CacheMode.PARTITIONED) //
				.setDataRegionName(dataRegion.name()) //
				.setAtomicityMode(CacheAtomicityMode.TRANSACTIONAL) //
				.setWriteSynchronizationMode(CacheWriteSynchronizationMode.FULL_SYNC) //
				.setBackups(1) //
//				.setGroupName("meta_data_group")//节点数量较多时使用
				.setEagerTtl(true) //
				.setPartitionLossPolicy(PartitionLossPolicy.READ_ONLY_SAFE) //
				.setIndexedTypes(new Class[] { Long.class, this.eClazz }) // TODO
//				.setExpiryPolicyFactory(factory) // 
				.setRebalanceMode(CacheRebalanceMode.ASYNC);

		this.cache = this.ignite.getOrCreateCache(cacheConfiguration);
//		this.cache.loadCache(null);// TODO 数据量超大情况下加载？？？

		Long maxId = 0L;
		SqlFieldsQuery sql = new SqlFieldsQuery(StrUtil.format("SELECT MAX({}) FROM {}", this.keyProperty, cacheName));
		try (QueryCursor<List<?>> cursor = this.cache.query(sql)) {
			for (List<?> row : cursor) {
				if (row.get(0) != null) {
					maxId = (Long) row.get(0);
				}
			}
		}
		this.sequence = this.ignite.atomicSequence(CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_HYPHEN, cacheName) + "-seq", maxId, true);
	}

	public void listen(BiConsumer<EventType, E> callback) {
		ContinuousQuery<Long, E> qry = new ContinuousQuery<>();
		qry.setLocalListener(new CacheEntryUpdatedListener<Long, E>() {

			@Override
			public void onUpdated(Iterable<CacheEntryEvent<? extends Long, ? extends E>> events) throws CacheEntryListenerException {
				for (CacheEntryEvent<? extends Long, ? extends E> e : events) {
					callback.accept(e.getEventType(), e.getValue());
				}
			}

		});

		this.cache.query(qry);
	}

	private Long nextId() {
		return this.sequence.incrementAndGet();
	}

	private E buildEntity(List<?> row) {
		try {
			E entity = eClazz.newInstance();

			for (int i = 0; i < this.cols.size(); i++) {
				Field field = this.colFieldMapping.get(this.cols.get(i));

				field.setAccessible(true);
				field.set(entity, row.get(i));
			}

			return entity;
		} catch (InstantiationException | IllegalAccessException e) {
			throw new ServiceException(e);
		}
	}

	public void save(E entity) {
		try {
			this.keyField.setAccessible(true);
			Long key = (Long) this.keyField.get(entity);
			if (key == null) {
				key = this.nextId();
				this.keyField.set(entity, key);
			}
			this.cache.put(key, entity);
		} catch (Exception e) {
			throw new ServiceException(e);
		}
	}

	public void saveBatch(Collection<E> entities) {
		this.keyField.setAccessible(true);

		try {
			for (E entity : entities) {
				Long key = (Long) this.keyField.get(entity);
				if (key == null) {
					key = this.nextId();
					this.keyField.set(entity, key);
				}
				this.cache.put(key, entity);
			}
		} catch (Exception e) {
			throw new ServiceException(e);
		}
	}

	public void updateById(E entity) {
		try {
			this.keyField.setAccessible(true);
			Long key = (Long) this.keyField.get(entity);
			this.cache.put(key, entity);
		} catch (Exception e) {
			throw new ServiceException(e);
		}
	}

	public void updateByConditions(E updatedObject, List<Condition> conditions) {
		List<Object> paramValues = new ArrayList<>();
		Entity entity = Entity.create().setTableName(cacheName);

		for (int i = 0; i < this.cols.size(); i++) {
			String colName = this.cols.get(i);
			Field field = this.colFieldMapping.get(colName);

			field.setAccessible(true);
			try {
				Object setVal = field.get(updatedObject);
				if (setVal != null) {
					entity.set(colName, setVal);
					paramValues.add(setVal);
				}
			} catch (IllegalArgumentException | IllegalAccessException e) {
				throw new ServiceException(e);
			}
		}

		SqlBuilder updateSqlBuilder = SqlBuilder.create().update(entity).where(conditions.toArray(new Condition[0]));
		paramValues.addAll(conditions.stream().filter(cond -> cond.getValue() != null).map(cond -> cond.getValue()).collect(Collectors.toList()));
		SqlFieldsQuery updateQuery = new SqlFieldsQuery(updateSqlBuilder.build()).setArgs(paramValues.toArray());

		this.cache.query(updateQuery).getAll();
	}

	public E getById(Long id) {
		return this.cache.get(id);
	}

	public E getOne(List<Condition> conditions) {
		SqlBuilder selectSqlBuilder = SqlBuilder.create().select(this.cols.toArray(new String[0])).from(cacheName).where(conditions.toArray(new Condition[0]));
		SqlFieldsQuery selectQuery = new SqlFieldsQuery(selectSqlBuilder.build()).setArgs(selectSqlBuilder.getParamValueArray());

		E entity = null;
		try (QueryCursor<List<?>> cursor = this.cache.query(selectQuery)) {
			int i = 0;
			for (List<?> row : cursor) {
				if (i > 1) {
					throw new ServiceException("One record is expected, but query result has multiple records");
				}

				entity = buildEntity(row);
				i++;
			}
		}

		return entity;
	}

	public List<E> list() {
		SqlBuilder selectSqlBuilder = SqlBuilder.create().select(this.cols.toArray(new String[0])).from(cacheName);
		SqlFieldsQuery selectQuery = new SqlFieldsQuery(selectSqlBuilder.build());

		List<E> entities = null;
		try (QueryCursor<List<?>> cursor = this.cache.query(selectQuery)) {
			entities = new ArrayList<>();
			for (List<?> row : cursor) {
				E entity = buildEntity(row);

				entities.add(entity);
			}
		}

		return entities;
	}

	public List<E> query(List<Condition> conditions) {
		SqlBuilder selectSqlBuilder = SqlBuilder.create().select(this.cols.toArray(new String[0])).from(cacheName).where(conditions.toArray(new Condition[0]));
		SqlFieldsQuery selectQuery = new SqlFieldsQuery(selectSqlBuilder.build()).setArgs(selectSqlBuilder.getParamValueArray());

		List<E> entities = null;
		try (QueryCursor<List<?>> cursor = this.cache.query(selectQuery)) {
			entities = new ArrayList<>();
			for (List<?> row : cursor) {
				E entity = buildEntity(row);

				entities.add(entity);
			}
		}

		return entities;
	}

	public static void main(String[] args) {
		Condition equalCond = new Condition("sn", "c1");
		Condition grpCond = new Condition("group", null);
		Condition neCond = new Condition("status", "!=", 1);
		Condition likeCond = new Condition("title", "test", LikeType.StartWith);
		Condition inCond = new Condition("type", "in", new int[] { 1, 2 });
		Condition geCond = new Condition("create_time", ">=", DateUtil.beginOfDay(DateUtil.date()));
		Condition leCond = new Condition("create_time", "<=", DateUtil.endOfDay(DateUtil.date()));
		Condition btwCond = new Condition("lng", "BETWEEN", new double[] { 96.09, 119.80 });
		List<Condition> conds = new ArrayList<>();
		conds.add(equalCond);
		conds.add(grpCond);
		conds.add(neCond);
		conds.add(likeCond);
		conds.add(inCond);
		conds.add(geCond);
		conds.add(leCond);
		conds.add(btwCond);

		String sql = SqlBuilder.create().select().from("device").where(conds.toArray(new Condition[0])).groupBy("type").build();

		System.out.println("sql=========================" + sql);

//		Device device = new Device();
//		device.setActiveState(1);
//		device.setActivateTime(DateUtil.date());
//		device.setCreateBy("admin");
//
//		List<Condition> conditions = Lists.newArrayList(new Condition("sn", "123"));
//
//		Entity dataEntity = Entity.create().setTableName("device");
//		Field[] _fields = Device.class.getDeclaredFields();
//		for (int i = 0; i < _fields.length; i++) {
//			Field field = _fields[i];
//
//			field.setAccessible(true);
//			try {
//				if (field.get(device) != null)
//					dataEntity.set(field.getName(), field.get(device));
//			} catch (IllegalArgumentException | IllegalAccessException e) {
//				throw new ServcieException(e);
//			}
//		}
//
//		SqlBuilder updateSqlBuilder = SqlBuilder.create().update(dataEntity).where(conditions.toArray(new Condition[0]));
//
//		System.out.println("update sql==================" + updateSqlBuilder.build());
	}

	public int count(List<Condition> conditions) {
		SqlBuilder countSqlBuilder = SqlBuilder.create().select("count(*)").from(cacheName).where(conditions.toArray(new Condition[0]));
		SqlFieldsQuery countQuery = new SqlFieldsQuery(countSqlBuilder.build()).setArgs(countSqlBuilder.getParamValueArray());

		int total = 0;
		try (QueryCursor<List<?>> cursor = this.cache.query(countQuery)) {
			for (List<?> row : cursor) {
				if (row.get(0) != null) {
					Long sum = (Long) row.get(0);
					total = sum.intValue();
				}
			}
		}

		return total;
	}

	public Page<E> query(List<Condition> conditions, Long startId, int size) {
		SqlBuilder countSqlBuilder = SqlBuilder.create().select("count(*)").from(cacheName).where(conditions.toArray(new Condition[0]));
		SqlFieldsQuery countQuery = new SqlFieldsQuery(countSqlBuilder.build()).setArgs(countSqlBuilder.getParamValueArray());

		int total = 0;
		try (QueryCursor<List<?>> cursor = this.cache.query(countQuery)) {
			for (List<?> row : cursor) {
				if (row.get(0) != null) {
					Long sum = (Long) row.get(0);
					total = sum.intValue();
				}
			}
		}

		conditions.add(new Condition(this.keyCol, ">", startId));
		SqlBuilder selectSqlBuilder = SqlBuilder.create().select(this.cols.toArray(new String[0])).from(cacheName).where(conditions.toArray(new Condition[0]));
		SqlFieldsQuery selectQuery = new SqlFieldsQuery(selectSqlBuilder.append(" LIMIT ").append(size).toString())
				.setArgs(selectSqlBuilder.getParamValueArray());

		List<E> entities = null;
		int curPageSize = 0;
		try (QueryCursor<List<?>> cursor = this.cache.query(selectQuery)) {
			entities = new ArrayList<>();
			for (List<?> row : cursor) {
				E entity = buildEntity(row);

				entities.add(entity);
				curPageSize++;
			}
		}

		return Page.of(-1, curPageSize, total, entities);// TODO current page????
	}

	public Page<E> query(List<Condition> conditions, int page, int size) {
		SqlBuilder countSqlBuilder = SqlBuilder.create().select("count(*)").from(cacheName).where(conditions.toArray(new Condition[0]));
		SqlFieldsQuery countQuery = new SqlFieldsQuery(countSqlBuilder.build()).setArgs(countSqlBuilder.getParamValueArray());

		int total = 0;
		try (QueryCursor<List<?>> cursor = this.cache.query(countQuery)) {
			for (List<?> row : cursor) {
				if (row.get(0) != null) {
					Long sum = (Long) row.get(0);
					total = sum.intValue();
				}
			}
		}

		SqlBuilder selectSqlBuilder = SqlBuilder.create().select(this.cols.toArray(new String[0])).from(cacheName).where(conditions.toArray(new Condition[0]));
		SqlFieldsQuery selectQuery = new SqlFieldsQuery(selectSqlBuilder.append(" LIMIT ").append(size).append(" OFFSET ").append((page - 1) * size).build())
				.setArgs(selectSqlBuilder.getParamValueArray());

		List<E> entities = null;
		int curPageSize = 0;
		try (QueryCursor<List<?>> cursor = this.cache.query(selectQuery)) {
			entities = new ArrayList<>();
			for (List<?> row : cursor) {
				E entity = buildEntity(row);

				entities.add(entity);
				curPageSize++;
			}
		}

		return Page.of(page, curPageSize, total, entities);
	}

	public void removeById(Long id) {
		this.cache.clear(id);
	}

	public void remove(List<Condition> conditions) {
		SqlBuilder deleteSqlBuilder = SqlBuilder.create().delete(cacheName).where(conditions.toArray(new Condition[0]));
		SqlFieldsQuery deleteQuery = new SqlFieldsQuery(deleteSqlBuilder.build()).setArgs(deleteSqlBuilder.getParamValueArray());

		this.cache.query(deleteQuery).getAll();
	}

}
