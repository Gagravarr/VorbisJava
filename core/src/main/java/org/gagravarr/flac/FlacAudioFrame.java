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


/**
 * Raw, compressed audio data.
 * TODO Parse into constituent parts
 */
public class FlacAudioFrame extends FlacFrame {
   private byte[] data; // TODO Parse
   private long position; // TODO Is this always there?
   
   public FlacAudioFrame(byte[] data) {
      this.data = data;
   }
   // TODO InputStream based constructor?
   
   public byte[] getData() {
      return data;
   }
}
