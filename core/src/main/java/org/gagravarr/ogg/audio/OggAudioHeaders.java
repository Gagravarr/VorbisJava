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
 * TODO Have the various formats implement this
 * TODO Have a stream based bean for this
 * TODO Have a Factory which can build these generically
 */
public interface OggAudioHeaders {
    public int getSid();
    public OggAudioInfoHeader getInfo();
    public OggAudioTagsHeader getTags(); // TODO Tags or Comments?
    public OggAudioSetupHeader getSetup();
}
