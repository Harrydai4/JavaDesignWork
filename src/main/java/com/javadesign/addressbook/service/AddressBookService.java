package com.javadesign.addressbook.service;

import com.javadesign.addressbook.model.Contact;
import com.javadesign.addressbook.model.Group;
import com.javadesign.addressbook.storage.AddressBookData;
import com.javadesign.addressbook.storage.JsonAddressBookStorage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 应用服务门面：统一管理内存中的 {@link AddressBookData} 与磁盘读写。
 * <p>
 * <b>职责</b>：联系人/分组的增删改查、分组关系、导入合并策略；UI 层只与本类交互，不直接操作 JSON。
 * </p>
 * <p><b>检查点</b>：{@link #removeGroup(String)} 必须不删除联系人；{@link #mergeImportedContacts} 去重规则为姓名+手机，论文中可说明。</p>
 */
public final class AddressBookService {

    private final JsonAddressBookStorage storage;
    /** 当前会话内存数据，与界面显示一致；{@link #save()} 时写盘。 */
    private AddressBookData data = new AddressBookData();

    /**
     * @param storage 非 null；决定 json 文件路径
     */
    public AddressBookService(JsonAddressBookStorage storage) {
        this.storage = Objects.requireNonNull(storage);
    }

    /** 启动时加载；若文件损坏将抛 IOException，由入口类弹窗提示。 */
    public void load() throws IOException {
        data = storage.load();
    }

    /** 将当前内存数据持久化；失败时 UI 应捕获并中文提示用户。 */
    public void save() throws IOException {
        storage.save(data);
    }

    public List<Group> listGroups() {
        return new ArrayList<>(data.getGroups());
    }

    public List<Contact> listAllContacts() {
        return new ArrayList<>(data.getContacts());
    }

    /**
     * 按分组筛选联系人。
     *
     * @param groupId 分组主键；若为 {@code null} 则与「全部」一致，返回所有联系人
     * @return 新列表，调用方可安全修改不影响内部集合迭代策略
     */
    public List<Contact> listContactsInGroup(String groupId) {
        if (groupId == null) {
            return listAllContacts();
        }
        List<Contact> out = new ArrayList<>();
        for (Contact c : data.getContacts()) {
            if (c.getGroupIds().contains(groupId)) {
                out.add(c);
            }
        }
        return out;
    }

    public void addGroup(Group g) {
        data.getGroups().add(g);
    }

    /**
     * 删除分组：不删除联系人，仅从各联系人中移除该组 id。
     */
    public void removeGroup(String groupId) {
        data.getGroups().removeIf(g -> g.getId().equals(groupId));
        for (Contact c : data.getContacts()) {
            c.getGroupIds().remove(groupId);
        }
    }

    public Optional<Group> findGroup(String id) {
        return data.getGroups().stream().filter(g -> g.getId().equals(id)).findFirst();
    }

    public void addContact(Contact c) {
        data.getContacts().add(c);
    }

    public void updateContact(Contact c) {
        for (int i = 0; i < data.getContacts().size(); i++) {
            if (data.getContacts().get(i).getId().equals(c.getId())) {
                data.getContacts().set(i, c);
                return;
            }
        }
    }

    public void removeContact(String contactId) {
        data.getContacts().removeIf(c -> c.getId().equals(contactId));
    }

    public Optional<Contact> findContact(String id) {
        return data.getContacts().stream().filter(c -> c.getId().equals(id)).findFirst();
    }

    /**
     * 将联系人加入指定分组（已存在则 Set 去重）。
     * <p><b>检查点</b>：仅当分组 id 在系统中存在时才添加，避免脏 id。</p>
     *
     * @param contacts 界面选中的联系人（可能为列表视图中的快照）
     * @param groupIds   目标分组 id 列表
     */
    public void addContactsToGroups(List<Contact> contacts, List<String> groupIds) {
        for (Contact c : contacts) {
            Contact real = findContact(c.getId()).orElse(c);
            for (String gid : groupIds) {
                if (data.getGroups().stream().anyMatch(g -> g.getId().equals(gid))) {
                    real.getGroupIds().add(gid);
                }
            }
        }
    }

    public void removeContactsFromGroup(List<Contact> contacts, String groupId) {
        for (Contact c : contacts) {
            findContact(c.getId()).ifPresent(rc -> rc.getGroupIds().remove(groupId));
        }
    }

    /**
     * 追加导入：为每条导入记录生成新 {@link Contact#Contact()} 以分配新 id。
     * <p>去重：姓名 trim 后相同且手机 trim 后相同则跳过（检查点：避免重复刷屏）。</p>
     *
     * @param imported 解析自 CSV/vCard 的临时对象列表
     */
    public void mergeImportedContacts(List<Contact> imported) {
        for (Contact in : imported) {
            boolean dup = data.getContacts().stream().anyMatch(
                    c -> Objects.equals(trim(c.getName()), trim(in.getName()))
                            && Objects.equals(trim(c.getMobile()), trim(in.getMobile()))
                            && !trim(in.getName()).isEmpty());
            if (!dup) {
                data.getContacts().add(copyAsNewContact(in));
            }
        }
    }

    /** 复制字段到新实例，由新构造器生成 id。 */
    private static Contact copyAsNewContact(Contact in) {
        Contact c = new Contact();
        c.setName(in.getName());
        c.setPhone(in.getPhone());
        c.setMobile(in.getMobile());
        c.setInstantMessaging(in.getInstantMessaging());
        c.setEmail(in.getEmail());
        c.setHomepage(in.getHomepage());
        c.setBirthday(in.getBirthday());
        c.setPhotoPath(in.getPhotoPath());
        c.setWorkUnit(in.getWorkUnit());
        c.setHomeAddress(in.getHomeAddress());
        c.setPostalCode(in.getPostalCode());
        c.setRemark(in.getRemark());
        c.setGroupIds(in.getGroupIds());
        return c;
    }

    private static String trim(String s) {
        return s == null ? "" : s.trim();
    }

    public String groupName(String groupId) {
        return findGroup(groupId).map(Group::getName).orElse("");
    }

    public List<Contact> exportContactsSelection(boolean all, List<Contact> selected) {
        if (all) {
            return listAllContacts();
        }
        return new ArrayList<>(selected);
    }

    public JsonAddressBookStorage getStorage() {
        return storage;
    }
}
