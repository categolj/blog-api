package org.flywaydb.core.internal.reports.json;

import org.flywaydb.core.extensibility.Plugin;

public class InfoResultDeserializer implements Plugin {

	@Override
	public boolean isEnabled() {
		return false;
	}

}
