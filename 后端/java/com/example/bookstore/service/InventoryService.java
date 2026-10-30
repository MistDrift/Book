package com.example.bookstore.service;

import com.example.bookstore.dao.InventoryDAO;
import com.example.bookstore.entity.InvItem;
import com.example.bookstore.entity.Inventory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InventoryService {

    private final InventoryDAO inventoryDAO = new InventoryDAO();

    private Map<String, Object> fail(Map<String, Object> map, String msg) {
        map.put("success", false);
        map.put("message", msg);
        return map;
    }

    public Map<String, Object> create(String name, int type, String area, Integer creator) {
        Map<String, Object> result = new HashMap<>();
        if (name == null || name.trim().isEmpty()) return fail(result, "盘点名称不能为空");
        if (type < 1 || type > 3) return fail(result, "无效的盘点类型");
        if (type == 2 && (area == null || area.trim().isEmpty())) return fail(result, "区域盘点必须指定区域");

        Inventory inv = new Inventory();
        inv.name = name;
        inv.type = type;
        inv.area = area;
        inv.creator = creator;
        inventoryDAO.insert(inv);
        inventoryDAO.generateItems(inv.id, type == 2 ? area : null);
        inventoryDAO.refreshTotal(inv.id);

        result.put("success", true);
        result.put("message", "盘点任务已创建");
        result.put("id", inv.id);
        return result;
    }

    public boolean start(Integer id) {
        Inventory inv = inventoryDAO.findById(id);
        if (inv == null || inv.status != 0) return false;
        return inventoryDAO.start(id) > 0;
    }

    public boolean check(Integer invId, Integer bookId, int realStatus, String remark, Integer checkerId) {
        Inventory inv = inventoryDAO.findById(invId);
        if (inv == null || inv.status != 1) return false;
        if (realStatus < 1 || realStatus > 4) return false;
        return inventoryDAO.updateItem(invId, bookId, realStatus, remark, checkerId) > 0;
    }

    public Map<String, Object> finish(Integer id) {
        Map<String, Object> result = new HashMap<>();
        Inventory inv = inventoryDAO.findById(id);
        if (inv == null) return fail(result, "盘点任务不存在");
        if (inv.status != 1) return fail(result, "盘点任务不在进行中");
        inventoryDAO.finish(id);
        inventoryDAO.applyResults(id);
        result.put("success", true);
        result.put("message", "盘点已完成，结果已回写馆藏状态");
        return result;
    }

    public boolean cancel(Integer id) {
        Inventory inv = inventoryDAO.findById(id);
        if (inv == null || (inv.status != 0 && inv.status != 1)) return false;
        return inventoryDAO.cancel(id) > 0;
    }

    public List<Inventory> findAll() { return inventoryDAO.findAll(); }
    public Inventory findById(Integer id) { return inventoryDAO.findById(id); }
    public List<InvItem> items(Integer invId) { return inventoryDAO.findItems(invId); }
}
