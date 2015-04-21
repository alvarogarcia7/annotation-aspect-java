
package com.example.booking;


import com.example.aspect.annotation.TryCatch;
import com.example.exception.BusinessRuleException;

import java.io.InvalidClassException;

public class BookingCreator {

	@TryCatch(catchException= {BusinessRuleException.class, InvalidClassException.class})
	public void create(final Exception value) throws Exception {
		if (value != null) {
			throw value;
		}
	}
}
