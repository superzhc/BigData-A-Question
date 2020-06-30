```java
package com.epoint.bigdata.datacleansing.trans.template;

import com.epoint.bigdata.datacleansing.trans.errodatarepair.ErrorDataRepairMeta;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.*;
import org.pentaho.di.core.Const;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

/**
 * 2019年09月09日 superz add
 */
public class TemplateDemoDialog extends BaseStepDialog implements StepDialogInterface
{
    private static Class<?> BASE_PKG = BaseStepMeta.class;// for i18n purposes

    private TemplateDemoMeta input;

    public TemplateDemoDialog(Shell parent, Object baseStepMeta, TransMeta transMeta, String stepname) {
        super(parent, (BaseStepMeta) baseStepMeta, transMeta, stepname);
        input = (TemplateDemoMeta) baseStepMeta;
    }

    @Override
    public String open() {
        Shell parent = getParent();
        Display display = parent.getDisplay();

        shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MIN | SWT.MAX);
        props.setLook(shell);
        setShellImage(shell, input);

        ModifyListener lsMod = new ModifyListener()
        {
            @Override
            public void modifyText(ModifyEvent arg0) {
                input.setChanged();
            }
        };
        changed = input.hasChanged();

        // 窗体里的控件使用FormLayout的布局方式
        FormLayout formLayout = new FormLayout();
        formLayout.marginWidth = Const.FORM_MARGIN;
        formLayout.marginHeight = Const.FORM_MARGIN;

        shell.setLayout(formLayout);
        shell.setText("错误数据修复");

        int middle = props.getMiddlePct();
        int margin = Const.MARGIN;

        // 添加一行步骤名称标签
        wlStepname = new Label(shell, SWT.RIGHT);
        wlStepname.setText(BaseMessages.getString(BASE_PKG, "System.Label.StepName"));
        props.setLook(wlStepname);
        fdlStepname = new FormData();
        fdlStepname.left = new FormAttachment(0, 0);
        fdlStepname.right = new FormAttachment(middle, -margin);
        fdlStepname.top = new FormAttachment(0, margin);
        wlStepname.setLayoutData(fdlStepname);
        wStepname = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        wStepname.setText(stepname);
        props.setLook(wStepname);
        wStepname.addModifyListener(lsMod);
        fdStepname = new FormData();
        fdStepname.left = new FormAttachment(middle, 0);
        fdStepname.top = new FormAttachment(0, margin);
        fdStepname.right = new FormAttachment(100, 0);
        wStepname.setLayoutData(fdStepname);

        // 添加控件===========================================================
        // TODO
        // 添加控件===========================================================

        // 创建两个按钮，“确认”和“取消”按钮，以及按钮单击事件的监听方法，把按钮放在对话框的最下面
        wOK = new Button(shell, SWT.PUSH);
        wOK.setText(BaseMessages.getString(BASE_PKG, "System.Button.OK")); //$NON-NLS-1$
        wCancel = new Button(shell, SWT.PUSH);
        wCancel.setText(BaseMessages.getString(BASE_PKG, "System.Button.Cancel")); //$NON-NLS-1$

        setButtonPositions(new Button[] {wOK, wCancel }, margin, null);

        // Add listeners
        lsCancel = new Listener()
        {
            public void handleEvent(Event e) {
                cancel();
            }
        };

        lsOK = new Listener()
        {
            public void handleEvent(Event e) {
                ok();
            }
        };

        wCancel.addListener(SWT.Selection, lsCancel);
        wOK.addListener(SWT.Selection, lsOK);

        // 下面的代码保证了窗口在非正常关闭时（没有使用“确定”或“取消”按钮关闭），取消用户的编辑
        lsDef = new SelectionAdapter()
        {
            public void widgetDefaultSelected(SelectionEvent e) {
                ok();
            }
        };

        wStepname.addSelectionListener(lsDef);

        // Detect X or ALT-F4 or something that kills this window...
        shell.addShellListener(new ShellAdapter()
        {// 保证了窗口在非正常关闭时，取消用户的编辑
            public void shellClosed(ShellEvent e) {
                cancel();
            }
        });

        // 下面的代码把数据从步骤的元数据对象里复制到窗口的控件里
        getData();

        // 窗口的大小和位置将根据窗口的自然属性、上次窗口大小和位置，以及显示屏的大小自动设置
        setSize();
        input.setChanged(changed);
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch())
                display.sleep();
        }
        return stepname;
    }

    private void cancel() {
        stepname = null;
        input.setChanged(changed);
        dispose();
    }

    // 单击OK把控件里用户输入的数据都写入到步骤的元数据对象中。
    private void ok() {
        if (Const.isEmpty(wStepname.getText()))
            return;

        stepname = wStepname.getText(); // return value
        getInfo(input);

        dispose();
    }

    private void getData() {

    }

    private void getInfo(TemplateDemoMeta meta) {

    }
}
```

