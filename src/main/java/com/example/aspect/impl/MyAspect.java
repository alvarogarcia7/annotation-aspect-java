
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
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

		List<String> exceptionNames = Arrays.asList(exceptionsToBeCaught).stream().map(x -> x.getCanonicalName()).collect(toList());
		try {
			collaborator.beforeJointPoint();
			final Object proceed = jointPoint.proceed();
			collaborator.afterJointPoint();
			return proceed;
		} catch (Exception e){



			final String actualExceptionName = e.getClass().getCanonicalName();

			final Object object = jointPoint.getTarget();
			final Field logger = object.getClass().getFields()[0];
			logger.setAccessible(true);
			((MyLogger)logger.get(object)).logException(e);

			final Field[] fields = object.getClass().getFields();
			List<Field> fieldList = Arrays.asList(fields).stream().collect(Collectors.toList());
			Predicate<Annotation> containsCanonicalName = y -> y.annotationType().getCanonicalName().equals(InjectedLogger.class.getCanonicalName());
			for (Field field : fieldList) {
				for (Annotation annotation : field.getDeclaredAnnotations()) {
					boolean matchesAnnotation = containsCanonicalName.test(annotation);
//					matchesAnnotation = annotation.annotationType().getCanonicalName().equals(InjectedLogger.class.getCanonicalName());
					System.out.println("field: " + field + ", " + matchesAnnotation);
				}
			}


//			List<Field> loggers = fieldStream.filter(x -> {
//				System.out.println(x);
////				return Arrays.asList(x.getDeclaredAnnotations()).stream().filter(containsCanonicalName).findAny().isPresent();
//				return Arrays.asList(x.getDeclaredAnnotations()).stream().map(z->{
//					System.out.println(z); return containsCanonicalName.test(z);});
//			}).collect(Collectors.toList());

//			String tryCatchCollaboratorName = InjectedLogger.class.getCanonicalName();
//			List<Field> fieldsWithAnnotation = Arrays.asList(tryCatch.getClass().getFields()).stream().filter(x -> {
//				final boolean containsADeclaration = Arrays.asList(x.getDeclaredAnnotations()).stream().filter(y -> y.getClass().getCanonicalName().equals(tryCatchCollaboratorName)).findAny().isPresent();
//				return containsADeclaration;
//			}).collect(Collectors.toList());
//
//			System.out.println(fieldsWithAnnotation);


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
