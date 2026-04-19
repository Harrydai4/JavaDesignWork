package com.javadesign.addressbook.ui;

import com.javadesign.addressbook.importexport.CsvContactCodec;
import com.javadesign.addressbook.importexport.VCardContactCodec;
import com.javadesign.addressbook.model.Contact;
import com.javadesign.addressbook.model.Group;
import com.javadesign.addressbook.service.AddressBookService;
import com.javadesign.addressbook.service.ContactSearch;

import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Swing 主窗口：课程要求「左侧分组 + 右侧列表」的核心界面。
 * <p>
 * <b>布局</b>：北 — 搜索框；中 — {@link JSplitPane} 左 {@link JTree} 右 {@link JTable}；菜单 — 导入导出、列显示。
 * 工具栏提供常用操作快捷键式入口。
 * </p>
 * <p>
 * <b>检查点</b>：关闭窗口前 {@link AddressBookService#save()}；导入后 {@link #rebuildTree()} 与 {@link #refreshTable()}；
 * 删除分组前确认文案强调「不删联系人」；所有 JOptionPane 使用中文。
 * </p>
 */
public final class MainFrame extends JFrame {

    /** 树节点中表示「全部联系人」的伪 id（非数据库中的分组） */
    public static final String TREE_ALL = "__ALL__";

    private final AddressBookService app;
    private final JTree tree;
    private final DefaultTreeModel treeModel;
    private final DefaultMutableTreeNode treeRoot;
    private final JTable table;
    private final DefaultTableModel tableModel;
    private final JTextField searchField = new JTextField(20);

    private final String[] colKeys = {
            "name", "mobile", "phone", "email", "workUnit", "homeAddress", "im", "homepage", "birthday", "postalCode", "remark", "photoPath", "groups"
    };
    private final String[] colTitles = {
            "姓名", "手机", "电话", "邮箱", "工作单位", "家庭地址", "即时通信", "主页", "生日", "邮编", "备注", "像片", "分组"
    };
    private final Map<String, JCheckBoxMenuItem> colMenus = new LinkedHashMap<>();

    /** 与表格行一一对应，用于根据选中行取 {@link Contact}（考虑排序器映射） */
    private List<Contact> viewRows = new ArrayList<>();

    /**
     * 构建主窗体并展示初始数据。
     *
     * @param app 已 load 后的服务实例
     */
    public MainFrame(AddressBookService app) {
        super("通讯录管理系统");
        this.app = app;
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                try {
                    app.save();
                } catch (IOException ignored) {
                    // ignore
                }
                dispose();
                System.exit(0);
            }
        });

        treeRoot = new DefaultMutableTreeNode("通讯录");
        treeModel = new DefaultTreeModel(treeRoot);
        tree = new JTree(treeModel);
        tree.setRootVisible(true);
        tree.addTreeSelectionListener(e -> refreshTable());

        tableModel = new DefaultTableModel(colTitles, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setAutoCreateRowSorter(true);
        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        table.setFillsViewportHeight(true);

        JPanel north = new JPanel(new FlowLayout(FlowLayout.LEFT));
        north.add(new JLabel("搜索:"));
        north.add(searchField);
        JButton searchBtn = new JButton("应用");
        searchBtn.addActionListener(e -> refreshTable());
        north.add(searchBtn);

        JToolBar bar = new JToolBar();
        bar.add(makeBtn("新建联系人", e -> addContact()));
        bar.add(makeBtn("编辑", e -> editContact()));
        bar.add(makeBtn("删除", e -> deleteContacts()));
        bar.addSeparator();
        bar.add(makeBtn("新建分组", e -> addGroup()));
        bar.add(makeBtn("删除当前分组", e -> deleteGroup()));
        bar.addSeparator();
        bar.add(makeBtn("加入分组…", e -> addToGroups()));
        bar.add(makeBtn("从当前组移除", e -> removeFromCurrentGroup()));

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                new JScrollPane(tree), new JScrollPane(table));
        split.setResizeWeight(0.22);

        JPanel main = new JPanel(new BorderLayout());
        main.add(bar, BorderLayout.NORTH);
        main.add(split, BorderLayout.CENTER);

        setLayout(new BorderLayout());
        add(north, BorderLayout.NORTH);
        add(main, BorderLayout.CENTER);
        setJMenuBar(buildMenuBar());

        rebuildTree();
        refreshTable();
        setSize(1100, 640);
        setLocationRelativeTo(null);
    }

    private JButton makeBtn(String t, java.awt.event.ActionListener a) {
        JButton b = new JButton(t);
        b.addActionListener(a);
        return b;
    }

    private JMenuBar buildMenuBar() {
        JMenuBar mb = new JMenuBar();
        JMenu file = new JMenu("文件");
        file.add(item("导入 CSV…", e -> importCsv()));
        file.add(item("导入 vCard…", e -> importVcf()));
        file.addSeparator();
        file.add(item("导出 CSV…", e -> exportCsv()));
        file.add(item("导出 vCard…", e -> exportVcf()));
        file.addSeparator();
        file.add(item("退出", e -> exitSave()));
        mb.add(file);

        JMenu view = new JMenu("视图");
        for (int i = 0; i < colTitles.length; i++) {
            String key = colKeys[i];
            JCheckBoxMenuItem mi = new JCheckBoxMenuItem(colTitles[i], true);
            int colIndex = i;
            mi.addActionListener(e -> applyColumnVisibility(colIndex, mi.isSelected()));
            colMenus.put(key, mi);
            view.add(mi);
        }
        mb.add(view);
        return mb;
    }

    private JMenuItem item(String t, java.awt.event.ActionListener a) {
        JMenuItem i = new JMenuItem(t);
        i.addActionListener(a);
        return i;
    }

    private void applyColumnVisibility(int columnIndex, boolean visible) {
        if (visible) {
            table.getColumnModel().getColumn(columnIndex).setMinWidth(40);
            table.getColumnModel().getColumn(columnIndex).setMaxWidth(Integer.MAX_VALUE);
            table.getColumnModel().getColumn(columnIndex).setWidth(100);
        } else {
            table.getColumnModel().getColumn(columnIndex).setMinWidth(0);
            table.getColumnModel().getColumn(columnIndex).setMaxWidth(0);
            table.getColumnModel().getColumn(columnIndex).setWidth(0);
        }
    }

    /** 根据服务层分组列表重建树节点（保留「全部联系人」节点）。检查点：保存后调用以同步删除的分组。 */
    private void rebuildTree() {
        treeRoot.removeAllChildren();
        DefaultMutableTreeNode all = new DefaultMutableTreeNode(new TreeNodeData(TREE_ALL, "全部联系人"));
        treeRoot.add(all);
        for (Group g : app.listGroups()) {
            treeRoot.add(new DefaultMutableTreeNode(new TreeNodeData(g.getId(), g.getName())));
        }
        treeModel.reload();
        expandTree();
    }

    private void expandTree() {
        for (int i = 0; i < tree.getRowCount(); i++) {
            tree.expandRow(i);
        }
    }

    /**
     * @return 当前树选中对应的分组 id，或 {@link #TREE_ALL}；根节点等非业务对象回退为全部
     */
    private String currentGroupFilter() {
        TreePath p = tree.getSelectionPath();
        if (p == null) {
            return TREE_ALL;
        }
        Object last = ((DefaultMutableTreeNode) p.getLastPathComponent()).getUserObject();
        if (last instanceof TreeNodeData td) {
            return td.groupId();
        }
        return TREE_ALL;
    }

    /**
     * 按当前分组 + 搜索框重算 {@link #viewRows} 并填充表格。
     * 检查点：搜索与树联动，空搜索表示仅分组过滤。
     */
    private void refreshTable() {
        String gid = currentGroupFilter();
        List<Contact> base;
        if (TREE_ALL.equals(gid)) {
            base = app.listAllContacts();
        } else {
            base = app.listContactsInGroup(gid);
        }
        String q = searchField.getText();
        viewRows = ContactSearch.filter(base, q);
        tableModel.setRowCount(0);
        for (Contact c : viewRows) {
            tableModel.addRow(rowVector(c));
        }
    }

    /** 将联系人转为表格一行（列顺序与 {@link #colTitles} 一致）。 */
    private Object[] rowVector(Contact c) {
        StringBuilder gs = new StringBuilder();
        for (String id : c.getGroupIds()) {
            if (gs.length() > 0) {
                gs.append(",");
            }
            gs.append(app.groupName(id));
        }
        return new Object[]{
                c.getName(),
                c.getMobile(),
                c.getPhone(),
                c.getEmail(),
                c.getWorkUnit(),
                c.getHomeAddress(),
                c.getInstantMessaging(),
                c.getHomepage(),
                c.getBirthday() != null ? c.getBirthday().toString() : "",
                c.getPostalCode(),
                c.getRemark(),
                c.getPhotoPath(),
                gs.toString()
        };
    }

    private void addContact() {
        ContactEditDialog d = new ContactEditDialog(this, null, app.listGroups(), app.getStorage());
        d.setVisible(true);
        if (d.isSaved()) {
            app.addContact(d.getContact());
            save();
            rebuildTree();
            refreshTable();
        }
    }

    private void editContact() {
        List<Contact> sel = selectedContacts();
        if (sel.size() != 1) {
            JOptionPane.showMessageDialog(this, "请选中一行进行编辑", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Contact cur = sel.get(0);
        app.findContact(cur.getId()).ifPresent(real -> {
            ContactEditDialog d = new ContactEditDialog(this, real, app.listGroups(), app.getStorage());
            d.setVisible(true);
            if (d.isSaved()) {
                app.updateContact(d.getContact());
                save();
                rebuildTree();
                refreshTable();
            }
        });
    }

    private void deleteContacts() {
        List<Contact> sel = selectedContacts();
        if (sel.isEmpty()) {
            return;
        }
        int ok = JOptionPane.showConfirmDialog(this,
                "确定删除选中的 " + sel.size() + " 位联系人？", "确认", JOptionPane.OK_CANCEL_OPTION);
        if (ok != JOptionPane.OK_OPTION) {
            return;
        }
        for (Contact c : sel) {
            app.removeContact(c.getId());
        }
        save();
        refreshTable();
    }

    private void addGroup() {
        String name = JOptionPane.showInputDialog(this, "新分组名称:", "新建分组", JOptionPane.PLAIN_MESSAGE);
        if (name == null || name.isBlank()) {
            return;
        }
        Group g = new Group();
        g.setName(name.trim());
        app.addGroup(g);
        save();
        rebuildTree();
        refreshTable();
    }

    private void deleteGroup() {
        String gid = currentGroupFilter();
        if (TREE_ALL.equals(gid)) {
            JOptionPane.showMessageDialog(this, "请在左侧选择具体分组（非「全部联系人」）", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int ok = JOptionPane.showConfirmDialog(this,
                "删除分组不会删除联系人，仅从分组中移除。确定？", "确认", JOptionPane.OK_CANCEL_OPTION);
        if (ok != JOptionPane.OK_OPTION) {
            return;
        }
        app.removeGroup(gid);
        save();
        rebuildTree();
        tree.setSelectionRow(0);
        refreshTable();
    }

    private void addToGroups() {
        List<Contact> sel = selectedContacts();
        if (sel.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请先选中联系人", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        List<Group> groups = app.listGroups();
        if (groups.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请先新建分组", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        JComboBox<Group> box = new JComboBox<>(groups.toArray(Group[]::new));
        int r = JOptionPane.showConfirmDialog(this, box, "加入分组", JOptionPane.OK_CANCEL_OPTION);
        if (r != JOptionPane.OK_OPTION) {
            return;
        }
        Group g = (Group) box.getSelectedItem();
        if (g == null) {
            return;
        }
        List<String> ids = new ArrayList<>();
        ids.add(g.getId());
        app.addContactsToGroups(sel, ids);
        save();
        refreshTable();
    }

    private void removeFromCurrentGroup() {
        String gid = currentGroupFilter();
        if (TREE_ALL.equals(gid)) {
            JOptionPane.showMessageDialog(this, "请先选择左侧具体分组", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        List<Contact> sel = selectedContacts();
        if (sel.isEmpty()) {
            return;
        }
        app.removeContactsFromGroup(sel, gid);
        save();
        refreshTable();
    }

    private List<Contact> selectedContacts() {
        int[] rows = table.getSelectedRows();
        List<Contact> list = new ArrayList<>();
        for (int vr : rows) {
            int m = table.convertRowIndexToModel(vr);
            if (m >= 0 && m < viewRows.size()) {
                list.add(viewRows.get(m));
            }
        }
        return list;
    }

    private void importCsv() {
        Path p = chooseFile(true, "csv");
        if (p == null) {
            return;
        }
        try {
            List<Contact> imp = CsvContactCodec.importContacts(p);
            app.mergeImportedContacts(imp);
            save();
            rebuildTree();
            refreshTable();
            JOptionPane.showMessageDialog(this, "已导入 " + imp.size() + " 条（已去重追加）", "完成", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "导入失败", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void importVcf() {
        Path p = chooseFile(true, "vcf");
        if (p == null) {
            return;
        }
        try {
            List<Contact> imp = VCardContactCodec.importContacts(p);
            app.mergeImportedContacts(imp);
            save();
            rebuildTree();
            refreshTable();
            JOptionPane.showMessageDialog(this, "已导入 " + imp.size() + " 条", "完成", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "导入失败", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exportCsv() {
        Path p = chooseFile(false, "csv");
        if (p == null) {
            return;
        }
        try {
            List<Contact> data = app.exportContactsSelection(true, List.of());
            CsvContactCodec.exportContacts(data, p);
            JOptionPane.showMessageDialog(this, "已导出到:\n" + p, "完成", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "导出失败", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exportVcf() {
        Path p = chooseFile(false, "vcf");
        if (p == null) {
            return;
        }
        try {
            List<Contact> data = app.exportContactsSelection(true, List.of());
            VCardContactCodec.exportContacts(data, p);
            JOptionPane.showMessageDialog(this, "已导出到:\n" + p, "完成", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "导出失败", JOptionPane.ERROR_MESSAGE);
        }
    }

    private Path chooseFile(boolean open, String ext) {
        JFileChooser ch = new JFileChooser();
        int r = open ? ch.showOpenDialog(this) : ch.showSaveDialog(this);
        if (r != JFileChooser.APPROVE_OPTION) {
            return null;
        }
        return ch.getSelectedFile().toPath();
    }

    /** 持久化；失败弹中文错误。检查点：所有 mutating 操作后应调用。 */
    private void save() {
        try {
            app.save();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "保存失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exitSave() {
        try {
            app.save();
        } catch (IOException ignored) {
            // ignore
        }
        dispose();
        System.exit(0);
    }

    /** 树节点用户对象：绑定真实分组 id 与显示名 */
    private record TreeNodeData(String groupId, String label) {
        @Override
        public String toString() {
            return label;
        }
    }

    /**
     * 在 EDT 创建并显示主窗口（供入口类调用）。
     *
     * @param app 已加载数据的服务
     */
    public static void show(AddressBookService app) {
        SwingUtilities.invokeLater(() -> {
            MainFrame f = new MainFrame(app);
            f.setVisible(true);
        });
    }
}
