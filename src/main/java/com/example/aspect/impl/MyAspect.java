
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
import java.util.List;
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
		Class<? extends Exception>[] exceptionsToBeCaught = tryCatch.catchException();

		List<String> exceptionNames = asList(exceptionsToBeCaught).stream().map(x -> x.getCanonicalName()).collect(toList());
		try {
			collaborator.beforeJointPoint();
			final Object proceed = jointPoint.proceed();
			collaborator.afterJointPoint();
			return proceed;
		} catch (Exception e){

			final Object object = jointPoint.getTarget();

			List<Field> fieldList = asList(object.getClass().getFields()).stream().collect(toList());
			for (Field field : fieldList) {
				for (Annotation annotation : field.getDeclaredAnnotations()) {
					if(sameAs(annotation, InjectedLogger.class)) {
						final Field logger = field;
						logger.setAccessible(true);
						((MyLogger) field.get(object)).logException(e);
					}
				}
			}

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

	private boolean sameAs(Annotation annotation, Class<InjectedLogger> clazz) {
		return equality(clazz).test(annotation);
	}

	private Predicate<Annotation> equality(Class<?> clazz) {
		return y -> y.annotationType().getCanonicalName().equals(clazz.getCanonicalName());
	}
}
