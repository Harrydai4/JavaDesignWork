package com.javadesign.addressbook.importexport;

import com.javadesign.addressbook.model.Contact;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * CSV 交换格式（课程要求导入/导出之一）。
 * <p>
 * <b>格式约定</b>：首行为英文表头；字段顺序固定；文本字段用双引号包裹，内部双引号加倍；
 * 分组 id 多值用 {@code |} 连接。编码 UTF-8。
 * </p>
 * <p><b>检查点</b>：导入时列数不足行跳过；生日解析失败则保持空。</p>
 */
public final class CsvContactCodec {

    /** 与 {@link #exportContacts} 写出顺序一致，导入时按列下标解析。 */
    private static final String[] HEADERS = {
            "name", "phone", "mobile", "instantMessaging", "email", "homepage",
            "birthday", "photoPath", "workUnit", "homeAddress", "postalCode", "remark", "groupIds"
    };

    private CsvContactCodec() {
    }

    public static void exportContacts(List<Contact> contacts, Path file) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append(String.join(",", HEADERS)).append('\n');
        for (Contact c : contacts) {
            sb.append(escape(c.getName())).append(',');
            sb.append(escape(c.getPhone())).append(',');
            sb.append(escape(c.getMobile())).append(',');
            sb.append(escape(c.getInstantMessaging())).append(',');
            sb.append(escape(c.getEmail())).append(',');
            sb.append(escape(c.getHomepage())).append(',');
            sb.append(escape(c.getBirthday() != null ? c.getBirthday().toString() : "")).append(',');
            sb.append(escape(c.getPhotoPath())).append(',');
            sb.append(escape(c.getWorkUnit())).append(',');
            sb.append(escape(c.getHomeAddress())).append(',');
            sb.append(escape(c.getPostalCode())).append(',');
            sb.append(escape(c.getRemark())).append(',');
            sb.append(escape(String.join("|", c.getGroupIds())));
            sb.append('\n');
        }
        Files.writeString(file, sb.toString(), StandardCharsets.UTF_8);
    }

    public static List<Contact> importContacts(Path file) throws IOException {
        String text = Files.readString(file, StandardCharsets.UTF_8);
        List<Contact> out = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new StringReader(text))) {
            String header = br.readLine();
            if (header == null) {
                return out;
            }
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                List<String> cols = splitLine(line);
                if (cols.size() < 12) {
                    continue;
                }
                Contact c = new Contact();
                c.setName(unescape(cols.get(0)));
                c.setPhone(unescape(cols.get(1)));
                c.setMobile(unescape(cols.get(2)));
                c.setInstantMessaging(unescape(cols.get(3)));
                c.setEmail(unescape(cols.get(4)));
                c.setHomepage(unescape(cols.get(5)));
                String bd = unescape(cols.get(6));
                if (!bd.isEmpty()) {
                    try {
                        c.setBirthday(LocalDate.parse(bd));
                    } catch (DateTimeParseException ignored) {
                        // leave null
                    }
                }
                c.setPhotoPath(unescape(cols.get(7)));
                c.setWorkUnit(unescape(cols.get(8)));
                c.setHomeAddress(unescape(cols.get(9)));
                c.setPostalCode(unescape(cols.get(10)));
                c.setRemark(unescape(cols.get(11)));
                if (cols.size() > 12) {
                    String g = unescape(cols.get(12));
                    if (!g.isEmpty()) {
                        Set<String> ids = new LinkedHashSet<>();
                        for (String p : g.split("\\|")) {
                            if (!p.isBlank()) {
                                ids.add(p.trim());
                            }
                        }
                        c.setGroupIds(ids);
                    }
                }
                out.add(c);
            }
        }
        return out;
    }

    private static String escape(String s) {
        if (s == null) {
            return "\"\"";
        }
        String t = s.replace("\"", "\"\"");
        return "\"" + t + "\"";
    }

    private static String unescape(String s) {
        if (s == null) {
            return "";
        }
        s = s.trim();
        if (s.length() >= 2 && s.startsWith("\"") && s.endsWith("\"")) {
            s = s.substring(1, s.length() - 1).replace("\"\"", "\"");
        }
        return s;
    }

    static List<String> splitLine(String line) {
        List<String> res = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQ = false;
        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (ch == '"') {
                if (inQ && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    cur.append('"');
                    i++;
                } else {
                    inQ = !inQ;
                }
            } else if (ch == ',' && !inQ) {
                res.add(cur.toString());
                cur.setLength(0);
            } else {
                cur.append(ch);
            }
        }
        res.add(cur.toString());
        return res;
    }
}
