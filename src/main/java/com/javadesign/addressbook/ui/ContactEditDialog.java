package com.javadesign.addressbook.ui;

import com.javadesign.addressbook.model.Contact;
import com.javadesign.addressbook.model.Group;
import com.javadesign.addressbook.storage.JsonAddressBookStorage;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 模态对话框：新建或编辑单个 {@link com.javadesign.addressbook.model.Contact}。
 * <p>
 * <b>职责</b>：收集课程要求全部字段；分组以多选框勾选；照片通过文件选择器复制到数据目录下 {@code photos/}，
 * 文本框保存绝对路径供列表与再次编辑使用。
 * </p>
 * <p><b>检查点</b>：姓名为空不允许保存；生日格式错误中文提示；确定后 {@link #isSaved()} 为 true 时父窗口再写盘。</p>
 */
public final class ContactEditDialog extends JDialog {

    /** 正在编辑的实体副本，确定时写回服务层 */
    private final Contact working;
    private final List<Group> allGroups;
    private final List<JCheckBox> groupBoxes = new ArrayList<>();

    private final JTextField name = new JTextField(24);
    private final JTextField phone = new JTextField(24);
    private final JTextField mobile = new JTextField(24);
    private final JTextField im = new JTextField(24);
    private final JTextField email = new JTextField(24);
    private final JTextField homepage = new JTextField(24);
    private final JTextField birthday = new JTextField(24);
    private final JTextField photoPath = new JTextField(24);
    private final JTextField workUnit = new JTextField(24);
    private final JTextField homeAddress = new JTextField(24);
    private final JTextField postalCode = new JTextField(24);
    private final JTextArea remark = new JTextArea(3, 24);

    private final JsonAddressBookStorage storage;
    /** 用户是否点击确定且通过校验 */
    private boolean saved;

    /**
     * @param owner   父窗口，用于模态与居中
     * @param contact null 表示新建，否则为浅拷贝编辑
     * @param groups  当前全部分组，用于生成勾选框
     * @param storage 用于解析照片目标目录（与 JSON 同根）
     */
    public ContactEditDialog(JFrame owner, Contact contact, List<Group> groups, JsonAddressBookStorage storage) {
        super(owner, contact == null ? "新建联系人" : "编辑联系人", true);
        this.storage = storage;
        this.allGroups = groups;
        this.working = contact != null ? shallowCopy(contact) : new Contact();
        build();
        pack();
        setLocationRelativeTo(owner);
    }

    /** 深拷贝字段到新建 Contact，保留原 id。 */
    private static Contact shallowCopy(Contact c) {
        Contact n = new Contact();
        n.setId(c.getId());
        n.setName(c.getName());
        n.setPhone(c.getPhone());
        n.setMobile(c.getMobile());
        n.setInstantMessaging(c.getInstantMessaging());
        n.setEmail(c.getEmail());
        n.setHomepage(c.getHomepage());
        n.setBirthday(c.getBirthday());
        n.setPhotoPath(c.getPhotoPath());
        n.setWorkUnit(c.getWorkUnit());
        n.setHomeAddress(c.getHomeAddress());
        n.setPostalCode(c.getPostalCode());
        n.setRemark(c.getRemark());
        n.setGroupIds(new LinkedHashSet<>(c.getGroupIds()));
        return n;
    }

    private void build() {
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        int r = 0;
        addRow(form, gbc, r++, "姓名", name);
        addRow(form, gbc, r++, "电话", phone);
        addRow(form, gbc, r++, "手机", mobile);
        addRow(form, gbc, r++, "即时通信", im);
        addRow(form, gbc, r++, "电子邮箱", email);
        addRow(form, gbc, r++, "个人主页", homepage);
        addRow(form, gbc, r++, "生日(yyyy-MM-dd)", birthday);
        JPanel photoRow = new JPanel(new BorderLayout(4, 0));
        photoRow.add(photoPath, BorderLayout.CENTER);
        JButton browse = new JButton("选择照片…");
        browse.addActionListener(e -> choosePhoto());
        photoRow.add(browse, BorderLayout.EAST);
        gbc.gridx = 0;
        gbc.gridy = r;
        form.add(new JLabel("像片路径"), gbc);
        gbc.gridx = 1;
        form.add(photoRow, gbc);
        r++;
        addRow(form, gbc, r++, "工作单位", workUnit);
        addRow(form, gbc, r++, "家庭地址", homeAddress);
        addRow(form, gbc, r++, "邮编", postalCode);

        gbc.gridx = 0;
        gbc.gridy = r;
        form.add(new JLabel("所属组"), gbc);
        JPanel gp = new JPanel();
        gp.setBorder(BorderFactory.createTitledBorder("勾选分组"));
        for (Group g : allGroups) {
            JCheckBox cb = new JCheckBox(g.getName());
            cb.setSelected(working.getGroupIds().contains(g.getId()));
            cb.putClientProperty("gid", g.getId());
            groupBoxes.add(cb);
            gp.add(cb);
        }
        gbc.gridx = 1;
        form.add(gp, gbc);
        r++;

        gbc.gridx = 0;
        gbc.gridy = r;
        form.add(new JLabel("备注"), gbc);
        gbc.gridx = 1;
        form.add(new JScrollPane(remark), gbc);
        r++;

        name.setText(working.getName());
        phone.setText(working.getPhone());
        mobile.setText(working.getMobile());
        im.setText(working.getInstantMessaging());
        email.setText(working.getEmail());
        homepage.setText(working.getHomepage());
        if (working.getBirthday() != null) {
            birthday.setText(working.getBirthday().toString());
        }
        photoPath.setText(working.getPhotoPath());
        workUnit.setText(working.getWorkUnit());
        homeAddress.setText(working.getHomeAddress());
        postalCode.setText(working.getPostalCode());
        remark.setText(working.getRemark());

        JPanel south = new JPanel();
        JButton ok = new JButton("确定");
        JButton cancel = new JButton("取消");
        ok.addActionListener(e -> onOk());
        cancel.addActionListener(e -> dispose());
        south.add(ok);
        south.add(cancel);

        setLayout(new BorderLayout(8, 8));
        add(form, BorderLayout.CENTER);
        add(south, BorderLayout.SOUTH);
    }

    private void addRow(JPanel form, GridBagConstraints gbc, int row, String label, JTextField field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        form.add(new JLabel(label), gbc);
        gbc.gridx = 1;
        form.add(field, gbc);
    }

    /** 检查点：复制失败时 JOptionPane 中文报错，不修改路径。 */
    private void choosePhoto() {
        JFileChooser ch = new JFileChooser();
        if (ch.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            Path src = ch.getSelectedFile().toPath();
            try {
                Path base = storage.getDataFile().getParent().resolve("photos");
                Files.createDirectories(base);
                String fn = System.currentTimeMillis() + "_" + src.getFileName();
                Path dst = base.resolve(fn);
                Files.copy(src, dst);
                photoPath.setText(dst.toAbsolutePath().toString());
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "复制照片失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /** 校验后写回 {@link #working}，置 {@link #saved}。 */
    private void onOk() {
        if (name.getText().isBlank()) {
            JOptionPane.showMessageDialog(this, "请填写姓名", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        working.setName(name.getText().trim());
        working.setPhone(phone.getText().trim());
        working.setMobile(mobile.getText().trim());
        working.setInstantMessaging(im.getText().trim());
        working.setEmail(email.getText().trim());
        working.setHomepage(homepage.getText().trim());
        String bd = birthday.getText().trim();
        if (!bd.isEmpty()) {
            try {
                working.setBirthday(LocalDate.parse(bd));
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(this, "生日格式应为 yyyy-MM-dd", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }
        } else {
            working.setBirthday(null);
        }
        working.setPhotoPath(photoPath.getText().trim());
        working.setWorkUnit(workUnit.getText().trim());
        working.setHomeAddress(homeAddress.getText().trim());
        working.setPostalCode(postalCode.getText().trim());
        working.setRemark(remark.getText().trim());

        Set<String> gids = new LinkedHashSet<>();
        for (JCheckBox cb : groupBoxes) {
            if (cb.isSelected()) {
                gids.add((String) cb.getClientProperty("gid"));
            }
        }
        working.setGroupIds(gids);
        saved = true;
        dispose();
    }

    public boolean isSaved() {
        return saved;
    }

    public Contact getContact() {
        return working;
    }
}
