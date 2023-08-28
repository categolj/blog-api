package am.ik.blog.admin.web;

import java.util.Map;

import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.boot.actuate.info.InfoEndpoint;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "info")
public class InfoController {

	private final InfoEndpoint infoEndpoint;

	public InfoController(InfoEndpoint infoEndpoint) {
		this.infoEndpoint = infoEndpoint;
	}

	@GetMapping(path = "/info")
	public Map<String, Object> info() {
		return this.infoEndpoint.info();
	}

}
