package com.example.labelMark.mapper;

import com.example.labelMark.domain.DatasetStore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.labelMark.domain.ImageInfo;
import org.apache.ibatis.annotations.*;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author hjw
 * @since 2024-05-08
 */
@Mapper
public interface DatasetStoreMapper extends BaseMapper<DatasetStore> {


//    @Results({
//            @Result(property = "task.taskId", column = "task_id"),
//            @Result(property = "task.taskName", column = "task_name"),
//            @Result(property = "task.taskType", column = "task_type"),
//            @Result(property = "task.mapServer", column = "map_server"),
//            @Result(property = "task.dateRange", column = "date_range"),
//            @Result(property = "task.markTable", column = "mark_table"),
//            @Result(property = "task.auditFeedback", column = "audit_feedback"),
//            @Result(property = "sampleId", column = "sample_id"),
//            @Result(property = "sampleName", column = "sample_name"),
//            @Result(property = "isPublic", column = "is_public")
//    })
    @Select("SELECT task.*, dataset_store.sample_id, dataset_store.sample_name, dataset_store.is_public " +
            "FROM task " +
            "JOIN dataset_store ON dataset_store.task_id = #{taskId} " +
            "WHERE task.task_id = #{taskId}")
    List<Map<String, Object>> findDatasetByTaskId(@Param("taskId") int taskId);

    @Select({"<script>",
            "SELECT COUNT(*) as count " ,
            "FROM sample_img ",
            "<when test='sampleId!=null'>",
            "WHERE sample_id = #{sampleId}",
            "</when>",
            "</script>"})
    int getTotalImgNumBySampleId(Integer sampleId);

    @Select({"<script>",
            "SELECT sample_img.img_src, type.type_name ",
            "FROM sample_img",
            "JOIN type ON sample_img.type_id = type.type_id ",
            "<when test='sampleId!=null'>",
            "WHERE sample_img.sample_id = #{sampleId}",
            "</when>",
            "LIMIT #{pageSize} OFFSET #{current} ",
            "</script>"})
    List<ImageInfo> findImgSrcTypeNameBySampleId(Integer sampleId, int pageSize, int current);

    @Update("update dataset_store SET is_public=#{isPublic} WHERE sample_id=#{sampleId}")
    void updateDatasetStatusBySampleId(int isPublic, int sampleId);

    @Select("select COUNT(*) as count from dataset_store where task_id=#{taskId}")
    Integer hasGenerateDataset(int taskId);

    @Insert("INSERT INTO dataset_store (task_id, is_public) VALUES (#{taskId}, #{isPublic})")
    @Options(useGeneratedKeys = true, keyProperty = "sampleId", keyColumn = "sample_id")
    void createDataset(DatasetStore datasetStore);
}
