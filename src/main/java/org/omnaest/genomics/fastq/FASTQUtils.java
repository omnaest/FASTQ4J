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
package org.omnaest.genomics.fastq;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.apache.commons.io.input.BOMInputStream;
import org.apache.commons.lang.StringUtils;
import org.omnaest.genomics.fastq.domain.CodeAndQuality;
import org.omnaest.genomics.fastq.domain.FASTQData;
import org.omnaest.genomics.fastq.domain.Sequence;
import org.omnaest.genomics.fastq.utils.ExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FASTQUtils
{
    private static final Logger LOG = LoggerFactory.getLogger(FASTQUtils.class);

    public static interface FASTQReader
    {
        /**
         * Reads the {@link FASTQData} from a given {@link File}
         * 
         * @see #usingEncoding(Charset)
         * @param file
         * @return
         * @throws FileNotFoundException
         */
        public FASTQData from(File file) throws FileNotFoundException;

        /**
         * Similar to {@link #from(File)} but for multiple {@link File}s
         * 
         * @param files
         * @return
         */
        public FASTQData from(File... files);

        /**
         * Reads the {@link FASTQData} from a given {@link InputStream}
         * 
         * @see #usingEncoding(Charset)
         * @param inputStream
         * @return
         */
        public FASTQData from(InputStream inputStream);

        /**
         * Reads the {@link FASTQData} from a given {@link Reader}
         * 
         * @param reader
         * @return
         */
        public FASTQData from(Reader reader);

        /**
         * Defines an {@link ExceptionHandler} for any occuring {@link IOException}s. As default a {@link IllegalStateException} is thrown
         * 
         * @param exceptionHandler
         * @return
         */
        public FASTQReader usingExceptionHandler(ExceptionHandler exceptionHandler);

        /**
         * Sets the {@link Charset} encoding for {@link InputStream} and {@link File}s. As default {@link StandardCharsets#UTF_8} is used
         * 
         * @param encoding
         * @return
         */
        public FASTQReader usingEncoding(Charset encoding);

    }

    public static FASTQReader read()
    {
        return new FASTQReader()
        {
            private ExceptionHandler exceptionHandler = e ->
                                                      {
                                                          throw new IllegalStateException(e);
                                                      };
            private Charset          encoding         = StandardCharsets.UTF_8;

            @Override
            public FASTQReader usingExceptionHandler(ExceptionHandler exceptionHandler)
            {
                this.exceptionHandler = exceptionHandler;
                return this;
            }

            @Override
            public FASTQReader usingEncoding(Charset encoding)
            {
                this.encoding = encoding;
                return this;
            }

            @Override
            public FASTQData from(File file) throws FileNotFoundException
            {
                return this.from(new FileInputStream(file));
            }

            @Override
            public FASTQData from(File... files)
            {
                return this.from(Stream.of(files)
                                       .flatMap(file ->
                                       {
                                           try
                                           {
                                               return this.from(file)
                                                          .getSequences();
                                           }
                                           catch (FileNotFoundException e)
                                           {
                                               throw new IllegalStateException(e);
                                           }
                                       }));
            }

            @Override
            public FASTQData from(InputStream inputStream)
            {
                return this.from(new InputStreamReader(new BOMInputStream(inputStream), this.encoding));
            }

            @Override
            public FASTQData from(Reader reader)
            {
                Stream<Sequence> sequences = read(reader, this.exceptionHandler);
                return this.from(sequences);

            }

            private FASTQData from(Stream<Sequence> sequences)
            {
                return new FASTQData()
                {
                    @Override
                    public Stream<Sequence> getSequences()
                    {
                        return sequences;
                    }
                };
            }

        };
    }

    public static Stream<Sequence> read(InputStream inputStream)
    {
        return read(new InputStreamReader(new BOMInputStream(inputStream), StandardCharsets.UTF_8));
    }

    public static Stream<Sequence> read(Reader reader)
    {
        return read(reader, (e) -> LOG.error("Encountered exception", e));
    }

    public static Stream<Sequence> read(Reader reader, ExceptionHandler exceptionHandler)
    {
        BufferedReader lineReader = new BufferedReader(reader);

        SquenceFilterAndMapper squenceFilterAndMapper = new SquenceFilterAndMapper();

        return lineReader.lines()
                         .filter(squenceFilterAndMapper)
                         .map(squenceFilterAndMapper)
                         .onClose(() ->
                         {
                             try
                             {
                                 lineReader.close();
                             }
                             catch (IOException e)
                             {
                                 if (exceptionHandler != null)
                                 {
                                     exceptionHandler.handle(e);
                                 }
                             }
                         });
    }

    protected static class SquenceFilterAndMapper implements Predicate<String>, Function<String, Sequence>
    {
        private String id           = null;
        private String description  = null;
        private String rawSequence  = null;
        private String id2          = null;
        private String description2 = null;
        private String rawQuality   = null;

        @Override
        public boolean test(String line)
        {
            this.parseLine(line);

            return this.id != null && this.rawSequence != null && this.rawQuality != null;
        }

        private void parseLine(String line)
        {
            if (StringUtils.isNotBlank(line))
            {
                if (this.id == null)
                {
                    this.parsePrimaryId(line);
                }
                else if (this.rawSequence == null)
                {
                    this.rawSequence = line;
                }
                else if (this.id2 == null)
                {
                    this.id2 = line;
                }
                else if (this.rawQuality == null)
                {
                    this.rawQuality = line;
                }
            }
        }

        private void parsePrimaryId(String line)
        {
            this.id = StringUtils.removeStart(line, "@");
        }

        @Override
        public Sequence apply(String line)
        {
            String id = this.id;
            List<CodeAndQuality> sequence = this.parseCodeAndQualitySequence();
            this.reset();
            return new Sequence(id, sequence);
        }

        private List<CodeAndQuality> parseCodeAndQualitySequence()
        {
            //
            this.assertCorrectRawSequenceAndQualitySequenceLength();

            //
            List<CodeAndQuality> retlist = new ArrayList<>();
            for (int ii = 0; ii < this.rawSequence.length(); ii++)
            {
                String code = StringUtils.substring(this.rawSequence, ii, ii + 1);
                String quality = StringUtils.substring(this.rawQuality, ii, ii + 1);
                retlist.add(new CodeAndQuality(code, quality));
            }
            return retlist;
        }

        private void assertCorrectRawSequenceAndQualitySequenceLength()
        {
            int length1 = this.rawSequence.length();
            int length2 = this.rawQuality.length();
            if (length1 != length2)
            {
                throw new IllegalStateException("Raw sequence length and quality length does not equal:" + this.rawSequence + "<>" + this.rawQuality);
            }
        }

        private void reset()
        {
            this.id = null;
            this.id2 = null;
            this.description = null;
            this.rawSequence = null;
            this.rawQuality = null;
            this.description2 = null;
        }

    }
}
