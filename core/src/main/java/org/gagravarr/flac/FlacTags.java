/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gagravarr.flac;

import org.gagravarr.ogg.IOUtils;
import org.gagravarr.vorbis.VorbisComments;


/**
 * This is a {@link VorbisComments} with a Flac metadata
 *  block header, rather than the usual vorbis one.
 */
public class FlacTags extends VorbisComments {

	/**
	 * Instead of "#vorbis" we have "#<len>"
	 */
	@Override
	protected int getDataBeginsAt() {
		return 4;
	}

	/**
	 * Instead of "#vorbis" we have "#<len>"
	 * We ignore the type, as we have our own one
	 */
	@Override
	protected void populateStart(byte[] b, int type) {
		b[0] = IOUtils.fromInt( FlacMetadataBlock.VORBIS_COMMENT );
		IOUtils.putInt3(b, 1, getData().length-4);
	}
}
