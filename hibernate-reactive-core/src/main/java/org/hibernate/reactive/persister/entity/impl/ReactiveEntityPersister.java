/* Hibernate, Relational Persistence for Idiomatic Java
 *
 * SPDX-License-Identifier: Apache-2.0
 * Copyright: Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.reactive.persister.entity.impl;

import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.LockOptions;
import org.hibernate.bytecode.BytecodeLogging;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.event.spi.EventSource;
import org.hibernate.loader.entity.UniqueEntityLoader;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.persister.entity.MultiLoadOptions;
import org.hibernate.reactive.loader.entity.ReactiveUniqueEntityLoader;

import javax.persistence.metamodel.Attribute;
import java.io.Serializable;
import java.util.List;
import java.util.concurrent.CompletionStage;


/**
 * A reactive {@link EntityPersister}. Supports non-blocking
 * insert/update/delete operations.
 *
 * @see ReactiveAbstractEntityPersister
 */
public interface ReactiveEntityPersister extends EntityPersister {

	/**
	 * Insert the given instance state without blocking.
	 *
	 * @see EntityPersister#insert(Serializable, Object[], Object, SharedSessionContractImplementor)
	 */
	CompletionStage<Void> insertReactive(
			Serializable id,
			Object[] fields,
			Object object,
			SharedSessionContractImplementor session);

	/**
	 * Insert the given instance state without blocking.
	 *
	 * @see EntityPersister#insert(Object[], Object, SharedSessionContractImplementor)
	 */
	CompletionStage<Serializable> insertReactive(
			Object[] fields,
			Object object,
			SharedSessionContractImplementor session);

	/**
	 * Delete the given instance without blocking.
	 *
	 * @see EntityPersister#delete(Serializable, Object, Object, SharedSessionContractImplementor)
	 */
	CompletionStage<Void> deleteReactive(
			Serializable id,
			Object version,
			Object object,
			SharedSessionContractImplementor session)
					throws HibernateException;

	/**
	 * Update the given instance state without blocking.
	 *
	 * @see EntityPersister#update(Serializable, Object[], int[], boolean, Object[], Object, Object, Object, SharedSessionContractImplementor)
	 */
	CompletionStage<Void> updateReactive(
			Serializable id,
			Object[] fields, int[] dirtyFields,
			boolean hasDirtyCollection,
			Object[] oldFields, Object oldVersion,
			Object object,
			Object rowId,
			SharedSessionContractImplementor session)
					throws HibernateException;

	/**
	 * Obtain a pessimistic lock without blocking
	 */
	CompletionStage<Void> lockReactive(
			Serializable id,
			Object version,
			Object object,
			LockOptions lockOptions,
			SharedSessionContractImplementor session)
			throws HibernateException;

	CompletionStage<List<Object>> reactiveMultiLoad(
	 		Serializable[] ids,
			SessionImplementor session,
			MultiLoadOptions loadOptions);

	CompletionStage<Object> reactiveLoad(Serializable id,
										 Object optionalObject,
										 LockOptions lockOptions,
										 SharedSessionContractImplementor session);

	CompletionStage<Object> reactiveLoad(Serializable id,
										 Object optionalObject,
										 LockOptions lockOptions,
										 SharedSessionContractImplementor session,
										 Boolean readOnly);

	CompletionStage<Object> reactiveLoadByUniqueKey(
			String propertyName,
			Object uniqueKey,
			SharedSessionContractImplementor session);

	ReactiveUniqueEntityLoader getAppropriateLoader(LockOptions lockOptions,
													SharedSessionContractImplementor session);

	ReactiveUniqueEntityLoader getAppropriateUniqueKeyLoader(String propertyName,
															 SharedSessionContractImplementor session);

	UniqueEntityLoader getLoaderForLockMode(LockMode lockMode);

	CompletionStage<Object> reactiveGetCurrentVersion(Serializable id,
													  SharedSessionContractImplementor session);

	/**
	 * @see EntityPersister#processInsertGeneratedProperties(Serializable, Object, Object[], SharedSessionContractImplementor)
	 */
	CompletionStage<Void> reactiveProcessInsertGenerated(Serializable id, Object entity, Object[] state, SharedSessionContractImplementor session);

	/**
	 * @see EntityPersister#processUpdateGeneratedProperties(Serializable, Object, Object[], SharedSessionContractImplementor)
	 */
	CompletionStage<Void> reactiveProcessUpdateGenerated(Serializable id, Object entity, Object[] state, SharedSessionContractImplementor session);

	/**
	 * Get the current database state of the object, in a "hydrated" form, without
	 * resolving identifiers
	 *
	 * @return null if there is no row in the database
	 */
	CompletionStage<Object[]> reactiveGetDatabaseSnapshot(Serializable id,
														  SharedSessionContractImplementor session);

	<E,T> CompletionStage<T> reactiveInitializeLazyProperty(Attribute<E,T> field, E entity,
															SharedSessionContractImplementor session);

	CompletionStage<Object> reactiveInitializeEnhancedEntityUsedAsProxy(
			Object entity,
			String nameOfAttributeBeingAccessed,
			SharedSessionContractImplementor session);

	CompletionStage<Serializable> reactiveLoadEntityIdByNaturalId(Object[] orderedNaturalIdValues,
																  LockOptions lockOptions, EventSource session);

	/**
	 * @see org.hibernate.bytecode.enhance.spi.interceptor.EnhancementAsProxyLazinessInterceptor#forceInitialize(Object, String, SharedSessionContractImplementor, boolean)
	 */
	//TODO find somewhere else for this function to live
	static CompletionStage<Object> forceInitialize(
			Object target,
			String attributeName,
			Object entityId,
			String entityName,
			SharedSessionContractImplementor session) {
		BytecodeLogging.LOGGER.tracef(
				"EnhancementAsProxyLazinessInterceptor#forceInitialize : %s#%s -> %s )",
				entityName,
				entityId,
				attributeName
		);

		ReactiveEntityPersister persister = (ReactiveEntityPersister)
				session.getFactory()
						.getMetamodel()
						.entityPersister(entityName);
		return persister.reactiveInitializeEnhancedEntityUsedAsProxy( target, attributeName, session );
	}
}
