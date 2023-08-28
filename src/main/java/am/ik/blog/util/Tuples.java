package am.ik.blog.util;

public class Tuples {

	public static <T1, T2> Tuple2<T1, T2> of(T1 t1, T2 t2) {
		return new Tuple2<>(t1, t2);
	}

	/**
	 * Create a {@link Tuple3} with the given objects.
	 * @param t1 The first value in the tuple. Not null.
	 * @param t2 The second value in the tuple. Not null.
	 * @param t3 The third value in the tuple. Not null.
	 * @param <T1> The type of the first value.
	 * @param <T2> The type of the second value.
	 * @param <T3> The type of the third value.
	 * @return The new {@link Tuple3}.
	 */
	public static <T1, T2, T3> Tuple3<T1, T2, T3> of(T1 t1, T2 t2, T3 t3) {
		return new Tuple3<>(t1, t2, t3);
	}

}
