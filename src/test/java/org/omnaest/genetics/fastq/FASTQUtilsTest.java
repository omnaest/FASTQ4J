/*

	Copyright 2017 Danny Kunz

	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at

		http://www.apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.


*/
package org.omnaest.genetics.fastq;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Ignore;
import org.junit.Test;
import org.omnaest.genetics.fastq.domain.Sequence;
import org.omnaest.utils.FileUtils;

/**
 * @see FASTQUtils
 * @author Omnaest
 */
public class FASTQUtilsTest
{
    @Test
    public void testReadInputStream() throws Exception
    {
        Stream<Sequence> sequences = FASTQUtils.read()
                                               .from(this.getClass()
                                                         .getResourceAsStream("/example2.fastq"))
                                               .getSequences();
        List<Sequence> collected = sequences.collect(Collectors.toList());
        assertEquals(2, collected.size());
        assertEquals("GGGTGATGGCCGCTGCCGATGGCGTCAAATCCCACCAAGTTACCCTTAACAACTTAAGGGTTTTCAAATAGA", collected.get(0)
                                                                                                          .asCodeSequence());
        assertEquals("GTTCAGGGATACGACGTTTGTATTTTAAGAATCTGAAGCAGAAGTCGATGATAATACGCGTCGTTTTATCAT", collected.get(1)
                                                                                                          .asCodeSequence());
        assertEquals("I", collected.get(0)
                                   .getSequence()
                                   .get(0)
                                   .getQuality());
        assertEquals("/", collected.get(0)
                                   .getSequence()
                                   .get(71)
                                   .getQuality());
        assertEquals(40, collected.get(0)
                                  .getSequence()
                                  .get(0)
                                  .getQualityQuantifier());
        assertEquals(14, collected.get(0)
                                  .getSequence()
                                  .get(71)
                                  .getQualityQuantifier());

    }

    @Test
    @Ignore
    public void testReadInputStream2() throws Exception
    {
        Stream<Sequence> sequences = FASTQUtils.read()
                                               .from(new File("C:\\Z\\data\\4\\raw\\55101705103780_read1.fastq"))
                                               .getSequences()
                                               .limit(10000);
        List<Sequence> collected = sequences.collect(Collectors.toList());

        FileUtils.toStreamConsumer(new File("C:\\Z\\data\\4\\raw\\55101705103780_read1_sample.fastq"))
                 .accept(collected.stream()
                                  .map(sequence -> sequence.asCodeSequence()));

    }

}
