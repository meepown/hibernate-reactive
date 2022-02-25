/* Hibernate, Relational Persistence for Idiomatic Java
 *
 * SPDX-License-Identifier: Apache-2.0
 * Copyright: Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.reactive;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import jakarta.persistence.Basic;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import org.hibernate.Hibernate;

import org.junit.Test;

import io.vertx.ext.unit.TestContext;

public class ManyToManyTest extends BaseReactiveTest {

	@Override
	protected Collection<Class<?>> annotatedEntities() {
		return List.of( Book.class, Author.class );
	}

	@Test
	public void test(TestContext context) {
		Book book1 = new Book( "Feersum Endjinn" );
		Book book2 = new Book( "Use of Weapons" );
		Author author = new Author( "Iain M Banks" );
		author.books.add( book1 );
		author.books.add( book2 );

		test(
				context,
				getMutinySessionFactory()
						.withTransaction( (session, transaction) -> session.persistAll( book1, book2, author ) )
						.chain( () -> getMutinySessionFactory()
								.withTransaction( (session, transaction) -> session.find( Author.class, author.id )
										.invoke( a -> context.assertFalse( Hibernate.isInitialized( a.books ) ) )
										.chain( a -> session.fetch( a.books ) )
										.invoke( books -> context.assertEquals( 2, books.size() ) )
								)
						)
						.chain( () -> getMutinySessionFactory()
								.withTransaction( (session, transaction) -> session.createQuery(
																  "select distinct a from Author a left join fetch a.books",
																  Author.class
														  )
														  .getSingleResult()
														  .invoke( a -> context.assertTrue( Hibernate.isInitialized( a.books ) ) )
														  .invoke( a -> context.assertEquals( 2, a.books.size() ) )
								)
						)
						.chain( () -> getMutinySessionFactory()
								.withTransaction( (session, transaction) -> session.find( Author.class, author.id )
										.chain( a -> session.fetch( a.books ) )
										.invoke( books -> books.remove( 0 ) )
								)
						)
						.chain( () -> getMutinySessionFactory()
								.withTransaction( (session, transaction) -> session.find( Author.class, author.id )
										.chain( a -> session.fetch( a.books ) )
										.invoke( books -> {
											context.assertEquals( 1, books.size() );
											context.assertEquals( book2.title, books.get( 0 ).title );
										} )
								)
						)
						.chain( () -> getMutinySessionFactory()
								.withTransaction( (session, transaction) -> session.find( Author.class, author.id )
										.chain( a -> session.fetch( a.books ) )
										.chain( books -> session.find( Book.class, book1.id ).invoke( books::add ) )
								)
						)
						.chain( () -> getMutinySessionFactory()
								.withTransaction( (session, transaction) -> session.find( Author.class, author.id )
										.invoke( a -> context.assertFalse( Hibernate.isInitialized( a.books ) ) )
										.chain( a -> session.fetch( a.books ) )
										.invoke( books -> context.assertEquals( 2, books.size() ) )
								)
						)
						.chain( () -> getMutinySessionFactory()
								.withTransaction( (session, transaction) -> session.find( Author.class, author.id )
										.chain( a -> session.fetch( a.books ) )
										.invoke( books -> books.remove( 1 ) )
								)
						)
						.chain( () -> getMutinySessionFactory()
								.withTransaction( (session, transaction) -> session.find( Author.class, author.id )
										.chain( a -> session.fetch( a.books ) )
										.invoke( books -> {
											context.assertEquals( 1, books.size() );
											context.assertEquals( book1.title, books.get( 0 ).title );
										} )
								)
						)
						.chain( () -> getMutinySessionFactory()
								.withTransaction( (session, transaction) -> session.find( Author.class, author.id )
										.chain( a -> session.fetch( a.books ) )
										.chain( books -> session.find( Book.class, book2.id ).invoke( books::add ) )
								)
						)
						.chain( () -> getMutinySessionFactory()
								.withTransaction( (session, transaction) -> session.find( Author.class, author.id )
										.invoke( a -> context.assertFalse( Hibernate.isInitialized( a.books ) ) )
										.chain( a -> session.fetch( a.books ) )
										.invoke( books -> context.assertEquals( 2, books.size() ) )
								)
						)
						.chain( () -> getMutinySessionFactory()
								.withTransaction( (session, transaction) -> session.find( Author.class, author.id )
										.chain( a -> session.fetch( a.books ) )
										.invoke( books -> books.add( books.remove( 0 ) ) )
								)
						)
						.chain( () -> getMutinySessionFactory()
								.withTransaction( (session, transaction) -> session.find( Author.class, author.id )
										.invoke( a -> context.assertFalse( Hibernate.isInitialized( a.books ) ) )
										.chain( a -> session.fetch( a.books ) )
										.invoke( books -> context.assertEquals( 2, books.size() ) )
								)
						)
						.chain( () -> getMutinySessionFactory()
								.withTransaction( (session, transaction) -> session.find( Author.class, author.id )
										.invoke( a -> a.books = null )
								)
						)
						.chain( () -> getMutinySessionFactory()
								.withTransaction( (session, transaction) -> session.find( Author.class, author.id )
										.chain( a -> session.fetch( a.books ) )
										.invoke( books -> context.assertTrue( books.isEmpty() ) )
								)
						)
		);
	}

	@Entity(name = "Book")
	@Table(name = "MTMBook")
	static class Book {
		Book(String title) {
			this.title = title;
		}

		Book() {
		}

		@GeneratedValue
		@Id
		long id;

		@Basic(optional = false)
		String title;
	}

	@Entity(name = "Author")
	@Table(name = "MTMAuthor")
	static class Author {
		Author(String name) {
			this.name = name;
		}

		public Author() {
		}

		@GeneratedValue
		@Id
		long id;

		@Basic(optional = false)
		String name;

		@ManyToMany
		@OrderBy("id")
		List<Book> books = new ArrayList<>();
	}
}
