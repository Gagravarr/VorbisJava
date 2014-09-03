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
package org.gagravarr.ogg.audio;

import org.gagravarr.ogg.OggAudioStream;

/**
 * Interface for reading the headers at the start of an
 *  {@link OggAudioStream}
 *
 * TODO Have a stream based bean for this
 * TODO Have a Factory which can build these generically
 */
public interface OggAudioHeaders {
    /**
     * @return The stream id of the overall audio stream
     */
    public int getSid();
    /**
     * @return The information / identification of the stream and audio encoding
     */
    public OggAudioInfoHeader getInfo();
    /**
     * @return The Tags / Comments describing the stream
     */
    public OggAudioTagsHeader getTags();
    /**
     * @return The Setup information for the audio encoding, if used in the format
     */
    public OggAudioSetupHeader getSetup();
}
