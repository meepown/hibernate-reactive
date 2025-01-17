/* Hibernate, Relational Persistence for Idiomatic Java
 *
 * SPDX-License-Identifier: Apache-2.0
 * Copyright: Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.reactive;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.junit.Test;

import io.vertx.ext.unit.TestContext;

import static org.hibernate.reactive.util.impl.CompletionStages.voidFuture;

public class EagerManyToOneAssociationTest extends BaseReactiveTest {

	@Override
	protected Collection<Class<?>> annotatedEntities() {
		return List.of( Book.class, Author.class );
	}

	@Test
	public void persistOneBook(TestContext context) {
		final Book book = new Book( 6, "The Boy, The Mole, The Fox and The Horse" );
		final Author author = new Author( 5, "Charlie Mackesy", book );

		test(
				context,
				openSession()
						.thenCompose( s -> s.persist( book )
								.thenCompose( v -> s.persist( author ) )
								.thenCompose( v -> s.flush() )
						)
						.thenCompose( v -> openSession() )
						.thenCompose( s -> s.find( Author.class, author.getId() ) )
						.thenAccept( optionalAuthor -> {
							context.assertNotNull( optionalAuthor );
							context.assertEquals( author, optionalAuthor );
							context.assertEquals( book, optionalAuthor.getBook()  );
						} )
						.thenCompose( v -> openSession() )
						.thenCompose( s -> s.find( Book.class, book.getId() ) )
						.thenAccept( optionalBook -> {
							context.assertNotNull( optionalBook );
							context.assertEquals( book, optionalBook );
						})
		);
	}

	@Test
	public void persistTwoAuthors(TestContext context) {
		final Book goodOmens = new Book( 72433, "Good Omens: The Nice and Accurate Prophecies of Agnes Nutter, Witch" );
		final Author neilGaiman = new Author( 21421, "Neil Gaiman", goodOmens );
		final Author terryPratchett = new Author( 2111, "Terry Pratchett", goodOmens );

		test(
				context,
				openSession()
						.thenCompose( s -> voidFuture()
								.thenCompose( v -> s.persist( goodOmens ) )
								.thenCompose( v -> s.persist( terryPratchett ) )
								.thenCompose( v -> s.persist( neilGaiman ) )
								.thenCompose( v -> s.flush() )
						)
						.thenCompose( v -> openSession() )
						.thenCompose( s -> s.find( Author.class, neilGaiman.getId() ) )
						.thenAccept( optionalAuthor -> {
							context.assertNotNull( optionalAuthor );
							context.assertEquals( neilGaiman, optionalAuthor );
							context.assertEquals( goodOmens, optionalAuthor.getBook()  );
						} )
		);
	}

	@Test
	public void manyToOneIsNull(TestContext context) {
		final Author author = new Author( 5, "Charlie Mackesy", null );

		test(
				context,
				openSession()
						.thenCompose( s -> s.persist( author ).thenCompose(v-> s.flush()))
						.thenCompose( v -> openSession() )
						.thenCompose( s -> s.find( Author.class, author.getId() ) )
						.thenAccept( optionalAuthor -> {
							context.assertNotNull( optionalAuthor );
							context.assertEquals( author, optionalAuthor );
							context.assertNull( author.book, "Book must be null");
						} )
		);
	}

	@Entity
	@Table(name = Book.TABLE)
	@DiscriminatorValue("N")
	//@Inheritance(strategy = InheritanceType.JOINED)
	public static class Book {
		public static final String TABLE = "Bookz";

		@Id
		private Integer id;
		private String title;

		public Book() {}

		public Book(Integer id, String title) {
			this.id = id;
			this.title = title;
		}

		public Integer getId() {
			return id;
		}

		public void setId(Integer id) {
			this.id = id;
		}

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		@Override
		public boolean equals(Object o) {
			if ( this == o ) {
				return true;
			}
			if ( o == null || getClass() != o.getClass() ) {
				return false;
			}
			Book book = (Book) o;
			return Objects.equals( title, book.title );
		}

		@Override
		public int hashCode() {
			return Objects.hash( title );
		}
	}

	@Entity
	@Table(name = Author.TABLE)
	public static class Author {

		public static final String TABLE = "Authorz";

		@Id
		private Integer id;
		private String name;

		@ManyToOne(fetch = FetchType.EAGER)
		private Book book;

		public Author() {}

		public Author(Integer id, String name, Book book) {
			this.id = id;
			this.name = name;
			this.book = book;
		}

		public Integer getId() {
			return id;
		}

		public void setId(Integer id) {
			this.id = id;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public Book getBook() {
			return book;
		}

		public void setBook(Book book) {
			this.book = book;
		}

		@Override
		public boolean equals(Object o) {
			if ( this == o ) {
				return true;
			}
			if ( o == null || getClass() != o.getClass() ) {
				return false;
			}
			Author author = (Author) o;
			return Objects.equals( name, author.name );
		}

		@Override
		public int hashCode() {
			return Objects.hash( name );
		}
	}
}
