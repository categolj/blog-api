package am.ik.blog.admin.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ActuatorRedirectController {
	@GetMapping(path = "/actuator/")
	public String reditect() {
		return "redirect:/actuator";
	}
}
