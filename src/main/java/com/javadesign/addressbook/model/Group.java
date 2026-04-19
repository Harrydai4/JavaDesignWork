package com.javadesign.addressbook.model;

import java.util.Objects;
import java.util.UUID;

/**
 * 领域模型：联系组。
 * <p><b>职责</b>：仅含 id 与名称；联系人与组的关联在 {@link Contact#getGroupIds()} 中维护。</p>
 * <p><b>检查点</b>：删除组时不得删除 {@link Contact}，仅由服务层从各联系人的 groupIds 中移除本组 id。</p>
 */
public final class Group {

    /** 分组主键。 */
    private String id;
    /** 分组显示名称（树节点、表格「分组」列）。 */
    private String name;

    public Group() {
        this.id = UUID.randomUUID().toString();
    }

    public Group(String id, String name) {
        this.id = id != null ? id : UUID.randomUUID().toString();
        this.name = name != null ? name : "";
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Group other)) {
            return false;
        }
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return name != null ? name : "";
    }
}
