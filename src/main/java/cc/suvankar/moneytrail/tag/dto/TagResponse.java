package cc.suvankar.moneytrail.tag.dto;

import cc.suvankar.moneytrail.tag.Tag;

public record TagResponse(Long tagId, String tagName) {
  public static TagResponse from(Tag tag) {
    return new TagResponse(tag.getId(), tag.getTagName());
  }
}
