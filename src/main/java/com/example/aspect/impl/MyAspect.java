
package com.example.aspect.impl;

import com.example.aspect.CaseVerifier;
import com.example.aspect.annotation.InjectedLogger;
import com.example.aspect.annotation.TryCatch;
import com.example.booking.MyLogger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Predicate;

import static java.util.Arrays.*;
import static java.util.stream.Collectors.toList;

@Aspect
public class MyAspect {

	public MyAspect(CaseVerifier verifier){
		collaborator = verifier;
	}

////	@Pointcut(value = "execution(public * com.example.booking.BookingCreator.create(..))")
//	@Pointcut(value = "execution(public * perthis(accessLogger()))")
//	public void accessLogger(){
//	}


//	@Around("accessLogger() && @annotation(tryCatch)")
//	public Object process2(ProceedingJoinPoint jointPoint, TryCatch tryCatch) throws Throwable {
//		return null;
//	}




	private CaseVerifier collaborator;


//	private static final ThreadLocal<Integer> threadId =
//			new ThreadLocal<Integer>() {
//				@Override protected Integer initialValue() {
//					return 1;
//				}
//			};

	@Pointcut(value = "execution(public * com.example.booking.BookingCreator.create(..))")
	public void businessRules() {
	}

	@Around("businessRules() && @annotation(tryCatch)")
	public Object process(ProceedingJoinPoint jointPoint, TryCatch tryCatch) throws Throwable {
		List<String> exceptionNames = getExceptionsToBeCapturedFrom(tryCatch);
		try {
			collaborator.beforeJointPoint();
			final Object proceed = jointPoint.proceed();
			collaborator.afterJointPoint();
			return proceed;
		} catch (Exception e){
			ArrayList<MyLogger> loggers = findLoggersIn(jointPoint);
			logInAll(e, loggers);
			captureOrRethrow(exceptionNames, e);
		}
		return null;
	}

	private List<String> getExceptionsToBeCapturedFrom(TryCatch tryCatch) {
		Class<? extends Exception>[] exceptionsToBeCaught = tryCatch.catchException();

		return asList(exceptionsToBeCaught).stream().map(x -> x.getCanonicalName()).collect(toList());
	}

	private void captureOrRethrow(List<String> exceptionNames, Exception e) throws Exception {
		final String actualExceptionName = e.getClass().getCanonicalName();
		if (exceptionNames.contains(actualExceptionName)) {
			collaborator.capturedExpectedException();
		} else {
			collaborator.capturedUnexpectedException();
			throw e;
		}
	}

	private void logInAll(Exception e, ArrayList<MyLogger> loggers) {
		for(MyLogger logger : loggers){
			 logger.logException(e);
		}
	}

	private ArrayList<MyLogger> findLoggersIn(ProceedingJoinPoint jp) throws IllegalAccessException {
		Object object = jp.getTarget();
		List<Field> fieldList = asList(object.getClass().getFields()).stream().collect(toList());
		ArrayList<MyLogger> loggers = new ArrayList<>();
		for (Field field : fieldList) {
			for (Annotation annotation : field.getDeclaredAnnotations()) {
				if(sameAs(annotation, InjectedLogger.class)) {
					final Object logger = field.get(object);
					field.setAccessible(true);
					loggers.add(((MyLogger) logger));
				}
			}
		}
		return loggers;
	}

	private boolean sameAs(Annotation annotation, Class<InjectedLogger> clazz) {
		return equality(clazz).test(annotation);
	}

	private Predicate<Annotation> equality(Class<?> clazz) {
		return y -> y.annotationType().getCanonicalName().equals(clazz.getCanonicalName());
	}
}
