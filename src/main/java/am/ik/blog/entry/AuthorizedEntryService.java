package am.ik.blog.entry;

import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import am.ik.blog.entry.search.SearchCriteria;
import am.ik.blog.security.Authorized;
import am.ik.blog.security.Privilege;
import am.ik.pagination.CursorPage;
import am.ik.pagination.CursorPageRequest;
import am.ik.pagination.OffsetPage;
import am.ik.pagination.OffsetPageRequest;

import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthorizedEntryService {

	private final EntryService entryService;

	public AuthorizedEntryService(EntryService entryService) {
		this.entryService = entryService;
	}

	public Long nextId(String tenantId) {
		return entryService.nextId(tenantId);
	}

	@Authorized(resource = "entry", requiredPrivileges = Privilege.LIST)
	public CursorPage<Entry, Instant> findPage(SearchCriteria criteria, @P("tenantId") String tenantId,
			CursorPageRequest<Instant> pageRequest) {
		return entryService.findPage(criteria, tenantId, pageRequest);
	}

	@Authorized(resource = "entry", requiredPrivileges = Privilege.LIST)
	public OffsetPage<Entry> findPage(SearchCriteria criteria, @P("tenantId") String tenantId,
			OffsetPageRequest pageRequest) {
		return entryService.findPage(criteria, tenantId, pageRequest);
	}

	@Authorized(resource = "entry", requiredPrivileges = Privilege.GET)
	public Optional<Entry> findOne(Long entryId, @P("tenantId") String tenantId, boolean excludeContent) {
		return entryService.findOne(entryId, tenantId, excludeContent);
	}

	@Authorized(resource = "entry", requiredPrivileges = Privilege.EXPORT)
	public Path exportEntriesAsZip(@P("tenantId") String tenantId) {
		return entryService.exportEntriesAsZip(tenantId);
	}

	@Authorized(resource = "entry", requiredPrivileges = Privilege.LIST)
	public List<Entry> findAll(SearchCriteria criteria, @P("tenantId") String tenantId, OffsetPageRequest pageRequest) {
		return entryService.findAll(criteria, tenantId, pageRequest);
	}

	@Transactional
	@Authorized(resource = "entry", requiredPrivileges = Privilege.EDIT)
	public Map<String, Integer> save(Entry entry, @P("tenantId") String tenantId) {
		return entryService.save(entry, tenantId);
	}

	@Transactional
	@Authorized(resource = "entry", requiredPrivileges = Privilege.DELETE)
	public int delete(Long entryId, @P("tenantId") String tenantId) {
		return entryService.delete(entryId, tenantId);
	}

}
