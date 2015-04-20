
package com.example.booking;


import com.example.aspect.annotation.TryCatch;
import com.example.exception.BusinessRuleException;

public class BookingCreator {

	@TryCatch(catchException= BusinessRuleException.class)
	public void create(final Exception value) throws Exception {
		if (value != null) {
			throw value;
		}
	}
}
