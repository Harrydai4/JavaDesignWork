package com.javadesign.addressbook.util;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

/**
 * 拼音工具：基于 pinyin4j，将中文姓名转为小写全拼与首字母串。
 * <p><b>用途</b>：{@link com.javadesign.addressbook.service.ContactSearch} 中的拼音模糊匹配。</p>
 * <p><b>检查点</b>：多音字取 pinyin4j 默认读音；非汉字字母数字按规则拼接，异常时降级为原字符或忽略。</p>
 */
public final class PinyinUtil {

    private static final HanyuPinyinOutputFormat FORMAT = new HanyuPinyinOutputFormat();

    static {
        FORMAT.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        FORMAT.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
    }

    private PinyinUtil() {
    }

    /**
     * @param name 姓名或任意字符串
     * @return 连续小写拼音字母（无分隔），非中文按规则附加
     */
    public static String toPinyinFull(String name) {
        if (name == null || name.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (char c : name.toCharArray()) {
            if (Character.toString(c).matches("[\\u4E00-\\u9FA5]+")) {
                try {
                    String[] py = PinyinHelper.toHanyuPinyinStringArray(c, FORMAT);
                    if (py != null && py.length > 0) {
                        sb.append(py[0]);
                    }
                } catch (BadHanyuPinyinOutputFormatCombination ignored) {
                    sb.append(c);
                }
            } else if (Character.isLetterOrDigit(c)) {
                sb.append(Character.toLowerCase(c));
            }
        }
        return sb.toString();
    }

    /** 每个汉字取首字母，其它字符忽略或小写追加 */
    public static String toPinyinInitials(String name) {
        if (name == null || name.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (char c : name.toCharArray()) {
            if (Character.toString(c).matches("[\\u4E00-\\u9FA5]+")) {
                try {
                    String[] py = PinyinHelper.toHanyuPinyinStringArray(c, FORMAT);
                    if (py != null && py.length > 0 && !py[0].isEmpty()) {
                        sb.append(py[0].charAt(0));
                    }
                } catch (BadHanyuPinyinOutputFormatCombination ignored) {
                    // skip
                }
            } else if (Character.isLetter(c)) {
                sb.append(Character.toLowerCase(c));
            }
        }
        return sb.toString();
    }

    /**
     * @param s 原始电话字符串
     * @return 仅保留数字，用于「2645」类连续数字匹配
     */
    public static String digitsOnly(String s) {
        if (s == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (Character.isDigit(c)) {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
