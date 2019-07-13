package am.ik.blog.entry;

import am.ik.blog.config.R2dbcConfig;
import am.ik.blog.entry.criteria.SearchCriteria;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import reactor.test.StepVerifier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.r2dbc.core.DatabaseClient;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.reactive.TransactionalOperator;

import static am.ik.blog.entry.Asserts.assertEntry99997;
import static am.ik.blog.entry.Asserts.assertEntry99998;
import static am.ik.blog.entry.Asserts.assertEntry99999;

@RunWith(SpringRunner.class)
@JdbcTest
@Import(R2dbcConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql({ "classpath:/delete-test-data.sql", "classpath:/insert-test-data.sql" })
public class EntryMapperTest {
	@Autowired
	EntryMapper entryMapper;

	@Test
	@Ignore
	public void findOne() {
		StepVerifier.create(entryMapper.findOne(new EntryId(99999L), false))
				.assertNext(entry -> assertEntry99999(entry) //
						.assertContent())
				.verifyComplete();
	}

	@Test
	@Ignore
	public void collectAll() {
		StepVerifier
				.create(entryMapper.collectAll(SearchCriteria.builder().build(),
						PageRequest.of(0, 10)))
				.assertNext(entry -> assertEntry99999(entry) //
						.assertThatContentIsNotSet())
				.assertNext(entry -> assertEntry99998(entry) //
						.assertThatContentIsNotSet())
				.assertNext(entry -> assertEntry99997(entry) //
						.assertThatContentIsNotSet())
				.verifyComplete();
	}

	@Test
	@Ignore("does not work :-(")
	public void save() {
		EventTime now = EventTime.now();
		Entry entry = Entry.builder() //
				.entryId(new EntryId(99991L)) //
				.content(new Content("demo")) //
				.created(new Author(new Name("test"), now)) //
				.updated(new Author(new Name("test"), now)) //
				.frontMatter(new FrontMatter(new Title("hello"),
						new Categories(new Category("foo"), new Category("bar")),
						new Tags(new Tag("test1"), new Tag("test2")))) //
				.build();

		StepVerifier.create(entryMapper.save(entry)) //
				.expectNext(entry) //
				.verifyComplete();
	}

	@Test
	@Ignore("does not work :-(")
	public void delete() {
		StepVerifier.create(this.entryMapper.delete(new EntryId(99999L)))
				.expectNext(new EntryId(99999L)) //
				.verifyComplete();
	}

	@Configuration
	static class Config {
		@Bean
		public EntryMapper reactiveEntryMapper(DatabaseClient databaseClient,
				TransactionalOperator transactionalOperator) {
			return new EntryMapper(databaseClient, transactionalOperator);
		}
	}
}