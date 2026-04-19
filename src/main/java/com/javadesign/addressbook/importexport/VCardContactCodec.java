package com.javadesign.addressbook.importexport;

import com.javadesign.addressbook.model.Contact;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * vCard 3.0 简化实现（与手机通讯录互操作时的子集）。
 * <p>
 * <b>导出</b>：每个联系人为一段 {@code BEGIN:VCARD}…{@code END:VCARD}，字段含 FN、TEL、EMAIL、BDAY、ORG、ADR、NOTE。<br>
 * <b>导入</b>：按块切分，行解析键值（忽略参数细节中的部分差异）。
 * </p>
 * <p><b>检查点</b>：手机导出格式多样，若 FN 缺失则本实现丢弃该卡片；论文中可附实测样例说明。</p>
 */
public final class VCardContactCodec {

    /** 匹配 {@code KEY:VALUE} 行（属性参数简化处理）。 */
    private static final Pattern LINE = Pattern.compile("^([^:;]+):(.*)$");

    private VCardContactCodec() {
    }

    public static void exportContacts(List<Contact> contacts, Path file) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (Contact c : contacts) {
            sb.append("BEGIN:VCARD\r\n");
            sb.append("VERSION:3.0\r\n");
            sb.append("FN:").append(escapeV(c.getName())).append("\r\n");
            if (!c.getPhone().isEmpty()) {
                sb.append("TEL;TYPE=VOICE:").append(escapeV(c.getPhone())).append("\r\n");
            }
            if (!c.getMobile().isEmpty()) {
                sb.append("TEL;TYPE=CELL:").append(escapeV(c.getMobile())).append("\r\n");
            }
            if (!c.getEmail().isEmpty()) {
                sb.append("EMAIL:").append(escapeV(c.getEmail())).append("\r\n");
            }
            if (c.getBirthday() != null) {
                sb.append("BDAY:").append(c.getBirthday().toString()).append("\r\n");
            }
            if (!c.getWorkUnit().isEmpty()) {
                sb.append("ORG:").append(escapeV(c.getWorkUnit())).append("\r\n");
            }
            if (!c.getHomeAddress().isEmpty()) {
                sb.append("ADR;TYPE=HOME:;;").append(escapeV(c.getHomeAddress())).append(";;;;\r\n");
            }
            if (!c.getRemark().isEmpty()) {
                sb.append("NOTE:").append(escapeV(c.getRemark())).append("\r\n");
            }
            sb.append("END:VCARD\r\n");
        }
        Files.writeString(file, sb.toString(), StandardCharsets.UTF_8);
    }

    private static String escapeV(String s) {
        return s == null ? "" : s.replace("\n", "\\n").replace("\r", "");
    }

    public static List<Contact> importContacts(Path file) throws IOException {
        String text = Files.readString(file, StandardCharsets.UTF_8);
        text = text.replace("\r\n", "\n").replace("\r", "\n");
        List<String> blocks = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inside = false;
        for (String line : text.split("\n")) {
            if (line.regionMatches(true, 0, "BEGIN:VCARD", 0, 11)) {
                inside = true;
                cur = new StringBuilder(line).append('\n');
                continue;
            }
            if (inside) {
                cur.append(line).append('\n');
                if (line.regionMatches(true, 0, "END:VCARD", 0, 8)) {
                    blocks.add(cur.toString());
                    inside = false;
                }
            }
        }
        List<Contact> out = new ArrayList<>();
        for (String block : blocks) {
            Contact c = parseBlock(block);
            if (c != null) {
                out.add(c);
            }
        }
        return out;
    }

    private static Contact parseBlock(String block) {
        Contact c = new Contact();
        try (BufferedReader br = new BufferedReader(new StringReader(block))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isEmpty()) {
                    continue;
                }
                Matcher m = LINE.matcher(line);
                if (!m.matches()) {
                    continue;
                }
                String keyPart = m.group(1).toUpperCase(Locale.ROOT);
                String val = m.group(2);
                if (keyPart.startsWith("FN")) {
                    c.setName(val);
                } else if (keyPart.startsWith("TEL")) {
                    if (keyPart.contains("CELL") || keyPart.contains("MOBILE")) {
                        if (c.getMobile().isEmpty()) {
                            c.setMobile(val);
                        }
                    } else {
                        if (c.getPhone().isEmpty()) {
                            c.setPhone(val);
                        }
                    }
                } else if (keyPart.startsWith("EMAIL")) {
                    c.setEmail(val);
                } else if (keyPart.startsWith("BDAY")) {
                    try {
                        c.setBirthday(LocalDate.parse(val.trim()));
                    } catch (DateTimeParseException e) {
                        try {
                            c.setBirthday(LocalDate.parse(val.trim(), DateTimeFormatter.BASIC_ISO_DATE));
                        } catch (DateTimeParseException ignored) {
                            // skip
                        }
                    }
                } else if (keyPart.startsWith("ORG")) {
                    c.setWorkUnit(val);
                } else if (keyPart.startsWith("NOTE")) {
                    c.setRemark(val);
                } else if (keyPart.startsWith("ADR")) {
                    String[] parts = val.split(";");
                    if (parts.length >= 3) {
                        c.setHomeAddress(parts[2]);
                    }
                }
            }
        } catch (IOException e) {
            return null;
        }
        if (c.getName() == null || c.getName().isEmpty()) {
            return null;
        }
        return c;
    }
}
