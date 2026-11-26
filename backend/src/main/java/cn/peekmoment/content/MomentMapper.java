package cn.peekmoment.content;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface MomentMapper extends BaseMapper<MomentEntity> {
    record Row(long id, String userId, String author, String title, String body, long createdAt, String status) {}
    record Tag(long momentId, String name) {}
    record Media(long momentId, String kind, String src) {}

    String COLUMNS = "SELECT m.id, m.user_id AS userId, u.nickname AS author, m.title, m.body, m.created_at AS createdAt, m.status ";
    String JOIN_USERS = " FROM moments m JOIN users u ON u.id = m.user_id ";
    String FILTER = """
        <where>
            <if test='!admin'> AND m.status = 'published' </if>
            <if test="keyword != null and keyword != ''">
                AND (LOCATE(LOWER(#{keyword}), LOWER(m.title)) > 0
                  OR LOCATE(LOWER(#{keyword}), LOWER(m.body)) > 0
                  OR LOCATE(LOWER(#{keyword}), LOWER(u.nickname)) > 0
                  OR LOCATE(#{keyword}, CONCAT('', m.id)) > 0)
            </if>
        </where>
        """;
    String IDS = " <foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach> ";

    @Select("<script>SELECT COUNT(*)" + JOIN_USERS + FILTER + "</script>")
    long countPage(@Param("admin") boolean admin, @Param("keyword") String keyword);

    @Select("<script>" + COLUMNS + JOIN_USERS + FILTER
        + " ORDER BY m.created_at DESC, m.id DESC LIMIT #{size} OFFSET #{offset}</script>")
    List<Row> pageRows(@Param("admin") boolean admin, @Param("keyword") String keyword,
                       @Param("size") int size, @Param("offset") long offset);

    @Select(COLUMNS + JOIN_USERS + " WHERE m.id = #{id}")
    Row moment(@Param("id") long id);

    @Select("<script>SELECT mt.moment_id AS momentId, t.name FROM moment_tags mt "
        + "JOIN tags t ON t.tag_key = mt.tag_key WHERE mt.moment_id IN " + IDS
        + " ORDER BY mt.moment_id, mt.sort_order</script>")
    List<Tag> tags(@Param("ids") List<Long> ids);

    @Select("<script>SELECT moment_id AS momentId, kind, src FROM moment_media WHERE moment_id IN "
        + IDS + " ORDER BY moment_id, sort_order</script>")
    List<Media> media(@Param("ids") List<Long> ids);

    @Insert("INSERT INTO tags (tag_key, name) VALUES (#{key}, #{name}) ON DUPLICATE KEY UPDATE name = VALUES(name)")
    void ensureTag(@Param("key") byte[] key, @Param("name") String name);

    @Insert("INSERT INTO moment_tags (moment_id, tag_key, sort_order) VALUES (#{id}, #{key}, #{position})")
    void attachTag(@Param("id") long id, @Param("key") byte[] key, @Param("position") int position);

    @Insert("INSERT INTO moment_media (moment_id, kind, src, sort_order) VALUES (#{id}, #{kind}, #{src}, #{position})")
    void attachMedia(@Param("id") long id, @Param("kind") String kind, @Param("src") String src,
                     @Param("position") int position);
}
