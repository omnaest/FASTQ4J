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
package org.omnaest.genetics.fastq.domain;

import java.util.List;
import java.util.stream.Collectors;

import org.omnaest.genetics.translator.domain.NucleicAcidCodeSequence;

/**
 * Representation of a single code sequence
 * 
 * @author omnaest
 */
public class Sequence
{
    private String               id;
    private List<CodeAndQuality> sequence;

    public Sequence(String id, List<CodeAndQuality> sequence)
    {
        super();
        this.id = id;
        this.sequence = sequence;
    }

    public List<CodeAndQuality> getSequence()
    {
        return this.sequence;
    }

    public String getId()
    {
        return this.id;
    }

    @Override
    public String toString()
    {
        return "Sequence [id=" + this.id + ", sequence=" + this.sequence + "]";
    }

    public String asCodeSequence()
    {
        return this.sequence.stream()
                            .map(cq -> cq.getCode())
                            .collect(Collectors.joining());
    }

    /**
     * Returns the {@link Sequence} as {@link NucleicAcidCodeSequence}
     * 
     * @return
     */
    public NucleicAcidCodeSequence asNucleicAcidCodeSequence()
    {
        return NucleicAcidCodeSequence.valueOf(this.asCodeSequence());
    }

}
