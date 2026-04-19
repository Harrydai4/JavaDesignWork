package com.javadesign.addressbook.service;

import com.javadesign.addressbook.model.Contact;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 搜索逻辑单元测试：拼音匹配与排序（不启动 Swing）。
 */
class ContactSearchTest {

    @Test
    void pinyinInitialsMatch() {
        Contact c = new Contact();
        c.setName("李小明");
        c.setMobile("13900001111");
        assertTrue(ContactSearch.matches(c, "lxm"));
        assertTrue(ContactSearch.matches(c, "li"));
    }

    @Test
    void sortByName() {
        Contact a = new Contact();
        a.setName("Bob");
        Contact b = new Contact();
        b.setName("Alice");
        List<Contact> r = ContactSearch.filter(List.of(a, b), "");
        assertEquals("Alice", r.get(0).getName());
        assertEquals("Bob", r.get(1).getName());
    }
}
