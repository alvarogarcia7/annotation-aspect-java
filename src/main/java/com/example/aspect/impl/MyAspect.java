
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
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static java.util.Arrays.asList;
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
		try {
			collaborator.beforeJointPoint();
			final Object proceed = jointPoint.proceed();
			collaborator.afterJointPoint();
			return proceed;
		} catch (Exception e){
			ArrayList<MyLogger> loggers = findLoggersIn(jointPoint);
			logInAll(loggers, e);
			List<Class> exceptionNames = getExceptionsToBeCapturedFrom(tryCatch);
			captureOrRethrow(exceptionNames, e);
		}
		return null;
	}

	private List<Class> getExceptionsToBeCapturedFrom(TryCatch tryCatch) {
		return asList(tryCatch.catchException());
	}

	private void captureOrRethrow(List<Class> exceptionNames, Exception e) throws Exception {
		boolean found = false;
		for (Class current : exceptionNames) {
			if (current.isAssignableFrom(e.getClass())) {
				found = true;
				collaborator.capturedExpectedException();
			}
		}
		if (!found) {
			collaborator.capturedUnexpectedException();
			throw e;
		}
	}

	private void logInAll(ArrayList<MyLogger> loggers, Exception e) {
		for(MyLogger logger : loggers){
			 logger.logException(e);
		}
	}

	private ArrayList<MyLogger> findLoggersIn(ProceedingJoinPoint jp) throws IllegalAccessException {
		Object object = jp.getTarget();
		List<Field> fieldList = asList(object.getClass().getDeclaredFields()).stream().collect(toList());
		ArrayList<MyLogger> loggers = new ArrayList<>();
		for (Field field : fieldList) {
			for (Annotation annotation : field.getDeclaredAnnotations()) {
				if(sameAs(annotation, InjectedLogger.class)) {
					field.setAccessible(true);
					final Object logger = field.get(object);
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
