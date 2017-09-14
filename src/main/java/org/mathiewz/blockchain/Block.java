package org.mathiewz.blockchain;

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
public class Block<T extends Serializable> implements Iterable<Block<T>>, Serializable {
    
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
     * @return
     */
    public T getData() {
        return data;
    }
    
    /**
     * Return the previous block in the chain. If the current block is the first, it return itself.
     *
     * @return
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
    
    /**
     * Return the best blockchain.
     * A blockchain is better than another if it is the only one valid and if its length is bigger.
     *
     * @param firstChain
     *            the first blockchain to compare
     * @param secondChain
     *            the second blockchain to compare
     * @return the best blockchain
     */
    public static <T extends Serializable> Block<T> mergeChains(Block<T> firstChain, Block<T> secondChain) {
        boolean firstChainValid = firstChain == null ? false : firstChain.isWholeChainValid();
        boolean secondChainValid = secondChain == null ? false : secondChain.isWholeChainValid();
        if (!firstChainValid || !secondChainValid) {
            return secondChainValid ? secondChain : firstChain;
        }
        return firstChain.index >= secondChain.index ? firstChain : secondChain;
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
    
}
