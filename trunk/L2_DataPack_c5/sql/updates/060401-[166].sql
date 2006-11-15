insert into henna_trees (class_id, symbol_id)
select b.id, c.symbol_id  from henna_trees as a, class_list as b, henna as c
where a.class_id = b.parent_id
and a.symbol_id = c.symbol_id
and b.id > 87;