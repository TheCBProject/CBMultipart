package codechicken.multipart.util;

import com.google.common.collect.Iterators;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;
import java.util.function.Predicate;

/**
 * Created by covers1624 on 1/1/21.
 */
public class FilteredCollectionView<E> extends AbstractCollection<E> {

    private final Collection<E> other;
    private final Predicate<E> filter;
    private final int size;

    public FilteredCollectionView(Collection<E> other, Predicate<E> filter) {
        this.other = other;
        this.filter = filter;
        this.size = Iterators.size(iterator());
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public Iterator<E> iterator() {
        return Iterators.filter(other.iterator(), filter::test);
    }
}
