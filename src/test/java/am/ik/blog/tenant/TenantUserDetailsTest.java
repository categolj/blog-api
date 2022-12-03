package am.ik.blog.tenant;

import java.util.List;
import java.util.Map;

import am.ik.blog.security.Privilege;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import static am.ik.blog.security.Privilege.DELETE;
import static am.ik.blog.security.Privilege.EDIT;
import static am.ik.blog.security.Privilege.GET;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class TenantUserDetailsTest {

	@Test
	void valueOf() {
		final TenantUserDetails userDetails = TenantUserDetails
				.valueOf("user|{noop}passwd|tenant1=GET,EDIT|tenant2=GET,DELETE");
		assertThat(userDetails.getUsername()).isEqualTo("user");
		assertThat(userDetails.password()).isEqualTo("{noop}passwd");
		assertThat(userDetails.privileges()).containsExactlyInAnyOrderEntriesOf(
				Map.of("tenant1", List.of(GET, EDIT), "tenant2", List.of(GET, DELETE)));
	}
}