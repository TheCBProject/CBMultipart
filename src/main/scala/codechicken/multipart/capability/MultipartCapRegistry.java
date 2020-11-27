//package codechicken.multipart;
//
//import com.google.common.base.Preconditions;
//import net.minecraftforge.common.capabilities.Capability;
//import scala.Function1;
//import scala.collection.Iterable;
//import scala.collection.JavaConverters;
//
//import java.util.HashMap;
//import java.util.Map;
//import java.util.function.Function;
//
///**
// * This class is used to register handlers who's soul purpose is to merge capabilities together,
// * For Example, all Parts in the space with IItemHandlers on the 'WEST' face will be merged into
// * a single inventory and so on. If a handler is not registered for a capability, the first part
// * in the list is used.
// *
// * Created by covers1624 on 6/10/18.
// */
//public class MultipartCapRegistry {
//
//    private static final Map<Capability<?>, Function<?, ?>> mergers = new HashMap<>();
//
//    /**
//     * Registers a Java based Capability Merger.
//     * This is the same as {@link #registerSCapMerger(Capability, Function1)} except with a Java Iterable.
//     *
//     * @param cap  The capability.
//     * @param func The function to merge the capabilities together.
//     * @param <T>  Type.
//     */
//    public static <T> void registerCapMerger(Capability<T> cap, Function<java.lang.Iterable<T>, T> func) {
//        mergers.putIfAbsent(cap, (Function<Iterable<T>, T>)iter -> func.apply(JavaConverters.asJavaIterableConverter(iter).asJava()));
//    }
//
//    /**
//     * Registers a Scala based Capability Merger.
//     * This is the same as {@link #registerCapMerger(Capability, Function)} except with a Scala Iterable.
//     *
//     * @param cap  The capability.
//     * @param func The function to merge the capabilities together.
//     * @param <T>  Type.
//     */
//    public static <T> void registerSCapMerger(Capability<T> cap, Function1<Iterable<T>, T> func) {
//        mergers.putIfAbsent(cap, (Function<Iterable<T>, T>) func::apply);
//    }
//
//    /**
//     * This is an internal function, Called to actually merge the caps together.
//     *
//     * @param cap   The cap.
//     * @param impls The capability implementations.
//     * @return The single merged cap.
//     */
//    @SuppressWarnings ("unchecked")
//    public static Object merge(Capability cap, Iterable<Object> impls) {
//        Preconditions.checkArgument(impls.nonEmpty(), "Expected Non-Empty iterable.");
//
//        Function<Iterable<Object>, Object> func = (Function<Iterable<Object>, Object>) mergers.get(cap);
//        if (func == null) {
//            return impls.head();
//        }
//        return func.apply(impls);
//    }
//
//}
