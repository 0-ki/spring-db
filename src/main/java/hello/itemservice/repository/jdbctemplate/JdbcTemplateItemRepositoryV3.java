package hello.itemservice.repository.jdbctemplate;

import hello.itemservice.domain.Item;
import hello.itemservice.repository.ItemRepository;
import hello.itemservice.repository.ItemSearchCond;
import hello.itemservice.repository.ItemUpdateDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/*
* + SimpleJdbcInsert
*
* NamedParameterJdbcTemplate
*
* String sql = "select name from ... where id = :id "  :id 는 java 변수
*
* SqlParameterSource ( Interface )
* - BeanPropertySqlParameterSource(item) - 변수의 속성에 맞게 매핑
* - MapSqlParameterSource().addValue("id", itemId) - Builder 쓰듯이 메서드 체인으로
* Map.of ( Map의 기능임 ) Java 9
*
* BeanPropertyRowMapper.newInstance( Item.class ) - RowMapper 자동 생성
* */
@Slf4j
public class JdbcTemplateItemRepositoryV3 implements ItemRepository {
//    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    public JdbcTemplateItemRepositoryV3(DataSource dataSource) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        this.jdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("item")
                .usingGeneratedKeyColumns("id");
//                .usingColumns("item_name", "price", "quantity") // dataSource로부터 가져오므로, 생략 가능.
    }

    @Override
    public Item save(Item item) {

        SqlParameterSource param = new BeanPropertySqlParameterSource(item);
        Number key = jdbcInsert.executeAndReturnKey(param);
        item.setId(key.longValue());
        return item;

        /*
        String sql = "insert into item(item_name, price, quantity) " +
                "values (:itemName, :price, :quantity)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        SqlParameterSource param = new BeanPropertySqlParameterSource(item);
        jdbcTemplate.update(sql, param, keyHolder);
        */
        /*
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, item.getItemName());
            ps.setInt(2, item.getPrice());
            ps.setInt(3, item.getQuantity());
            return ps;
        }, keyHolder);
        */
        /*

        long key = keyHolder.getKey().longValue();
        item.setId( key);
        return item;
        */
    }

    @Override
    public void update(Long itemId, ItemUpdateDto updateParam) {
        String sql = "update item set item_name = :itemName, price = :price, quantity = :quantity" +
                " where id = :id";

        SqlParameterSource param = new MapSqlParameterSource()
                .addValue("itemName", updateParam.getItemName())
                .addValue("price", updateParam.getPrice())
                .addValue("quantity", updateParam.getQuantity())
                .addValue("id", itemId);
        jdbcTemplate.update(sql, param);

        /*
        jdbcTemplate.update(sql,
                updateParam.getItemName(),
                updateParam.getPrice(),
                updateParam.getQuantity(),
                itemId);
        */
    }

    @Override
    public Optional<Item> findById(Long id) {
        String sql = "select id, item_name, price, quantity from item where id = :id";
        try{
            Map<String, Object> param = Map.of("id", id);
            Item item = jdbcTemplate.queryForObject(sql, param, itemRowMapper());
//            Item item = jdbcTemplate.queryForObject(sql, itemRowMapper(), id);
            return Optional.of( item);

        } catch (EmptyResultDataAccessException e) {
            e.getMessage();
            return Optional.empty();
        }
    }

    @Override
    public List<Item> findAll(ItemSearchCond cond) {
        String itemName = cond.getItemName();
        Integer maxPrice = cond.getMaxPrice();
        String sql = "select id, item_name, price, quantity from item";

        SqlParameterSource param = new BeanPropertySqlParameterSource(cond);

        //동적 쿼리
        if (StringUtils.hasText(itemName) || maxPrice != null) {
            sql += " where";
        }
        boolean andFlag = false;
//        List<Object> param = new ArrayList<>();
        if (StringUtils.hasText(itemName)) {
            sql += " item_name like concat('%',:itemName,'%')";
//            param.add(itemName);
            andFlag = true;
        }
        if (maxPrice != null) {
            if (andFlag) {
                sql += " and";
            }
            sql += " price <= :maxPrice";
//            param.add(maxPrice);
        }
        log.info("sql={}", sql);
        return jdbcTemplate.query(sql, param, itemRowMapper());
//        return jdbcTemplate.query(sql, itemRowMapper(), param.toArray());
    }

    private RowMapper<Item> itemRowMapper() {

        // setItemName이 아닌 item_name도 매핑해준다!! member_name 을 userName객체 매핑하고 싶으면 select member_name as userName 하면 됨.
        return BeanPropertyRowMapper.newInstance(Item.class);

        /*
        return ((rs, rowNum) -> {
           Item item = new Item();
           item.setId(rs.getLong("id"));
           item.setItemName((rs.getString("item_name")));
           item.setPrice(rs.getInt("price"));
           item.setQuantity(rs.getInt("quantity"));
            return item;
        });
        */
    }


}
