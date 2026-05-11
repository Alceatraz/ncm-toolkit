package top.btswork.ncmtoolkit.libtag.module.flac.schema.block

import top.btswork.ncmtoolkit.tool.io.stream.Reader
import top.btswork.ncmtoolkit.tool.io.stream.Writer

object VorbisCommentBlocks {

  fun getInstance(
    vendor: String,
    store: List<Pair<String, String>>,
  ) = VorbisCommentBlock(
    vendor,
    store
  )

  fun Reader.parse(): VorbisCommentBlock {
    TODO()
  }

  fun Writer.write(block: VorbisCommentBlock) {

  }

}

/**
 *
 * TITLE	曲名
 * VERSION	版本/混音信息
 * ALBUM	专辑名
 * TRACKNUMBER	曲目号
 * ARTIST	艺术家（流行音乐）/作曲家（古典）
 * PERFORMER	表演者（古典：指挥/乐团/独奏）
 * COPYRIGHT	版权信息
 * LICENSE	授权信息
 * ORGANIZATION	出版方（唱片公司）
 * DESCRIPTION	简短描述
 * GENRE	流派
 * DATE	录制日期
 * LOCATION	录制地点
 * CONTACT	联系方式
 * ISRC	国际标准录音代码
 *
 *
 * ALBUMARTIST	专辑艺术家（非常常用）
 * ALBUMARTISTSORT	排序用（如 Beatles, The → Beatles）
 * ALBUMSORT	专辑排序名
 *
 * TITLESORT	排序用标题
 * TRACKTOTAL / TOTALTRACKS	专辑总曲目数
 * DISCNUMBER	碟号
 * DISCTOTAL / TOTALDISCS	总碟数
 *
 *
 * ARTISTS	多艺术家列表（MusicBrainz）
 * ARTISTSORT	排序用艺术家名
 * COMPOSER	作曲家
 * COMPOSERSORT	排序用作曲家名
 * LYRICIST	作词者
 * CONDUCTOR	指挥
 * ENSEMBLE	乐团
 * ARRANGER	编曲
 *
 *
 * COMMENT	注释（最常见）
 * LYRICS	歌词（不推荐，见下）
 * UNSYNCEDLYRICS	无时间轴歌词（更常见）
 * SYNCLYRICS	有时间轴歌词
 *
 *
 * REPLAYGAIN_TRACK_GAIN	单曲增益
 * REPLAYGAIN_TRACK_PEAK	单曲峰值
 * REPLAYGAIN_ALBUM_GAIN	专辑增益
 * REPLAYGAIN_ALBUM_PEAK	专辑峰值
 *
 *
 * ENCODER	编码器名称
 * ENCODING	编码设置
 * ENCODERSETTINGS	编码参数
 *
 */
data class VorbisCommentBlock(
  val vendor: String,
  val store: List<Pair<String, String>>,
) : Block {
  override fun getType(): Byte = VORBIS_COMMENT
}