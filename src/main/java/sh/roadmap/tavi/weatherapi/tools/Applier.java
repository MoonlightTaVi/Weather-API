package sh.roadmap.tavi.weatherapi.tools;


/**
 * A lambda function to change some object (target)
 * based on another object (applied value). Target object accepts
 * changes, then the function returns nothing.
 * @param <T> - target object to take changes
 * @param <N> - object, applied to the target, to change it
 */
@FunctionalInterface
public interface Applier<T, N> {
	public void apply(T toValue, N appliedValue);
}
