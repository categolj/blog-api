package am.ik.blog.service.entry;

import am.ik.blog.model.Entry;
import io.rsocket.exceptions.ApplicationErrorException;
import org.springframework.cloud.sleuth.annotation.NewSpan;
import org.springframework.data.domain.Page;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
public class EntryController {

    private final EntryMapper entryMapper;

    public EntryController(EntryMapper entryMapper) {
        this.entryMapper = entryMapper;
    }

    @MessageMapping("entries.stream")
    @NewSpan
    public Flux<Entry> streamAllEntry(EntryRequest request) {
        return this.entryMapper.findAll(request.toCriteria(), request.toPageable());
    }

    @MessageMapping("entries")
    @NewSpan
    public Mono<Page<Entry>> responsePage(EntryRequest request) {
        return this.entryMapper.findPage(request.toCriteria(), request.toPageable());
    }

    @MessageMapping("entries+c")
    @NewSpan
    public Mono<Page<Entry>> responsePageContentIncluded(EntryRequest request) {
        return this.entryMapper.findPage(request.toCriteria(false), request.toPageable());
    }

    @MessageMapping("entries.{entryId}")
    @NewSpan
    public Mono<Entry> responseEntry(@DestinationVariable("entryId") Long entryId) {
        return this.entryMapper.findOne(entryId, false)
            .switchIfEmpty(errorResponse(entryId));
    }

    static <T> Mono<T> errorResponse(Long entryId) {
        return Mono.error(() -> new ApplicationErrorException(String.format("The requested entry is not found (entryId = %d)", entryId)));
    }
}
