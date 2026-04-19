package com.javadesign.addressbook.storage;

import com.javadesign.addressbook.model.Contact;
import com.javadesign.addressbook.model.Group;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 持久化回归：验证 Jackson 与 {@link com.javadesign.addressbook.model.Contact} 字段可往返序列化。
 * <p>检查点：含分组 id 关联与 {@link java.time.LocalDate}。</p>
 */
class JsonAddressBookStorageTest {

    @Test
    void roundTrip(@TempDir Path temp) throws IOException {
        JsonAddressBookStorage st = new JsonAddressBookStorage(temp);
        AddressBookData data = new AddressBookData();
        Group g = new Group();
        g.setName("朋友");
        data.getGroups().add(g);
        Contact c = new Contact();
        c.setName("张三");
        c.setMobile("13800138000");
        c.setBirthday(LocalDate.of(2000, 1, 2));
        c.getGroupIds().add(g.getId());
        data.getContacts().add(c);

        st.save(data);
        AddressBookData loaded = st.load();
        assertEquals(1, loaded.getContacts().size());
        assertEquals("张三", loaded.getContacts().get(0).getName());
        assertEquals(1, loaded.getGroups().size());
        assertTrue(loaded.getContacts().get(0).getGroupIds().contains(g.getId()));
    }
}
