package am.ik.blog.entry;

import java.util.Optional;

import am.ik.blog.github.Fixtures;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.transaction.TransactionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE,
		properties = { "resilience4j.retry.instances.blog-db.wait-duration=100ms",
				"resilience4j.retry.instances.blog-db.max-attempts=3" })
class EntryServiceTest {

	@Autowired
	EntryService entryService;

	@MockBean
	EntryMapper entryMapper;

	@Test
	void findOne() {
		Entry entry = Fixtures.entry(1L);
		given(this.entryMapper.findOne(1L, null, false)).willReturn(Optional.of(entry));
		Optional<Entry> found = this.entryService.findOne(1L, null, false);
		assertThat(found).isNotEmpty();
		assertThat(found).containsSame(entry);
	}

	@Test
	void findOneRetry() {
		Entry entry = Fixtures.entry(1L);
		given(this.entryMapper.findOne(1L, null, false)).willThrow(new CannotCreateTransactionException("1"))
			.willThrow(new CannotCreateTransactionException("2"))
			.willReturn(Optional.of(entry));
		Optional<Entry> found = this.entryService.findOne(1L, null, false);
		assertThat(found).isNotEmpty();
		assertThat(found).containsSame(entry);
	}

	@Test
	void findOneRetryFail() {
		given(this.entryMapper.findOne(1L, null, false)).willThrow(new CannotCreateTransactionException("1"))
			.willThrow(new CannotCreateTransactionException("2"))
			.willThrow(new CannotCreateTransactionException("3"));
		assertThatThrownBy(() -> {
			this.entryService.findOne(1L, null, false);
		}).isInstanceOf(TransactionException.class).hasMessageContaining("3");
	}

}