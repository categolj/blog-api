package am.ik.blog.entry;

import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import am.ik.blog.entry.search.SearchCriteria;
import am.ik.blog.security.AccessControl;
import am.ik.blog.security.Privilege;
import am.ik.pagination.CursorPage;
import am.ik.pagination.CursorPageRequest;
import am.ik.pagination.OffsetPage;
import am.ik.pagination.OffsetPageRequest;

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

	@AccessControl(resource = "entry", requiredPrivileges = Privilege.LIST)
	public CursorPage<Entry, Instant> findPage(SearchCriteria criteria, String tenantId,
			CursorPageRequest<Instant> pageRequest) {
		return entryService.findPage(criteria, tenantId, pageRequest);
	}

	@AccessControl(resource = "entry", requiredPrivileges = Privilege.LIST)
	public OffsetPage<Entry> findPage(SearchCriteria criteria, String tenantId,
			OffsetPageRequest pageRequest) {
		return entryService.findPage(criteria, tenantId, pageRequest);
	}

	@AccessControl(resource = "entry", requiredPrivileges = Privilege.GET)
	public Optional<Entry> findOne(Long entryId, String tenantId,
			boolean excludeContent) {
		return entryService.findOne(entryId, tenantId, excludeContent);
	}

	@AccessControl(resource = "entry", requiredPrivileges = Privilege.EXPORT)
	public Path exportEntriesAsZip(String tenantId) {
		return entryService.exportEntriesAsZip(tenantId);
	}

	@AccessControl(resource = "entry", requiredPrivileges = Privilege.LIST)
	public List<Entry> findAll(SearchCriteria criteria, String tenantId,
			OffsetPageRequest pageRequest) {
		return entryService.findAll(criteria, tenantId, pageRequest);
	}

	@Transactional
	@AccessControl(resource = "entry", requiredPrivileges = Privilege.EDIT)
	public Map<String, Integer> save(Entry entry, String tenantId) {
		return entryService.save(entry, tenantId);
	}

	@Transactional
	@AccessControl(resource = "entry", requiredPrivileges = Privilege.DELETE)
	public int delete(Long entryId, String tenantId) {
		return entryService.delete(entryId, tenantId);
	}
}
