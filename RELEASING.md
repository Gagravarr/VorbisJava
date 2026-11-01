Before the release:
 * Ensure `CHANGES.txt` is up-to-date
 * Try running Apache Tika with a manual build of the new version, and
   ensure all Tika tests pass
 * Update the version strings and dates in the files which embed them:
   * `core/src/main/java/org/gagravarr/speex/SpeexInfo.java`
   * `core/src/main/java/org/gagravarr/vorbis/VorbisStyleComments.java`
 * Check https://central.sonatype.com/usertoken and update
   `~/.m2/settings.xml` with a new Token if the current one has expired
   (monthly tokens are recommended for security reasons!)

To release:
 * See the following Sonatype Guides and Instructions
   * https://maven.apache.org/repository/guide-central-repository-upload.html
   * https://central.sonatype.org/publish/publish-portal-maven/
   * https://central.sonatype.org/publish/requirements/
 * Key steps are:
  * mvn -DdryRun=true release:clean release:prepare
  * mvn release:clean release:prepare
  * mvn release:perform

After the release:
 * Add a new section in the CHANGES file
 * Upgrade the dependency in Apache Tika
