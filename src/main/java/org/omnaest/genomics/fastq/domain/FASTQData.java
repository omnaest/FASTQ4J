package org.omnaest.genomics.fastq.domain;

import java.util.stream.Stream;

/**
 * Representation of the data of a fastq file
 * 
 * @author omnaest
 */
public interface FASTQData
{
    /**
     * @see Sequence
     * @return
     */
    public Stream<Sequence> getSequences();
}
