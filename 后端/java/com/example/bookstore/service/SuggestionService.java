package com.example.bookstore.service;

import com.example.bookstore.dao.NoticeDAO;
import com.example.bookstore.dao.SuggestionDAO;
import com.example.bookstore.entity.Notice;
import com.example.bookstore.entity.Suggestion;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SuggestionService {

    private final SuggestionDAO suggestionDAO = new SuggestionDAO();
    private final NoticeDAO noticeDAO = new NoticeDAO();

    private Map<String, Object> fail(Map<String, Object> map, String msg) {
        map.put("success", false);
        map.put("message", msg);
        return map;
    }

    public Map<String, Object> submit(Suggestion s) {
        Map<String, Object> result = new HashMap<>();
        if (s.title == null || s.title.trim().isEmpty()) return fail(result, "书名不能为空");
        if (s.type == null || (s.type != 1 && s.type != 2)) s.type = 1;
        suggestionDAO.insert(s);
        result.put("success", true);
        result.put("message", "提交成功，等待审核");
        return result;
    }

    public Map<String, Object> handle(Integer id, int status, String remark, Integer handlerId) {
        Map<String, Object> result = new HashMap<>();
        Suggestion s = suggestionDAO.findById(id);
        if (s == null) return fail(result, "荐购记录不存在");
        if (status < 1 || status > 4) return fail(result, "无效的处理结果");
        // 状态机：0待处理→1已采纳/2已驳回；1已采纳→3采购中；3采购中→4已到馆
        boolean allowed = (s.status == 0 && (status == 1 || status == 2))
                       || (s.status == 1 && status == 3)
                       || (s.status == 3 && status == 4);
        if (!allowed) return fail(result, "当前状态不允许该操作");
        suggestionDAO.handle(id, status, handlerId, remark);

        String title;
        switch (status) {
            case 1: title = "荐购已采纳"; break;
            case 2: title = "荐购已驳回"; break;
            case 3: title = "荐购已采购"; break;
            default: title = "荐购已到馆"; break;
        }
        Notice n = new Notice();
        n.userId = s.userId;
        n.type = 5;
        n.title = title;
        n.content = "您荐购的《" + s.title + "》" + title.substring(3) +
                    (remark != null && !remark.isEmpty() ? "：" + remark : "");
        n.refType = "SUGGESTION";
        n.refId = id;
        noticeDAO.insert(n);

        result.put("success", true);
        result.put("message", "处理完成");
        return result;
    }

    public Map<String, Object> vote(Integer id, Integer userId) {
        Map<String, Object> result = new HashMap<>();
        if (suggestionDAO.findById(id) == null) return fail(result, "荐购记录不存在");
        if (suggestionDAO.hasLiked(id, userId)) return fail(result, "您已点赞过");
        suggestionDAO.insertLike(id, userId);
        suggestionDAO.vote(id);
        result.put("success", true);
        result.put("message", "已投票");
        return result;
    }

    public List<Suggestion> findAll() { return suggestionDAO.findAll(); }
    public List<Suggestion> findByUser(Integer userId) { return suggestionDAO.findByUser(userId); }
    public Suggestion findById(Integer id) { return suggestionDAO.findById(id); }
}
