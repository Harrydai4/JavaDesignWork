package com.javadesign.addressbook;

import com.javadesign.addressbook.service.AddressBookService;
import com.javadesign.addressbook.storage.JsonAddressBookStorage;
import com.javadesign.addressbook.ui.MainFrame;

import javax.swing.JOptionPane;
import javax.swing.UIManager;
import java.io.IOException;

/**
 * 应用程序入口（含 {@code main}）。
 * <p>
 * <b>职责</b>：设置 Swing 系统外观；构造 {@link JsonAddressBookStorage} 与 {@link AddressBookService}；
 * 调用 {@link com.javadesign.addressbook.storage.JsonAddressBookStorage#load()}；
 * 在 EDT 上打开 {@link com.javadesign.addressbook.ui.MainFrame}。
 * </p>
 * <p>
 * <b>检查点</b>：加载失败时仅弹窗提示并退出，不启动空主窗，避免用户误以为数据已清空。
 * 正常路径下数据目录为 {@link JsonAddressBookStorage#defaultBaseDir()}。
 * </p>
 */
public final class AddressBookApp {

    /**
     * 程序入口。
     *
     * @param args 未使用（无可选命令行参数）
     */
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // 检查点：外观设置失败时使用 JVM 默认 LaF，不阻断启动
        }
        JsonAddressBookStorage storage = new JsonAddressBookStorage(JsonAddressBookStorage.defaultBaseDir());
        AddressBookService app = new AddressBookService(storage);
        try {
            app.load();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null,
                    "加载数据失败，请检查数据目录权限或文件是否损坏。\n详情：" + e.getMessage(),
                    "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        MainFrame.show(app);
    }
}
