package com.github.mathiewz.blockchain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * A block containing some data.
 * The data is stored in an object T
 * It is chained with other blocks.
 *
 * @param <T>
 *            The class of the data.
 */
public class Block<T extends Serializable> implements Iterable<Block<T>>, Comparable<Block<T>>, Serializable {
    
    private static final long serialVersionUID = 1L;

    private final int index;
    
    private final T data;
    
    private final Block<T> previous;

    private final int hash;
    
    /**
     * Create the first block of the chain.
     *
     * @param data
     *            The data contained in the block.
     */
    public Block(T data) {
        index = 0;
        this.data = data;
        previous = null;
        hash = hashCode();
    }
    
    /**
     * Create a new block in the chain.
     *
     * @param data
     *            The data contained in the block.
     * @param previous
     *            The previous block in the chain.
     */
    public Block(T data, Block<T> previous) {
        index = previous.index + 1;
        this.data = data;
        this.previous = previous;
        hash = hashCode();
    }
    
    /**
     * Return the data contained in the block.
     *
     * @return the data contained in the block.
     */
    public T getData() {
        return data;
    }
    
    /**
     * Return the previous block in the chain. If the current block is the first, it return itself.
     *
     * @return the previous block in the chain. If the current block is the first, it return itself.
     */
    public Block<T> getPrevious() {
        return previous;
    }
    
    /**
     * Check if the block is valid
     *
     * @return true if the data in the block are not altered
     */
    public boolean isValid() {
        return hash == hashCode();
    }
    
    /**
     * Returns a sequential Stream with this blockchain as its source.
     *
     * @return a sequential Stream with this blockchain as its source.
     */
    public Stream<Block<T>> stream() {
        return StreamSupport.stream(Spliterators.spliterator(iterator(), 0L, Spliterator.ORDERED), false);
    }
    
    /**
     * Check if the whole chain is valid
     *
     * @return true if all the previous block to the index 0 are valid
     */
    public boolean isWholeChainValid() {
        return this.stream().allMatch(Block::isValid);
    }
    
    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(data)
                .append(index)
                .append(index == 0 ? 0 : previous)
                .toHashCode();
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || !(obj instanceof Block)) {
            return false;
        }
        Block<T> other = (Block<T>) obj;
        return new EqualsBuilder()
                .append(data, other.data)
                .append(index, other.index)
                .append(index == 0 ? 0 : previous, index == 0 ? 0 : other.previous)
                .isEquals();
    }

    @Override
    public Iterator<Block<T>> iterator() {
        List<Block<T>> list = new ArrayList<>();

        Block<T> block = this;
        while (block != null) {
            list.add(block);
            block = block.previous;
        }
        return new BlockIterator(list.listIterator(list.size()));
    }

    @Override
    public String toString() {
        String lineStarter = "\n\t";
        return new ToStringBuilder(this)
                .append(lineStarter + "Index", index)
                .append(lineStarter + "Data", data)
                .append(lineStarter + "Hash", hash)
                .append(lineStarter + "Previous block hash", index == 0 ? 0 : previous.hash)
                .append(lineStarter + "Validity", isValid())
                .append(lineStarter + "Whole chain validity", isWholeChainValid())
                .build();
    }
    
    @Override
    public int compareTo(Block<T> o) {
        Boolean firstChainValid = this.isWholeChainValid();
        Boolean secondChainValid = o == null ? false : o.isWholeChainValid();
        if (!firstChainValid || !secondChainValid) {
            return Boolean.compare(firstChainValid, secondChainValid);
        }
        return Integer.compare(this.index, o.index);
    }

    private class BlockIterator implements Iterator<Block<T>> {
        
        ListIterator<Block<T>> itr;
        
        private BlockIterator(ListIterator<Block<T>> listIterator) {
            itr = listIterator;
        }

        @Override
        public boolean hasNext() {
            return itr.hasPrevious();
        }

        @Override
        public Block<T> next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            return itr.previous();
        }
        
    }

    /**
     * Compare two blocks and return the best one.
     * A block is better if it is valid and the bigger its index is.
     *
     * @param <T>
     *            the data contained in the two blocks
     * @param firstChain
     *            the first blockchain to compare
     * @param secondChain
     *            the second blockchain to compare
     * @return a negative value secondChain is better, a positive value if the firstChain is better, and 0 if it's not possible to determine which is the better chain.
     */
    public static <T extends Serializable> int compare(Block<T> firstChain, Block<T> secondChain) {
        if (firstChain == null) {
            return secondChain == null ? 0 : -1;
        }
        return firstChain.compareTo(secondChain);
    }
    
    /**
     * Check if any block of the chain contains a data.
     * It uses the method equals of T to check the equality.
     *
     * @param object
     *            the data to check
     * @return true if any data in the chain is equals to the specified data.
     */
    public boolean contains(T object) {
        return stream().anyMatch(data -> data.equals(object));
    }
}
