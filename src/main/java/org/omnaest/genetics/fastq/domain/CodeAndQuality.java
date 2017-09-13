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

import org.apache.commons.lang.StringUtils;

public class CodeAndQuality
{
	private static final String	QUALITIES				= "!\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~";
	public static final int		MAX_QUALITY_QUANTIFIER	= QUALITIES.length() - 1;

	private String	code;
	private String	quality;

	public CodeAndQuality(String code, String quality)
	{
		super();
		this.code = code;
		this.quality = quality;
	}

	public String getCode()
	{
		return this.code;
	}

	public String getQuality()
	{
		return this.quality;
	}

	/**
	 * Returns higher numbers for better qualities. Starts with 0 and ends with {@value #MAX_QUALITY_QUANTIFIER}
	 *
	 * @see #MAX_QUALITY_QUANTIFIER
	 * @return
	 */
	public int getQualityQuantifier()
	{
		return this.getQualityQuantifier(QUALITIES);
	}

	public int getQualityQuantifier(String qualities)
	{
		return StringUtils.indexOf(qualities, this.quality);
	}

	@Override
	public String toString()
	{
		return "[code=" + this.code + ", quality=" + this.quality + "]";
	}

}
