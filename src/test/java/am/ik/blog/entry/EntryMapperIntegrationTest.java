package am.ik.blog.entry;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import am.ik.blog.category.Category;
import am.ik.blog.entry.search.SearchCriteria;
import am.ik.blog.github.Fixtures;
import am.ik.blog.tag.Tag;
import am.ik.blog.util.FileLoader;
import am.ik.pagination.OffsetPageRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestConstructor;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@Testcontainers(disabledWithoutDocker = true)
class EntryMapperIntegrationTest {

	@Autowired
	EntryMapper entryMapper;

	@Autowired
	JdbcTemplate jdbcTemplate;

	@Container
	@ServiceConnection
	static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:14-alpine");

	@BeforeEach
	public void reset() {
		jdbcTemplate.update(FileLoader.loadAsString("sql/delete-test-data.sql"));
	}

	@ParameterizedTest
	@CsvSource({ ",", "demo," })
	@Transactional
	void insert(String tenantId) {
		final Entry entry = Fixtures.entry(99999L);
		final Map<String, Integer> result = this.entryMapper.save(entry, tenantId);
		assertThat(result.get("upsertEntry")).isEqualTo(1);
		final Optional<Entry> one = this.entryMapper.findOne(entry.getEntryId(), tenantId, false);
		assertThat(one.isPresent()).isTrue();
		final Entry found = one.get();
		assertThat(found.getEntryId()).isEqualTo(99999L);
		assertThat(found.getContent()).isEqualTo("Hello");
		final FrontMatter frontMatter = found.getFrontMatter();
		assertThat(frontMatter.title()).isEqualTo("Hello");
		assertThat(frontMatter.tags()).hasSize(3);
		assertThat(frontMatter.tags()).containsExactly(new Tag("a"), new Tag("b"), new Tag("c"));
		assertThat(frontMatter.categories()).hasSize(3);
		assertThat(frontMatter.categories()).containsExactly(new Category("foo"), new Category("bar"),
				new Category("hoge"));
		assertThat(found.getCreated().name()).isEqualTo("demo");
		assertThat(found.getCreated().date()).isEqualTo(entry.getCreated().date());
		assertThat(found.getUpdated().name()).isEqualTo("demo");
		assertThat(found.getUpdated().date()).isEqualTo(entry.getUpdated().date());
		final Optional<Entry> defaultOne = this.entryMapper.findOne(entry.getEntryId(),
				tenantId == null ? "demo" : null, false);
		assertThat(defaultOne.isEmpty()).isTrue();
	}

	@ParameterizedTest
	@CsvSource({ ",", "demo," })
	@Transactional
	void update(String tenantId) {
		jdbcTemplate.update(FileLoader.loadAsString("sql/insert-test-data.sql"));
		final Entry entry = Fixtures.entry(99999L);
		final Map<String, Integer> result = this.entryMapper.save(entry, tenantId);
		assertThat(result.get("upsertEntry")).isEqualTo(1);
		final Optional<Entry> one = this.entryMapper.findOne(entry.getEntryId(), tenantId, false);
		assertThat(one.isPresent()).isTrue();
		final Entry found = one.get();
		assertThat(found.getEntryId()).isEqualTo(99999L);
		assertThat(found.getContent()).isEqualTo("Hello");
		final FrontMatter frontMatter = found.getFrontMatter();
		assertThat(frontMatter.title()).isEqualTo("Hello");
		assertThat(frontMatter.tags()).hasSize(3);
		assertThat(frontMatter.tags()).containsExactly(new Tag("a"), new Tag("b"), new Tag("c"));
		assertThat(frontMatter.categories()).hasSize(3);
		assertThat(frontMatter.categories()).containsExactly(new Category("foo"), new Category("bar"),
				new Category("hoge"));
		assertThat(found.getCreated().name()).isEqualTo("demo");
		assertThat(found.getCreated().date()).isEqualTo(entry.getCreated().date());
		assertThat(found.getUpdated().name()).isEqualTo("demo");
		assertThat(found.getUpdated().date()).isEqualTo(entry.getUpdated().date());
	}

