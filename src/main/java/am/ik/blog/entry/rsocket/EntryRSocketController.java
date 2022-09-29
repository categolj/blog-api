package am.ik.blog.entry.rsocket;

import am.ik.blog.entry.Entry;
import am.ik.blog.entry.EntryService;
import io.rsocket.exceptions.ApplicationErrorException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.data.domain.Page;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EntryRSocketController {

	private final EntryService entryService;

	public EntryRSocketController(EntryService entryService) {
		this.entryService = entryService;
	}

	@MessageMapping("entries.stream")
	public Flux<Entry> streamAllEntry(EntryRequest request) {
		return this.entryService.findAll(request.toCriteria(), request.toPageable());
	}

	@MessageMapping("entries")
	public Mono<Page<Entry>> responsePage(EntryRequest request) {
		return this.entryService.findPage(request.toCriteria(), request.toPageable());
	}

	@MessageMapping("entries+c")
	public Mono<Page<Entry>> responsePageContentIncluded(EntryRequest request) {
		return this.entryService.findPage(request.toCriteria(false), request.toPageable());
	}

	@MessageMapping("entries.{entryId}")
	public Mono<Entry> responseEntry(@DestinationVariable("entryId") Long entryId) {
		return this.entryService.findOne(entryId, false)
				.switchIfEmpty(errorResponse(entryId));
	}

	static <T> Mono<T> errorResponse(Long entryId) {
		return Mono.error(() -> new ApplicationErrorException(String.format("The requested entry is not found (entryId = %d)", entryId)));
	}
}
