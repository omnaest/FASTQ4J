# FASTQ4J
Java Utilities for the FASTQ file format 

## Examples

    Stream<Sequence> sequences = FASTQUtils.read()
                                           .from(new File("some_file.fastq"))
                                           .getSequences();                                      

# Maven Snapshots

    <dependency>
      <groupId>org.omnaest.genomics</groupId>
      <artifactId>FASTQ4J</artifactId>
      <version>0.0.1-SNAPSHOT</version>
    </dependency>
    
    <repositories>
        <repository>
            <id>ossrh</id>
            <url>https://oss.sonatype.org/content/repositories/snapshots</url>
            <snapshots>
                <enabled>true</enabled>
            </snapshots>
        </repository>
    </repositories>