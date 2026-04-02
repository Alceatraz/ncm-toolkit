package top.btswork.ncmtoolkit.libncm.schema

@JvmInline value class MusicId(val value: String)
@JvmInline value class MusicName(val value: String)

@JvmInline value class ArtistId(val value: String)
@JvmInline value class ArtistName(val value: String)

@JvmInline value class AlbumId(val value: String)
@JvmInline value class Album(val value: String)
@JvmInline value class AlbumPicDocId(val value: String)
@JvmInline value class AlbumPic(val value: String)
@JvmInline value class Bitrate(val value: String)
@JvmInline value class Mp3DocId(val value: String)
@JvmInline value class Duration(val value: String)
@JvmInline value class MvId(val value: String)
@JvmInline value class Alias(val value: String)
@JvmInline value class TransNames(val value: String)
@JvmInline value class Format(val value: String)
@JvmInline value class Fee(val value: String)
@JvmInline value class VolumeDelta(val value: String)
@JvmInline value class Privilege(val value: String)

data class Artist(val id: ArtistId, val name: ArtistName)

fun String.toMusicId() = MusicId(this)
fun String.toMusicName() = MusicName(this)
fun String.toArtistId() = ArtistId(this)
fun String.toArtistName() = ArtistName(this)
fun String.toAlbumId() = AlbumId(this)
fun String.toAlbum() = Album(this)
fun String.toAlbumPicDocId() = AlbumPicDocId(this)
fun String.toAlbumPic() = AlbumPic(this)
fun String.toBitrate() = Bitrate(this)
fun String.toMp3DocId() = Mp3DocId(this)
fun String.toDuration() = Duration(this)
fun String.toMvId() = MvId(this)
fun String.toAlias() = Alias(this)
fun String.toTransNames() = TransNames(this)
fun String.toFormat() = Format(this)
fun String.toFee() = Fee(this)
fun String.toVolumeDelta() = VolumeDelta(this)
fun String.toPrivilege() = Privilege(this)

fun Pair<ArtistId, ArtistName>.toArtist() = Artist(first, second)

class NcmMetadata(
  val musicId: MusicId,
  val musicName: MusicName,
  val artist: Array<Artist>,
  val albumId: AlbumId,
  val album: Album,
  val albumPicDocId: AlbumPicDocId,
  val albumPic: AlbumPic,
  val bitrate: Bitrate,
  val mp3DocId: Mp3DocId,
  val duration: Duration,
  val mvId: MvId,
  val alias: Array<Alias>,
  val transNames: Array<TransNames>,
  val format: Format,
  val fee: Fee,
  val volumeDelta: VolumeDelta,
  val privilege: Privilege,
) {

  override fun toString(): String {
    return "NcmMetadata(musicId=$musicId, musicName=$musicName, artist=${artist.contentToString()}, albumId=$albumId, album=$album, albumPicDocId=$albumPicDocId, albumPic=$albumPic, bitrate=$bitrate, mp3DocId=$mp3DocId, duration=$duration, mvId=$mvId, alias=${alias.contentToString()}, transNames=${transNames.contentToString()}, format=$format, fee=$fee, volume=$volumeDelta, privilege=$privilege)"
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as NcmMetadata

    if (musicId != other.musicId) return false
    if (musicName != other.musicName) return false
    if (!artist.contentEquals(other.artist)) return false
    if (albumId != other.albumId) return false
    if (album != other.album) return false
    if (albumPicDocId != other.albumPicDocId) return false
    if (albumPic != other.albumPic) return false
    if (bitrate != other.bitrate) return false
    if (mp3DocId != other.mp3DocId) return false
    if (duration != other.duration) return false
    if (mvId != other.mvId) return false
    if (!alias.contentEquals(other.alias)) return false
    if (!transNames.contentEquals(other.transNames)) return false
    if (format != other.format) return false
    if (fee != other.fee) return false
    if (volumeDelta != other.volumeDelta) return false
    if (privilege != other.privilege) return false

    return true
  }

  override fun hashCode(): Int {
    var result = musicId.hashCode()
    result = 31 * result + musicName.hashCode()
    result = 31 * result + artist.contentHashCode()
    result = 31 * result + albumId.hashCode()
    result = 31 * result + album.hashCode()
    result = 31 * result + albumPicDocId.hashCode()
    result = 31 * result + albumPic.hashCode()
    result = 31 * result + bitrate.hashCode()
    result = 31 * result + mp3DocId.hashCode()
    result = 31 * result + duration.hashCode()
    result = 31 * result + mvId.hashCode()
    result = 31 * result + alias.contentHashCode()
    result = 31 * result + transNames.contentHashCode()
    result = 31 * result + format.hashCode()
    result = 31 * result + fee.hashCode()
    result = 31 * result + volumeDelta.hashCode()
    result = 31 * result + privilege.hashCode()
    return result
  }

}

class NcmMetadataBuilder {

  var musicId: MusicId? = null
  var musicName: MusicName? = null
  var artist: Array<Artist>? = null
  var albumId: AlbumId? = null
  var album: Album? = null
  var albumPicDocId: AlbumPicDocId? = null
  var albumPic: AlbumPic? = null
  var bitrate: Bitrate? = null
  var mp3DocId: Mp3DocId? = null
  var duration: Duration? = null
  var mvId: MvId? = null
  var alias: Array<Alias>? = null
  var transNames: Array<TransNames>? = null
  var format: Format? = null
  var fee: Fee? = null
  var volumeDelta: VolumeDelta? = null
  var privilege: Privilege? = null

  fun build() = NcmMetadata(
    musicId ?: error("musicId missing"),
    musicName ?: error("musicName missing"),
    artist ?: error("artist missing"),
    albumId ?: error("albumId missing"),
    album ?: error("album missing"),
    albumPicDocId ?: error("albumPicDocId missing"),
    albumPic ?: error("albumPic missing"),
    bitrate ?: error("bitrate missing"),
    mp3DocId ?: error("mp3DocId missing"),
    duration ?: error("duration missing"),
    mvId ?: error("mvId missing"),
    alias ?: error("aliases missing"),
    transNames ?: error("transNames missing"),
    format ?: error("format missing"),
    fee ?: error("fee missing"),
    volumeDelta ?: error("volume missing"),
    privilege ?: error("privilege missing"),
  )

}

fun NcmMetadata(block: NcmMetadataBuilder.() -> Unit) = NcmMetadataBuilder().apply(block).build()