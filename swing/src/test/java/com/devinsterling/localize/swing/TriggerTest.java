package com.devinsterling.localize.swing;

import com.devinsterling.localize.event.Subscription;
import com.devinsterling.localize.swing.junit.SwingEdtExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.swing.DefaultListSelectionModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.table.DefaultTableColumnModel;

import java.awt.event.ComponentEvent;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SwingEdtExtension.class)
public class TriggerTest {

    @Test public void testActionTrigger() {
        AtomicInteger count = new AtomicInteger();
        JButton button = new JButton();
        Trigger onAction = Trigger.action(button);
        Subscription subscription = onAction.subscribe(count::incrementAndGet);

        assertEquals(0, count.get());

        button.doClick();
        button.doClick();
        assertEquals(2, count.get());

        subscription.dispose();

        button.doClick();
        assertEquals(2, count.get());
    }

    @Test public void testPropertyChangeTrigger() {
        AtomicInteger count = new AtomicInteger();
        JProgressBar bar = new JProgressBar();
        Trigger onPropertyChange = Trigger.propertyChange(bar);
        Subscription subscription = onPropertyChange.subscribe(count::incrementAndGet);

        bar.setIndeterminate(true);
        assertEquals(1, count.get());

        bar.setString("123");
        bar.setStringPainted(true);
        bar.setBorderPainted(false);
        assertEquals(4, count.get());

        subscription.dispose();

        bar.setIndeterminate(false);
        assertEquals(4, count.get());
    }

    @Test public void testNamedPropertyChangeTrigger() {
        AtomicInteger count = new AtomicInteger();
        JProgressBar bar = new JProgressBar();
        Trigger onPropertyChange = Trigger.propertyChange("string", bar);
        Subscription subscription = onPropertyChange.subscribe(count::incrementAndGet);

        bar.setStringPainted(true);
        assertEquals(0, count.get());

        bar.setString("abc");
        bar.setStringPainted(false);
        bar.setString("xyz");
        assertEquals(2, count.get());

        subscription.dispose();

        bar.setString("1337");
        assertEquals(2, count.get());
    }

    @Test public void testResizeTrigger() {
        AtomicInteger count = new AtomicInteger();
        JPanel panel = new JPanel();
        Trigger onResize = Trigger.resize(panel);
        Subscription subscription = onResize.subscribe(count::incrementAndGet);

        // NOTE: Synthesize component resizing as the test environment is headless
        //       > Calling `panel.setSize(x, y)` will not work here
        Runnable dispatchEvent = () -> panel.dispatchEvent(new ComponentEvent(panel, ComponentEvent.COMPONENT_RESIZED));

        assertEquals(0, count.get());

        dispatchEvent.run();
        dispatchEvent.run();
        assertEquals(2, count.get());

        subscription.dispose();

        dispatchEvent.run();
        assertEquals(2, count.get());
    }

    @Test public void testListSelectionChangeTrigger() {
        AtomicInteger count = new AtomicInteger();

        Integer[] data = { 1, 2, 3 };
        JList<Integer> list = new JList<>(data);

        Trigger onSelection = Trigger.listSelection(list);
        Subscription subscription = onSelection.subscribe(count::incrementAndGet);

        assertEquals(0, count.get());

        list.setSelectedIndex(2);
        list.setSelectedIndex(2);
        assertEquals(1, count.get());

        list.setSelectedIndices(new int[] { 0, 1, 2 });
        assertEquals(5, count.get());

        subscription.dispose();

        list.clearSelection();
        list.setSelectedIndex(0);
        assertEquals(5, count.get());
    }

    @Test public void testTableRowSelectionChangeTrigger() {
        AtomicInteger count = new AtomicInteger();

        JTable table = createTable();
        BiConsumer<Integer, Integer> select = (row, col) -> table.changeSelection(row, col, false, false);

        Trigger onSelection = Trigger.tableRowSelection(table);
        Subscription subscription = onSelection.subscribe(count::incrementAndGet);

        assertEquals(0, count.get());

        select.accept(0, 0);
        select.accept(0, 0);
        assertEquals(1, count.get());

        select.accept(0, 1);
        select.accept(4, 0);
        select.accept(4, 1);
        assertEquals(2, count.get());

        subscription.dispose();

        select.accept(3, 0);
        select.accept(2, 1);
        assertEquals(2, count.get());
    }

    @Test public void testTableColumnSelectionChangeTrigger() {
        AtomicInteger count = new AtomicInteger();

        JTable table = createTable();
        BiConsumer<Integer, Integer> select = (row, col) -> table.changeSelection(row, col, false, false);

        Trigger onSelection = Trigger.tableColumnSelection(table);
        Subscription subscription = onSelection.subscribe(count::incrementAndGet);

        assertEquals(0, count.get());

        select.accept(1, 0);
        select.accept(2, 0);
        assertEquals(1, count.get());

        select.accept(0, 1);
        select.accept(1, 0);
        select.accept(3, 1);
        assertEquals(4, count.get());

        subscription.dispose();

        select.accept(1, 0);
        assertEquals(4, count.get());
    }

    @Test public void testTableRowSelectionModelChangeTrigger() {
        AtomicInteger count = new AtomicInteger();

        JTable table = createTable();
        BiConsumer<Integer, Integer> select = (row, col) -> table.changeSelection(row, col, false, false);

        Trigger onSelection = Trigger.tableRowSelection(table);
        onSelection.subscribe(count::incrementAndGet);

        assertEquals(0, count.get());

        table.setSelectionModel(new DefaultListSelectionModel());
        assertEquals(1, count.get());

        select.accept(1, 0);
        select.accept(1, 1);
        assertEquals(2, count.get());
    }

    @Test public void testTableColumnSelectionModelChangeTrigger() {
        AtomicInteger count = new AtomicInteger();

        JTable table = createTable();
        BiConsumer<Integer, Integer> select = (row, col) -> table.changeSelection(row, col, false, false);

        Trigger onSelection = Trigger.tableColumnSelection(table);
        onSelection.subscribe(count::incrementAndGet);

        assertEquals(0, count.get());

        table.setColumnModel(new DefaultTableColumnModel());
        assertEquals(1, count.get());

        select.accept(0, 1);
        select.accept(1, 1);
        assertEquals(2, count.get());
    }


    private JTable createTable() {
        String[] columnNames = { "#1", "#2" };
        Object[][] data = {{ 1, 6 }, { 2, 7 }, { 3, 8 }, { 4, 9 }, { 5, 10 }};
        return new JTable(data, columnNames);
    }
}
