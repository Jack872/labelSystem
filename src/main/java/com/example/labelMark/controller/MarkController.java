package com.example.labelMark.controller;

import cn.hutool.core.util.ObjectUtil;
import com.example.labelMark.domain.Mark;
import com.example.labelMark.domain.Type;
import com.example.labelMark.service.MarkService;
import com.example.labelMark.service.TaskService;
import com.example.labelMark.utils.CoordinateConverter;
import com.example.labelMark.utils.ResultGenerator;
import com.example.labelMark.vo.constant.Result;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author hjw
 * @since 2024-04-28
 */
@RestController
@RequestMapping("/mark")
public class MarkController {

    @Resource
    private MarkService markService;

    @Resource
    private TaskService taskService;

    @PostMapping("/saveMarkInfo")
    public Result saveMarkInfo(@RequestBody Map<String, Object> request) {
        Integer userId = Integer.valueOf(request.get("userid").toString());
        Integer taskId = Integer.valueOf(request.get("id").toString());
        List<Map<String, Object>> typeIdAndMarkInfoArr = (List<Map<String, Object>>) request.get("jsondataArr");
        List<Map<String, Object>> typeMapArr = (List<Map<String, Object>>) request.get("typeArr");
        List<Type> typeArr = new ArrayList<>();
        for (Map typeMap : typeMapArr) {
            Type type = new Type();
            Integer typeId = Integer.valueOf(typeMap.get("typeId").toString());
            String typeName = typeMap.get("typeName").toString();
            String typeColor = typeMap.get("typeColor").toString();
            type.setTypeColor(typeColor);
            type.setTypeName(typeName);
            type.setTypeId(typeId);
            typeArr.add(type);
        }
        List<Map<String, Object>> geometryArr = CoordinateConverter.convertCoordinate(typeIdAndMarkInfoArr);
        List<Map<String, Object>> markInfoArr=geometryArr;
        if (markInfoArr.isEmpty()) {
            return ResultGenerator.getSuccessResult("没有标注信息，已删除多余Type");
        }
//                筛选需要删除的标记
        List<Mark> total = markService.getTotal();
        for (Map<String, Object> geomAndTypeId : markInfoArr) {
            Object markIdObj = geomAndTypeId.get("markId");
            if (ObjectUtil.isNotNull(markIdObj)){
                Integer markId = Integer.valueOf(markIdObj.toString());
//            排除了添加的标记(无markId)，还剩下需要修改，无变化（前两类都返回了markId）
//            和需要删除（不返回markId）的标记
                Mark mark = markService.selectByMarkId(markId);
//                属于前两类,排除
                if (ObjectUtil.isNotNull(mark)){
                    total = total.stream()
                            .filter(totalMarkItem -> totalMarkItem.getId() != markId)
                            .collect(Collectors.toList());
                }
            }
        }
//                删除需要删除的标记
        markService.deleteMarks(total);
        //此任务此用户是否已经标注
        boolean exist = markService.isMark(taskId, userId);
        if(exist) {
//            清空任务表中已存在的标注ID
            taskService.updateTask(taskId, null);
            updateTaskAndMark(userId, taskId, markInfoArr);
            return ResultGenerator.getSuccessResult("已有有标注信息，已完成更新");
        }else {
            updateTaskAndMark(userId, taskId, markInfoArr);
        }
        return ResultGenerator.getSuccessResult("mark创建成功");
    }

    /**
     * 接收前端对标记的更改，更新或添加标记和任务信息
     * @param userId
     * @param taskId
     * @param markInfoArr
     */
    private void updateTaskAndMark(Integer userId, int taskId, List<Map<String, Object>> markInfoArr) {
        for (Map<String, Object> geomAndTypeId : markInfoArr) {
            Mark mark = new Mark();
            Integer markId = ObjectUtil.isNotNull(geomAndTypeId.get("markId"))
                    ?Integer.valueOf(geomAndTypeId.get("markId").toString()):null;
            mark.setId(markId);
            mark.setTaskId(taskId);
            mark.setUserId(userId);
            mark.setGeom(geomAndTypeId.get("geom").toString());
            mark.setStatus(0);
            mark.setTypeId(Integer.valueOf(geomAndTypeId.get("typeId").toString()));
            markService.insertOrUpdateMark(mark);
            //更新任务的标注ID
            String markIdStr = taskService.getMarkIdById(taskId);
            markIdStr = markIdStr == null ? mark.getId().toString()
                    : markIdStr + "," + mark.getId().toString();
            taskService.updateTask(taskId, markIdStr);
        }
    }
}
