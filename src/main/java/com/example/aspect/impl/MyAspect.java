
package com.example.aspect.impl;

import com.example.aspect.CaseVerifier;
import com.example.aspect.annotation.TryCatch;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Aspect
public class MyAspect {

	public MyAspect(CaseVerifier verifier){
		collaborator = verifier;
	}


	private CaseVerifier collaborator;

	@Pointcut(value = "execution(public * com.example.booking.BookingCreator.create(..))")
	public void businessRules() {
	}

	@Around("businessRules() && @annotation(tryCatch)")
	public Object process(ProceedingJoinPoint jointPoint, TryCatch tryCatch) throws Throwable {
		Class<? extends Exception>[] exceptionsToBeCaught = tryCatch.catchException();

		List<String> exceptionNames = Arrays.asList(exceptionsToBeCaught).stream().map(x -> x.getCanonicalName()).collect(toList());
		try {
			collaborator.beforeJointPoint();
			final Object proceed = jointPoint.proceed();
			collaborator.afterJointPoint();
			return proceed;
		} catch (Exception e){

			final String actualExceptionName = e.getClass().getCanonicalName();

			if (exceptionNames.contains(actualExceptionName)) {
				collaborator.capturedExpectedException();
			} else {
				collaborator.capturedUnexpectedException();
				throw e;
			}
		}
		return null;
	}
}
