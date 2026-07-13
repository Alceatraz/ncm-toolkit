package top.btswork.ncmtoolkit.libncm.api.core

interface NcmApiDelegateFactory {

}

@JvmInline value class AlbumID(val value: String)
@JvmInline value class TrackID(val value: String)
@JvmInline value class ArtistID(val value: String)

abstract class NcmApiDelegate {

  fun fetchAlbumInfo(id: AlbumID) {

  }

  protected abstract fun fetchArtistInfo(id: ArtistID): ByteArray
  protected abstract fun parseArtistInfo(raw: ByteArray): ByteArray

}

// @JvmInline value class AlbumID(val value: String)

// data class TrackInfo()

sealed class Node {
  val children = mutableListOf<Node>() // 树结构，不是属性
}

sealed class StructuralNode : Node() // structural class
sealed class AuxiliaryNode : Node()  // auxiliary class (接口)

class Top : AuxiliaryNode() // abstract
class Person : StructuralNode()
class OrganizationalPerson : StructuralNode()
class InetOrgPerson : StructuralNode()

class OrganizationalUnit : StructuralNode() // OU
class DomainComponent : AuxiliaryNode()      // dcObject