	@ParameterizedTest
	@CsvSource({ ",", ",demo" })
	@Transactional
	void insertAndSearch(String tenantId) {
		final OffsetDateTime now = OffsetDateTime.now();
		final Entry entry99993 = new EntryBuilder().withEntryId(99993L)
			.withContent(
					"""
							Spring Boot makes it easy to create stand-alone, production-grade Spring based Applications that you can "just run".

							We take an opinionated view of the Spring platform and third-party libraries so you can get started with minimum fuss. Most Spring Boot applications need minimal Spring configuration.

							If you’re looking for information about a specific version, or instructions about how to upgrade from an earlier release, check out the project release notes section on our wiki.
							""")
			.withCreated(new Author("test", now))
			.withUpdated(new Author("test", now))
			.withFrontMatter(new FrontMatterBuilder().withTitle("Spring Boot")
				.withCategories(List.of(new Category("Document")))
				.withTags(List.of(new Tag("Spring Boot")))
				.build())
			.build();
		this.entryMapper.save(entry99993, tenantId);
		final Entry entry99994 = new EntryBuilder().withEntryId(99994L)
			.withContent(
					"""
							The Spring Framework provides a comprehensive programming and configuration model for modern Java-based enterprise applications - on any kind of deployment platform.

							A key element of Spring is infrastructural support at the application level: Spring focuses on the "plumbing" of enterprise applications so that teams can focus on application-level business logic, without unnecessary ties to specific deployment environments.
							""")
			.withCreated(new Author("test", now.plusHours(1)))
			.withUpdated(new Author("test", now.plusHours(1)))
			.withFrontMatter(new FrontMatterBuilder().withTitle("Spring Framework")
				.withCategories(List.of(new Category("Document")))
				.withTags(List.of(new Tag("Spring Framework")))
				.build())
			.build();
		this.entryMapper.save(entry99994, tenantId);

		final List<Entry> search1 = this.entryMapper.findAll(SearchCriteria.builder().keyword("Spring Boot").build(),
				tenantId, new OffsetPageRequest(0, 100));
		assertThat(search1).hasSize(1);
		assertThat(search1.get(0).getEntryId()).isEqualTo(99993L);

		final List<Entry> search2 = this.entryMapper.findAll(
				SearchCriteria.builder().keyword("Spring Framework").build(), tenantId, new OffsetPageRequest(0, 100));
		assertThat(search2).hasSize(1);
		assertThat(search2.get(0).getEntryId()).isEqualTo(99994L);

		final List<Entry> search3 = this.entryMapper.findAll(
				SearchCriteria.builder().keyword("Applications Platform").build(), tenantId,
				new OffsetPageRequest(0, 100));
		assertThat(search3).hasSize(2);
		assertThat(search3.get(0).getEntryId()).isEqualTo(99994L);
		assertThat(search3.get(1).getEntryId()).isEqualTo(99993L);

		final List<Entry> search4 = this.entryMapper.findAll(SearchCriteria.builder().keyword("Spring -Boot").build(),
				tenantId, new OffsetPageRequest(0, 100));
		assertThat(search4).hasSize(1);
		assertThat(search4.get(0).getEntryId()).isEqualTo(99994L);
	}

	@ParameterizedTest
	@CsvSource({ ",", "demo," })
	@Transactional
	void insertAndSearch_Japanese(String tenantId) {
		final OffsetDateTime now = OffsetDateTime.now();
		final Entry entry99993 = new EntryBuilder().withEntryId(99993L)
			.withContent("""
					むかし、むかし、あるところに、おじいさんとおばあさんがありました。まいにち、おじいさんは山へしば刈りに、おばあさんは川へ洗濯に行きました。
					ある日、おばあさんが、川のそばで、せっせと洗濯をしていますと、川上から、大きな桃が一つ、
					「ドンブラコッコ、スッコッコ。
					ドンブラコッコ、スッコッコ。」
					と流れて来ました。
					""")
			.withCreated(new Author("test", now))
			.withUpdated(new Author("test", now))
			.withFrontMatter(new FrontMatterBuilder().withTitle("桃太郎")
				.withCategories(List.of(new Category("青空文庫")))
				.withTags(List.of(new Tag("桃太郎")))
				.build())
			.build();
		this.entryMapper.save(entry99993, tenantId);
		final Entry entry99994 = new EntryBuilder().withEntryId(99994L)
			.withContent("""
					むかし、金太郎という強い子供がありました。相模国足柄山の山奥に生まれて、おかあさんの山うばといっしょにくらしていました。
					金太郎は生まれた時ときからそれはそれは力が強くって、もう七つ八つのころには、石臼やもみぬかの俵ぐらい、へいきで持ち上げました。
					大抵の大人を相手にすもうを取とっても負けませんでした。近所にもう相手がなくなると、つまらなくなって金太郎は、一日森の中をかけまわりました。
					そしておかあさんにもらった大きなまさかりをかついで歩いて、やたらに大きな杉の木や松の木をきり倒しては、きこりのまねをしておもしろがっていました。
					""")
			.withCreated(new Author("test", now))
			.withUpdated(new Author("test", now))
			.withFrontMatter(new FrontMatterBuilder().withTitle("金太郎")
				.withCategories(List.of(new Category("青空文庫")))
				.withTags(List.of(new Tag("金太郎")))
				.build())
			.build();
		this.entryMapper.save(entry99994, tenantId);

		final List<Entry> search1 = this.entryMapper.findAll(SearchCriteria.builder().keyword("おじいさん おばあさん").build(),
				tenantId, new OffsetPageRequest(0, 100));
		assertThat(search1).hasSize(1);
		assertThat(search1.get(0).getEntryId()).isEqualTo(99993L);
		final List<Entry> search2 = this.entryMapper.findAll(SearchCriteria.builder().keyword("おかあさん うば").build(),
				tenantId, new OffsetPageRequest(0, 100));
		assertThat(search2).hasSize(1);
		assertThat(search2.get(0).getEntryId()).isEqualTo(99994L);
	}

}