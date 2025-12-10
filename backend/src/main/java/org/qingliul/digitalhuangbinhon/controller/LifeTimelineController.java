package org.qingliul.digitalhuangbinhon.controller;

import org.qingliul.digitalhuangbinhon.entity.vo.ResponseVO;
import org.qingliul.digitalhuangbinhon.service.LifeTimelineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/timeline")
public class LifeTimelineController {

    @Autowired
    private LifeTimelineService lifeTimelineService;

    /**
     * 生平事件列表接口
     * GET /api/timeline/list
     *
     * 参数：
     * - year: 年份（可选）
     * - page: 页码（可选，默认1）
     * - size: 每页条数（可选，默认10）
     */
    @GetMapping("/list")
    public ResponseVO<Map<String, Object>> getLifeTimelineList(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size) {

        try {
            Map<String, Object> result = lifeTimelineService.getLifeTimelineList(year, page, size);
            return ResponseVO.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseVO.error(500, "服务器内部错误：" + e.getMessage());
        }
    }

    /**
     * 生平事件详情接口
     * GET /api/timeline/detail
     *
     * 参数：
     * - timeline_id: 生平事件ID（必填）
     */
    @GetMapping("/detail")
    public ResponseVO<Map<String, Object>> getLifeTimelineDetail(
            @RequestParam("timeline_id") Integer timelineId) {

        try {
            if (timelineId == null) {
                return ResponseVO.error("参数错误：timeline_id不能为空");
            }

            Map<String, Object> timelineDetail = lifeTimelineService.getLifeTimelineDetail(timelineId);
            if (timelineDetail == null) {
                return ResponseVO.error(404, "未找到该生平事件");
            }

            return ResponseVO.success(timelineDetail);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseVO.error(500, "服务器内部错误");
        }
    }
    @GetMapping("/year/art-stats")
    public ResponseVO<List<Map<String, Object>>> getYearArtStats() {
        return ResponseVO.success(lifeTimelineService.selectYearArtStats());
    }
    /**
     * 测试接口
     * GET /api/timeline/test
     */
    @GetMapping("/test")
    public ResponseVO<String> test() {
        return ResponseVO.success("生平编年体接口测试成功！");
    }
}