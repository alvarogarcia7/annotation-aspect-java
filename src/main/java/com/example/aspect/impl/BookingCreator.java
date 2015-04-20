
package com.example.aspect.impl;

import com.example.aspect.annotation.TryCatch;
import com.exception.BusinessRuleException;

public class BookingCreator {

	@TryCatch(catchException= BusinessRuleException.class)
	public void create(final Exception value) throws Exception {
		if (value != null) {
			throw value;
		}
	}
}
