
package com.example.aspect.impl.internal;

import com.example.aspect.CaseVerifier;
import com.example.aspect.annotation.TryCatch;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

@Aspect
public class MyAspect {

	public MyAspect(CaseVerifier verifier){
		collaborator = verifier;
	}


	private CaseVerifier collaborator;

	@Pointcut(value = "execution(public * com.example.aspect.impl.BookingCreator.create(..))")
	public void businessRules() {
	}

	@Around("businessRules() && @annotation(tryCatch)")
	public Object process(ProceedingJoinPoint jointPoint, TryCatch tryCatch) throws Throwable {
		Class<? extends Exception> get = tryCatch.catchException();
		try {
			collaborator.beforeJointPoint();
			final Object proceed = jointPoint.proceed();
			collaborator.afterJointPoint();
			return proceed;
		} catch (Exception e){
			if(e.getClass().getCanonicalName().equals(get.getCanonicalName())){
				collaborator.capturedExpectedException();
			} else {
				collaborator.capturedUnexpectedException();
				throw e;
			}
		}
		return null;
	}
}
