package com.javadesign.addressbook.service;

import com.javadesign.addressbook.model.Contact;
import com.javadesign.addressbook.util.PinyinUtil;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * 搜索与排序服务（无状态工具类）。
 * <p>
 * <b>职责</b>：在给定联系人集合上应用关键字过滤，规则包括——姓名包含、拼音全拼/首字母子串、
 * 电话/手机数字子串（与题目模糊查询一致）；结果按姓名字典序排序。
 * </p>
 * <p><b>检查点</b>：空查询仍执行排序，保证与「全部」视图顺序一致；{@link #matches} 供单元测试覆盖。</p>
 */
public final class ContactSearch {

    private ContactSearch() {
    }

    /**
     * @param scope   当前左侧树决定的「全部」或某分组下的联系人集合
     * @param rawQuery 搜索框原文，trim 后转小写参与匹配
     * @return 新列表，已按姓名排序
     */
    public static List<Contact> filter(List<Contact> scope, String rawQuery) {
        List<Contact> out;
        if (rawQuery == null || rawQuery.isBlank()) {
            out = new ArrayList<>(scope);
        } else {
            String q = rawQuery.trim().toLowerCase(Locale.ROOT);
            out = new ArrayList<>();
            for (Contact c : scope) {
                if (matches(c, q)) {
                    out.add(c);
                }
            }
        }
        out.sort(Comparator.comparing(Contact::getName, Comparator.nullsFirst(String::compareTo)));
        return out;
    }

    /**
     * 单条是否命中（已小写的查询串）。
     *
     * @param c      联系人
     * @param qLower 查询串，已 {@link String#toLowerCase(Locale)} 处理
     */
    static boolean matches(Contact c, String qLower) {
        String name = c.getName() != null ? c.getName() : "";
        if (name.toLowerCase(Locale.ROOT).contains(qLower)) {
            return true;
        }
        String full = PinyinUtil.toPinyinFull(name);
        if (full.contains(qLower)) {
            return true;
        }
        String ini = PinyinUtil.toPinyinInitials(name);
        if (ini.contains(qLower)) {
            return true;
        }
        String phone = PinyinUtil.digitsOnly(c.getPhone());
        String mobile = PinyinUtil.digitsOnly(c.getMobile());
        if (!qLower.isEmpty() && Character.isDigit(qLower.charAt(0))) {
            String qd = PinyinUtil.digitsOnly(qLower);
            if (!qd.isEmpty() && (phone.contains(qd) || mobile.contains(qd))) {
                return true;
            }
        }
        return phone.contains(qLower) || mobile.contains(qLower);
    }

    /** 按「姓」首字符分组键：首字为中文则用其拼音首字母，否则用首字符大写 */
    public static String surnameBucketKey(Contact c) {
        String name = c.getName();
        if (name == null || name.isEmpty()) {
            return "#";
        }
        char first = name.charAt(0);
        if (Character.toString(first).matches("[\\u4E00-\\u9FA5]+")) {
            String ini = PinyinUtil.toPinyinInitials(String.valueOf(first));
            if (!ini.isEmpty()) {
                return ini.substring(0, 1).toUpperCase(Locale.ROOT);
            }
        }
        if (Character.isLetter(first)) {
            return String.valueOf(Character.toUpperCase(first));
        }
        return "#";
    }

    public static List<Contact> sortAndGroupBySurnamePinyin(List<Contact> list) {
        return list.stream()
                .sorted(Comparator.comparing(Contact::getName, Comparator.nullsFirst(String::compareTo)))
                .collect(Collectors.toList());
    }
}
