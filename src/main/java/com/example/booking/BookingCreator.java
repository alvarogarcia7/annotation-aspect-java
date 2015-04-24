
package com.example.booking;


import com.example.aspect.annotation.TryCatch;
import com.example.aspect.annotation.InjectedLogger;
import com.example.exception.BusinessRuleException;

import java.io.InvalidClassException;

public class BookingCreator {

	@InjectedLogger
	private MyLogger logger = new MyLogger();

	@Deprecated
	private MyLogger fakeLogger = new MyLogger();

	@TryCatch(catchException= {BusinessRuleException.class, InvalidClassException.class})
	public void create(final Exception value) throws Exception {
		if (value != null) {
			if(null != fakeLogger ) {
				this.fakeLogger.logException(value);
			}
			throw value;
		}
	}

	public void setLogger(MyLogger logger){
		this.logger = logger;
	}

	public void setFakeLogger (final MyLogger logger) {
		this.fakeLogger = logger;
	}
}
