package de.deverado.framework.core;

import static java.util.Arrays.asList;

import com.google.common.base.Function;
import com.google.common.collect.Ordering;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.List;

public class ExceptionsHelper {

	private static final Logger log = LoggerFactory
			.getLogger(ExceptionsHelper.class);

	/* **************************************
	 * Code from Google Guava Futures class:
	 * (Ported here because they are hidden in guava.)
	 */

	public static <X extends Throwable> X newWithCause(Class<X> exceptionClass,
			Throwable cause) {
		return newWithCause(exceptionClass, cause, null);
	}

	/**
	 * 
	 * @param exceptionClass
	 * @param cause
	 * @param message
	 *            if <code>null</code>, the new exception gets
	 *            'Cause.Class.Name: Cause.Message' as message.
	 * @return
	 */
	public static <X extends Throwable> X newWithCause(Class<X> exceptionClass,
			Throwable cause, @Nullable String message) {
		// getConstructors() guarantees this as long as we don't modify the
		// array.
		@SuppressWarnings({ "unchecked", "rawtypes" })
		List<Constructor<X>> constructors = (List) Arrays.asList(exceptionClass
				.getConstructors());
		for (Constructor<X> constructor : preferringStringsWithThrowables(constructors)) {
			@Nullable
			X instance = newFromConstructor(constructor, cause, message);
			if (instance != null) {
				if (instance.getCause() == null) {
					instance.initCause(cause);
				}
				return instance;
			}
		}
		throw new IllegalArgumentException(
				"No appropriate constructor for exception of type "
						+ exceptionClass + " in response to chained exception",
				cause);
	}

	protected static <X extends Throwable> List<Constructor<X>> preferringStringsWithThrowables(
			List<Constructor<X>> constructors) {
		return WITH_STRING_PARAM_FIRST.sortedCopy(constructors);
	}

	private static final Ordering<Constructor<?>> WITH_STRING_PARAM_FIRST = Ordering
			.natural().onResultOf(new Function<Constructor<?>, Integer>() {
				@Override
				public Integer apply(Constructor<?> input) {
					int points = 0;
					List<Class<?>> list = asList(input.getParameterTypes());
					if (list.contains(String.class)) {
						points++;
						points++;
					}
					if (list.contains(Throwable.class)) {
						points++;
					}
					return points;
				}
			}).reverse();

	@Nullable
	private static <X> X newFromConstructor(Constructor<X> constructor,
			Throwable cause, @Nullable String message) {
		Class<?>[] paramTypes = constructor.getParameterTypes();
		Object[] params = new Object[paramTypes.length];
		for (int i = 0; i < paramTypes.length; i++) {
			Class<?> paramType = paramTypes[i];
			if (paramType.equals(String.class)) {
				if (message != null) {
					params[i] = message;
				} else {
					params[i] = cause != null ? cause.toString() : "";
				}
			} else if (paramType.equals(Throwable.class)) {
				params[i] = cause;
			} else {
				return null;
			}
		}
		try {
			return constructor.newInstance(params);
		} catch (Exception e) {
			log.debug("Cannot construct instance of {}", constructor, e);
			return null;
		}
	}

	/* *****************************************
	 * END Code from Google Guava Futures class
	 */

	public static String getStacktraceAsString(Throwable throwable) {
		StringWriter stringWriter = new StringWriter();
		PrintWriter pw = new PrintWriter(stringWriter);
		throwable.printStackTrace(pw);
		pw.close();
		return stringWriter.toString();
	}
}
