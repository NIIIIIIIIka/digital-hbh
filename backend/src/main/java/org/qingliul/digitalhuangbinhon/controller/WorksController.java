package org.qingliul.digitalhuangbinhon.controller;

import org.qingliul.digitalhuangbinhon.entity.vo.ResponseVO;
import org.qingliul.digitalhuangbinhon.entity.vo.WorksVO;
import org.qingliul.digitalhuangbinhon.service.WorksService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/works")
public class WorksController {

    @Autowired
    private WorksService worksService;

    /**
     * 作品列表接口
     * GET /api/works/list
     *
     * 参数：
     * - creation_year: 创作年份（可选）
     * - works_name: 作品名称关键词（可选）
     * - page: 页码（可选，默认1）
     * - size: 每页条数（可选，默认10）
     */
//    @GetMapping("/list")
//    public ResponseVO<Map<String, Object>> getWorksList(
//            @RequestParam(required = false) Integer creation_year,
//            @RequestParam(required = false) String works_name,
//            @RequestParam(required = false) String category,
//            @RequestParam(required = false) String tags,
//            @RequestParam(required = false, defaultValue = "1") Integer page,
//            @RequestParam(required = false, defaultValue = "10") Integer size) {
//
//        try {
//            Map<String, Object> result = worksService.getWorksList(creation_year, works_name, category, tags, page, size);
//            return ResponseVO.success(result);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return ResponseVO.error(500, "服务器内部错误：" + e.getMessage());
//        }
//    }
    @GetMapping("/list")
    public ResponseVO<Map<String, Object>> getWorksList(
            @RequestParam(required = false) Integer creation_year,
            @RequestParam(required = false) String works_name,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String tags,
            @RequestParam(required = false) String art_period, // ✅ 新增
            @RequestParam(required = false) Integer startYear,
            @RequestParam(required = false) Integer endYear,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "12") Integer size) {

        Map<String, Object> result = worksService.getWorksList(
                creation_year, works_name, category, tags,art_period, startYear, endYear, page, size);
        return ResponseVO.success(result);
    }
    /**
     * 获取作品分类统计
     * GET /api/works/category/stats
     */
    @GetMapping("/category/stats")
    public ResponseVO<List<Map<String, Object>>> getCategoryStats() {
        try {
            List<Map<String, Object>> stats = worksService.getCategoryStats();
            return ResponseVO.success(stats);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseVO.error(500, "服务器内部错误：" + e.getMessage());
        }
    }

    /**
     * 获取作品分类统计（带总数）
     * GET /api/works/category/stats-with-total
     */
    @GetMapping("/category/stats-with-total")
    public ResponseVO<Map<String, Object>> getCategoryStatsWithTotal() {
        try {
            List<Map<String, Object>> stats = worksService.getCategoryStats();
            Integer total = 0;

            // 计算总数
            for (Map<String, Object> stat : stats) {
                total += ((Number) stat.get("count")).intValue();
            }

            Map<String, Object> result = new HashMap<>();
            result.put("total", total);
            result.put("categories", stats);

            return ResponseVO.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseVO.error(500, "服务器内部错误：" + e.getMessage());
        }
    }
    /**
     * 作品详情接口
     * GET /api/works/detail
     *
     * 参数：
     * - works_id: 作品ID（必填）
     */
    @GetMapping("/detail")
    public ResponseVO<WorksVO> getWorksDetail(
            @RequestParam("works_id") Integer works_id) {

        try {
            if (works_id == null) {
                return ResponseVO.error("参数错误：works_id不能为空");
            }

            WorksVO worksVO = worksService.getWorksDetail(works_id);
            if (worksVO == null) {
                return ResponseVO.error(404, "未找到该作品");
            }

            return ResponseVO.success(worksVO);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseVO.error(500, "服务器内部错误");
        }
    }
    /**
     * 获取所有标签列表（带统计）
     * GET /api/works/tags
     */
    @GetMapping("/tags")
    public ResponseVO<List<Map<String, Object>>> getAllTags() {
        try {
            List<Map<String, Object>> tags = worksService.getAllTags();
            return ResponseVO.success(tags);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseVO.error(500, "服务器内部错误：" + e.getMessage());
        }
    }
    /**
     * 根据标签获取作品列表
     * GET /api/works/by-tag
     *
     * 参数：
     * - tag_id: 标签ID（必填）
     * - page: 页码（可选，默认1）
     * - size: 每页条数（可选，默认10）
     */
    @GetMapping("/by-tag")
    public ResponseVO<Map<String, Object>> getWorksByTag(
            @RequestParam("tag_id") Integer tagId,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size) {

        try {
            if (tagId == null || tagId <= 0) {
                return ResponseVO.error("参数错误：tag_id必须为正整数");
            }

            Map<String, Object> result = worksService.getWorksByTag(tagId, page, size);
            return ResponseVO.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseVO.error(500, "服务器内部错误：" + e.getMessage());
        }

    }
    @GetMapping("/tags/search")
    public ResponseVO<List<Map<String, Object>>> searchTags(
            @RequestParam(required = false) String keyword) {
        try {
            List<Map<String, Object>> tags = worksService.searchTags(keyword);
            return ResponseVO.success(tags);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseVO.error(500, "服务器内部错误：" + e.getMessage());
        }
    }

    @GetMapping("/by-period")
    public ResponseVO<Map<String, Object>> byPeriod(
            @RequestParam String period,   // early / middle / late
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        // 时期 -> 年份区间
        int[] range = switch (period) {
            case "early"  -> new int[]{1865, 1900};
            case "middle" -> new int[]{1901, 1930};
            case "late"   -> new int[]{1931, 1955};
            default -> throw new IllegalArgumentException("period 只能是 early/middle/late");
        };

        Map<String, Object> res = worksService.getWorksByYearRange(range[0], range[1], page, size);
        return ResponseVO.success(res);
    }
    /**
     * 测试接口
     * GET /api/works/test
     */
    @GetMapping("/test")
    public ResponseVO<String> test() {
        return ResponseVO.success("作品接口测试成功！");
    }
}