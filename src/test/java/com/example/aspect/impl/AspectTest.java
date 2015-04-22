package com.example.aspect.impl;

import com.example.aspect.CaseVerifier;
import com.example.booking.BookingCreator;
import com.example.booking.MyLogger;
import com.example.exception.BusinessRuleException;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.InvalidClassException;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class AspectTest {

	private static ClassPathXmlApplicationContext appContext;
	private CaseVerifier caseVerifier;
	private BookingCreator myComponent;


	@BeforeClass
	public static void setUpClass() throws Exception {
		appContext = new ClassPathXmlApplicationContext("spring-config.xml");
	}

	@Before
	public void setUp() throws Exception {
		caseVerifier = appContext.getBean(CaseVerifier.class);
		caseVerifier.reset();
		myComponent = appContext.getBean(BookingCreator.class);
	}

	@Test
	public void capture_the_first_correct_exception() throws Exception {

		myComponent.create(new BusinessRuleException());

		assertCapturedException();
	}

	@Test
	public void invoke_the_logger() throws Exception {
		final MyLogger mock = mock(MyLogger.class);
		myComponent.setLogger(mock);

		final InvalidClassException invalidClassException = new InvalidClassException("");
		myComponent.create(invalidClassException);

		verify(mock).logException(invalidClassException);
	}

	@Test
	public void capture_the_second_correct_exception() throws Exception {
		myComponent.create(new InvalidClassException(""));

		assertCapturedException();
	}

	@Test
	public void should_interact_with_my_collaborator() throws Exception {

		myComponent.create(new InvalidClassException(""));

		assertCapturedException();
	}



	@Test
	public void not_affect_a_correct_execution() throws Exception {

		myComponent.create(null);

		assertThat(caseVerifier.isBefore(), is(true));
		assertThat(caseVerifier.isAfter(), is(true));
		assertThat(caseVerifier.isExpected(), is(false));
		assertThat(caseVerifier.isUnexpected(), is(false));
	}

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Test
	public void not_capture_any_other_exception() throws Exception {
		expectedException.expect(IllegalArgumentException.class);

		myComponent.create(new IllegalArgumentException());

		assertThat(caseVerifier.isBefore(), is(true));
		assertThat(caseVerifier.isAfter(), is(false));
		assertThat(caseVerifier.isExpected(), is(false));
		assertThat(caseVerifier.isUnexpected(), is(true));
	}

	private void assertCapturedException() {
		assertThat(caseVerifier.isBefore(), is(true));
		assertThat(caseVerifier.isAfter(), is(false));
		assertThat(caseVerifier.isExpected(), is(true));
		assertThat(caseVerifier.isUnexpected(), is(false));
	}

}
