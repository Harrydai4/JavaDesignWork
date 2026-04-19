package com.javadesign.addressbook.model;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * 领域模型：联系人。
 * <p>
 * <b>职责</b>：承载课程要求全部业务字段；与 {@link com.javadesign.addressbook.model.Group}
 * 通过 {@code groupIds} 多对多关联（仅存分组主键）。
 * </p>
 * <p>
 * <b>检查点</b>：{@code id} 在构造时生成 UUID，持久化后用于更新与删除；
 * setter 对字符串统一做空安全处理，避免界面与 JSON 出现 {@code null} 导致 NPE。
 * </p>
 */
public final class Contact {

    /** 唯一标识，持久化后主键。 */
    private String id;
    /** 姓名（搜索、排序、拼音均依赖此字段）。 */
    private String name = "";
    /** 固定电话。 */
    private String phone = "";
    /** 手机号码。 */
    private String mobile = "";
    /** 即时通信工具及号码，如 微信:abc123 */
    private String instantMessaging = "";
    private String email = "";
    private String homepage = "";
    private LocalDate birthday;
    /** 本地照片路径（应用数据目录内相对或绝对路径） */
    private String photoPath = "";
    private String workUnit = "";
    private String homeAddress = "";
    private String postalCode = "";
    private String remark = "";
    /** 所属分组 id 集合（多对多） */
    private Set<String> groupIds = new LinkedHashSet<>();

    public Contact() {
        this.id = UUID.randomUUID().toString();
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
        this.name = name != null ? name : "";
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone != null ? phone : "";
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile != null ? mobile : "";
    }

    public String getInstantMessaging() {
        return instantMessaging;
    }

    public void setInstantMessaging(String instantMessaging) {
        this.instantMessaging = instantMessaging != null ? instantMessaging : "";
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email != null ? email : "";
    }

    public String getHomepage() {
        return homepage;
    }

    public void setHomepage(String homepage) {
        this.homepage = homepage != null ? homepage : "";
    }

    public LocalDate getBirthday() {
        return birthday;
    }

    public void setBirthday(LocalDate birthday) {
        this.birthday = birthday;
    }

    public String getPhotoPath() {
        return photoPath;
    }

    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath != null ? photoPath : "";
    }

    public String getWorkUnit() {
        return workUnit;
    }

    public void setWorkUnit(String workUnit) {
        this.workUnit = workUnit != null ? workUnit : "";
    }

    public String getHomeAddress() {
        return homeAddress;
    }

    public void setHomeAddress(String homeAddress) {
        this.homeAddress = homeAddress != null ? homeAddress : "";
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode != null ? postalCode : "";
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark != null ? remark : "";
    }

    public Set<String> getGroupIds() {
        return groupIds;
    }

    public void setGroupIds(Set<String> groupIds) {
        this.groupIds = groupIds != null ? new LinkedHashSet<>(groupIds) : new LinkedHashSet<>();
    }

    public boolean inGroup(String groupId) {
        return groupIds.contains(groupId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Contact other)) {
            return false;
        }
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
