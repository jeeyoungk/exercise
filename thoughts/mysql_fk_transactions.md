testing mysql locks on foreign keys
```sql
CREATE TABLE parent (
    p_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(32)
);

CREATE TABLE child (
    c_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    p_id INT NOT NULL,
    name VARCHAR(32),
    FOREIGN KEY (p_id) REFERENCES parent(p_id)
);

insert into parent (p_id, name) values (1, 'papa');
insert into parent (p_id, name) values (2, 'mama');
insert into child (name, p_id) values ('joe', 1);
-- transaction 1
-- update child set p_id = 2 where name = 'joe';
insert into child (name, p_id) values ('sarah', 2); -- lock on (2)
-- transaction 2
select * from parent where p_id = 2 for update;

```
