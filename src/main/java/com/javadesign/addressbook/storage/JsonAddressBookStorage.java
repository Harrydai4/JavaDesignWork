package com.javadesign.addressbook.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * JSON 文件持久化实现。
 * <p>
 * <b>职责</b>：将 {@link AddressBookData} 读写至磁盘；默认目录为 {@code 用户主目录/.javadesign-addressbook/}，
 * 主文件名为 {@code addressbook.json}。
 * </p>
 * <p><b>检查点</b>：首次保存前创建父目录；反序列化失败时由调用方捕获 {@link IOException} 并向用户中文提示。</p>
 */
public final class JsonAddressBookStorage {

    private final Path file;
    private final ObjectMapper mapper;

    /**
     * @param baseDir 数据目录根路径（不含文件名）
     */
    public JsonAddressBookStorage(Path baseDir) {
        this.file = baseDir.resolve("addressbook.json");
        this.mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    /** @return 与 {@link #JsonAddressBookStorage(Path)} 配合使用的默认数据目录 */
    public static Path defaultBaseDir() {
        String home = System.getProperty("user.home");
        return Path.of(home, ".javadesign-addressbook");
    }

    /** @return 当前 JSON 文件绝对路径（用于照片目录相对定位等） */
    public Path getDataFile() {
        return file;
    }

    /**
     * 从磁盘加载；文件不存在时返回空数据（非错误）。
     *
     * @return 内存中的通讯录快照
     */
    public AddressBookData load() throws IOException {
        if (!Files.isRegularFile(file)) {
            return new AddressBookData();
        }
        return mapper.readValue(file.toFile(), AddressBookData.class);
    }

    /**
     * 整表写回磁盘（覆盖原文件）。
     *
     * @param data 当前内存中的全量数据，不可为 null
     */
    public void save(AddressBookData data) throws IOException {
        Files.createDirectories(file.getParent());
        mapper.writeValue(file.toFile(), data);
    }
}
