package com.javadesign.addressbook.storage;

import com.javadesign.addressbook.model.Contact;
import com.javadesign.addressbook.model.Group;

import java.util.ArrayList;
import java.util.List;

/**
 * 持久化层 DTO：与 {@code addressbook.json} 根结构一一对应。
 * <p><b>字段</b>：{@code contacts}、{@code groups} 两个列表，由 Jackson 序列化。</p>
 * <p><b>检查点</b>：新增字段时需同步 JSON 样例与 {@link com.javadesign.addressbook.storage.JsonAddressBookStorage}。</p>
 */
public final class AddressBookData {

    private List<Contact> contacts = new ArrayList<>();
    private List<Group> groups = new ArrayList<>();

    public List<Contact> getContacts() {
        return contacts;
    }

    public void setContacts(List<Contact> contacts) {
        this.contacts = contacts != null ? contacts : new ArrayList<>();
    }

    public List<Group> getGroups() {
        return groups;
    }

    public void setGroups(List<Group> groups) {
        this.groups = groups != null ? groups : new ArrayList<>();
    }
}
