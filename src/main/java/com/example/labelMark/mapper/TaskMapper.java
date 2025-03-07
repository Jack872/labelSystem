package com.example.labelMark.mapper;

import com.example.labelMark.domain.Task;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author hjw
 * @since 2024-04-25
 */
@Mapper
public interface TaskMapper extends BaseMapper<Task> {

    /*@MapKey("task_id")
    List<Map<String, Object>> getTaskInfo();*/

    @Select({"<script>",
            "select task_accepted.id,task_accepted.type_arr,sys_user.username as username,",
            "sys_user.user_id as userid,task.* from task join task_accepted on task.task_id=task_accepted.task_id",
            "join sys_user on  task_accepted.username=sys_user.username ",
            "<when test='username!=null'>",
            "where sys_user.username=#{username}",
            "</when>",
            "<when test='taskid!=null'>",
            "where task.task_id=#{taskid}",
            "</when>",
            "order by status,task_id DESC ",
            "</script>"})
    List<Map<String, Object>> getTaskInfo(String username,Integer taskid);

    @Select({"<script>",
            "select task_accepted.id,task_accepted.type_arr,sys_user.username as username,",
            "sys_user.user_id as userid,task.* from task join task_accepted on task.task_id=task_accepted.task_id",
            "join sys_user on  task_accepted.username=sys_user.username ",
            "<when test='taskid!=null'>",
            "where task.task_id=#{taskid}",
            "</when>",
            "order by status,task_id DESC ",
            "</script>"})
    List<Map<String, Object>> getAllTaskInfo(Integer taskid);

    @Select("select task_id = #{taskId} from task")
    List<Integer> getIDs();

    @Update("update task set task_name=#{taskName}, date_range=#{dateRange}, task_type=#{taskType}, map_server=#{mapServer} where task_id=#{taskId}")
    void updateTaskById(int taskId, String taskName, String dateRange, String taskType, String mapServer);

    @Select("SELECT * FROM task WHERE task_id = #{taskId}")
    Task selectTaskById(int taskId);

    @Update("UPDATE task SET status=0 WHERE task_id = #{taskId}")
    void updateTaskStatus(int taskId);

    @Update("UPDATE task SET status=#{status}, audit_feedback=#{auditFeedback} WHERE task_id = #{taskId}")
    void auditTask(int taskId, int status, String auditFeedback);

    @Select("SELECT task.task_id " +
            "FROM task " +
            "JOIN dataset_store ON dataset_store.task_id = task.task_id")
    List<Map<String, Object>> findAllTask();


    @Select("SELECT task.task_id " +
            "FROM task " +
            "JOIN dataset_store ON dataset_store.task_id = task.task_id " +
            "WHERE dataset_store.is_public = 1")
    List<Map<String, Object>> findPublicTask();

    @Select("SELECT task_id FROM task_accepted WHERE username=#{username}")
    List<Map<String, Object>> findTasksByUsername(String username);

    @Select("SELECT username FROM task_accepted WHERE task_id=#{taskId}")
    List<String> findUserListByTaskId(int taskId);

    @Update("UPDATE task SET mark_id=#{markIdStr} WHERE task_id=#{taskId}")
    void updateTask(int taskId, String markIdStr);

    @Select("select map_server FROM task where task_id=#{taskId}")
    String getServerById(int taskId);

    @Select("select task_type FROM task where task_id=#{taskId}")
    String getTypeById(int taskId);
}
